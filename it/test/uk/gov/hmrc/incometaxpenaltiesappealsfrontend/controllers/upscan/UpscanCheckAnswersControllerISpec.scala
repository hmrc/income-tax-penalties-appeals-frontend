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
import fixtures.messages.NonJsUploadCheckAnswersMessages
import fixtures.views.BaseSelectors
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{NOT_IMPLEMENTED, OK, SEE_OTHER}
import play.api.{Application, inject}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadAnotherFileForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.{FileUploadJourneyRepository, UserAnswersRepository}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, TimeMachine, ViewSpecHelper}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class UpscanCheckAnswersControllerISpec extends ComponentSpecHelper with ViewSpecHelper with NavBarTesterHelper
  with AuthStub
  with FileUploadFixtures {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val timeMachine: TimeMachine = new TimeMachine {
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
    userAnswersRepo.upsertUserAnswer(UserAnswers(testJourneyId)).futureValue
    super.beforeEach()
  }

  Seq(
    false -> successfulIndividualAuthResponse,
    true -> successfulAgentAuthResponse
  ).foreach { case (isAgent, authResponse) =>

    s"when authenticating as an ${if (isAgent) "agent" else "individual"}" when {

      if(!isAgent) {
        testNavBar("/upload-supporting-evidence/check-answers"){
          stubAuth(OK, authResponse)
          fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
        }
      }

      "GET /upload-supporting-evidence/check-answers" when {

        s"the number of files uploaded is < ${appConfig.upscanMaxNumberOfFiles}" should {

          "render the File Upload check answers page with a form action to add another file" in {
            stubAuth(OK, authResponse)
            fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

            val result = get("/upload-supporting-evidence/check-answers", isAgent = isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)
            document.select("form").attr("action") shouldBe routes.UpscanCheckAnswersController.onSubmit().url
            document.select(BaseSelectors.legend).text() shouldBe NonJsUploadCheckAnswersMessages.English.uploadAnotherFileLegend
            document.select(BaseSelectors.radio(1)).text() shouldBe NonJsUploadCheckAnswersMessages.English.yes
            document.select(BaseSelectors.radio(2)).text() shouldBe NonJsUploadCheckAnswersMessages.English.no
          }
        }

        s"the number of files uploaded is == ${appConfig.upscanMaxNumberOfFiles}" should {

          "render the File Upload check answers page with a form action but without the 'Add another file' question" in {
            stubAuth(OK, authResponse)

            (1 to appConfig.upscanMaxNumberOfFiles).foreach { i =>
              fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel.copy(reference = s"ref$i")).futureValue
            }

            val result = get("/upload-supporting-evidence/check-answers", isAgent = isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)
            document.select("form").attr("action") shouldBe routes.UpscanCheckAnswersController.onSubmit().url
            document.select(BaseSelectors.legend).isEmpty shouldBe true
            document.select(BaseSelectors.radio(1)).isEmpty shouldBe true
            document.select(BaseSelectors.radio(2)).isEmpty shouldBe true
          }
        }
      }

      "POST /upload-supporting-evidence/check-answers" when {

        s"number of files which has been uploaded is < ${appConfig.upscanMaxNumberOfFiles}" when {

          "the User selects 'Yes' to upload another file" should {

            "redirect to the UpscanInitiate page" in {

              stubAuth(OK, authResponse)
              fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

              val result = post("/upload-supporting-evidence/check-answers", isAgent = isAgent)(
                Map(UploadAnotherFileForm.key -> "true")
              )

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(routes.UpscanInitiateController.onPageLoad().url)
            }
          }

          "the User selects 'No' to NOT upload another file" should {

            //TODO: Upload this as part of future story
            "redirect to ??? page" in {

              stubAuth(OK, authResponse)
              fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

              val result = post("/upload-supporting-evidence/check-answers", isAgent = isAgent)(
                Map(UploadAnotherFileForm.key -> "false")
              )

              result.status shouldBe NOT_IMPLEMENTED
              //TODO: Add these tests as part of future story when routing is known
              //result.status shouldBe SEE_OTHER
              //result.header("Location") shouldBe ???
            }
          }
        }

        s"number of files which has been uploaded is == ${appConfig.upscanMaxNumberOfFiles}" should {

          //TODO: Upload this as part of future story
          "redirect to ??? page" in {

            stubAuth(OK, authResponse)
            (1 to appConfig.upscanMaxNumberOfFiles).foreach { i =>
              fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel.copy(reference = s"ref$i")).futureValue
            }

            val result = post("/upload-supporting-evidence/check-answers", isAgent = isAgent)(Map.empty[String, String])

            result.status shouldBe NOT_IMPLEMENTED
            //TODO: Add these tests as part of future story when routing is known
            //result.status shouldBe SEE_OTHER
            //result.header("Location") shouldBe ???
          }
        }
      }
    }
  }
}
