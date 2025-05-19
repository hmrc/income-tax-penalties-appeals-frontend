/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.upscan

import fixtures.FileUploadFixtures
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, NOT_IMPLEMENTED, OK, SEE_OTHER}
import play.api.test.Helpers.LOCATION
import play.api.{Application, inject}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.{FileUploadJourneyRepository, UserAnswersRepository}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.UpscanStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import utils.TimerUtil

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class UpscanInitiateControllerISpec extends ControllerISpecHelper
  with UpscanStub
  with FileUploadFixtures
  with TimerUtil {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val timeMachine: TimeMachine = new TimeMachine(appConfig) {
    override def getCurrentDateTime: LocalDateTime = testDateTime
  }

  override lazy val app: Application = appWithOverrides(
    inject.bind[TimeMachine].toInstance(timeMachine)
  )

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]
  lazy val fileUploadRepo: FileUploadJourneyRepository = app.injector.instanceOf[FileUploadJourneyRepository]

  val testDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    fileUploadRepo.collection.deleteMany(Document()).toFuture().futureValue
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
    super.beforeEach()
  }

  Seq(
    false,
    true
  ).foreach { isAgent =>

    s"when authenticating as an ${if (isAgent) "agent" else "individual"}" when {

      s"GET /upload-evidence/upload-file" when {

        "loading normal variant of the page (i.e. User is initiating the upload for the first time)" when {

          "a success response is returned from the upscan-initiate call" should {

            "add a new entry to File Upload repo AND render the page with the file upload meta data" in {
              stubAuthRequests(isAgent)
              stubUpscanInitiate(status = OK, body = initiateResponse)

              val uploadFileUrl = if(isAgent) {"agent-upload-file"} else {"upload-file"}
              val result = get(s"/upload-evidence/$uploadFileUrl", isAgent = isAgent)
              result.status shouldBe OK

              val document = Jsoup.parse(result.body)
              document.select("form").attr("action") shouldBe initiateResponse.uploadRequest.href
              initiateResponse.uploadRequest.fields.map { case (key, value) =>
                document.select(s"input[name=$key]").`val`() shouldBe value
              }

              fileUploadRepo.getFile(testJourneyId, initiateResponse.reference).futureValue shouldBe
                Some(waitingFile.copy(lastUpdated = testDateTime))
            }
          }
        }

        "loading the error variant of the page (i.e. a synchronous failure has been returned when the User submitted the file to upscan)" when {

          "a file upload journey exists within the File Upload repo" should {

            "render a BadRequest, NOT call the initiate endpoint and should re-use the data from the File Upload repo for the hidden file meta data input" in {
              stubAuthRequests(isAgent)
              fileUploadRepo.upsertFileUpload(testJourneyId, waitingFile).futureValue

              val uploadFileUrl = if(isAgent) {"agent-upload-file"} else {"upload-file"}
              val result = get(s"/upload-evidence/$uploadFileUrl?key=$fileRef1&errorCode=UnableToUpload", isAgent = isAgent)
              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)
              document.select("form").attr("action") shouldBe waitingFile.uploadFields.get.href
              waitingFile.uploadFields.get.fields.map { case (key, value) =>
                document.select(s"input[name=$key]").`val`() shouldBe value
              }
            }
          }

          "a file upload journey DOES NOT exist within the File Upload repo" should {

            "render a BadRequest, call the initiate endpoint and should use the data from the initiate response for the hidden file meta data input" in {
              stubAuthRequests(isAgent)
              stubUpscanInitiate(status = OK, body = initiateResponse)

              val uploadFileUrl = if(isAgent) {"agent-upload-file"} else {"upload-file"}
              val result = get(s"/upload-evidence/$uploadFileUrl?key=$fileRef1&errorCode=UnableToUpload", isAgent = isAgent)
              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)
              document.select("form").attr("action") shouldBe initiateResponse.uploadRequest.href
              initiateResponse.uploadRequest.fields.map { case (key, value) =>
                document.select(s"input[name=$key]").`val`() shouldBe value
              }

              fileUploadRepo.getFile(testJourneyId, initiateResponse.reference).futureValue shouldBe
                Some(waitingFile.copy(lastUpdated = testDateTime))
            }
          }
        }
      }

      s"GET /upload-evidence/success-redirect" when {

        "the callback from upscan is received with a key (fileReference)" when {

          "an entry for that key exists in the File Upload repo" when {

            "the file status is 'READY'" should {

              s"redirect to the File Upload Check Answers page after a configured artificial delay of ${appConfig.upscanCheckInterval.toMillis}ms" in {

                stubAuthRequests(isAgent)
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

                calculateRuntime {
                  val result = get(s"/upload-evidence/success-redirect?key=$fileRef1", isAgent = isAgent)
                  result.status shouldBe SEE_OTHER
                  result.header(LOCATION) shouldBe Some(routes.UpscanCheckAnswersController.onPageLoad(isAgent).url)
                }.shouldTakeAtLeast(appConfig.upscanCheckInterval)
              }
            }

            "the file status is still 'WAITING' after configured wait time" should {

              //TODO: Update this test in future story to test the redirect to the "It's taking longer than expected" page
              s"redirect to the 'Taking longer than expected' page after a configured total wait time of ${appConfig.upscanTimeout.toMillis}ms" in {
                stubAuthRequests(isAgent)
                fileUploadRepo.upsertFileUpload(testJourneyId, waitingFile).futureValue

                calculateRuntime {
                  val result = get(s"/upload-evidence/success-redirect?key=$fileRef1", isAgent = isAgent)
                  result.status shouldBe NOT_IMPLEMENTED
                }.shouldTakeAtLeast(appConfig.upscanTimeout)
              }
            }

            "the file status is 'FAILED'" should {

              s"redirect to the file upload page with the failureReason as the errorCode after a configured artificial delay ${appConfig.upscanCheckInterval.toMillis}ms" in {
                stubAuthRequests(isAgent)
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModelFailed).futureValue

                calculateRuntime {
                  val result = get(s"/upload-evidence/success-redirect?key=$fileRef1", isAgent = isAgent)
                  result.status shouldBe SEE_OTHER
                  result.header(LOCATION) shouldBe Some(routes.UpscanInitiateController.onPageLoad(Some(fileRef1), callbackModelFailed.failureDetails.map(_.failureReason.toString), isAgent).url)
                }.shouldTakeAtLeast(appConfig.upscanCheckInterval)
              }
            }
          }

          "an entry for that key exists DOES NOT exist in the File Upload repo" should {

            "redirect to the initiate upload page with the errorCode set to 'UnableToUpload'" in {
              stubAuthRequests(isAgent)

              val result = get(s"/upload-evidence/success-redirect?key=$fileRef1", isAgent = isAgent)
              result.status shouldBe SEE_OTHER
              result.header(LOCATION) shouldBe Some(routes.UpscanInitiateController.onPageLoad(errorCode = Some("UnableToUpload"), isAgent = isAgent).url)
            }
          }
        }
      }
    }
  }
}
