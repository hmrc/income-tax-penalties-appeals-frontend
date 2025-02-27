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
import fixtures.messages.upscan.NonJsRemoveFileMessages
import fixtures.views.BaseSelectors
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.{Application, inject}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.{routes => appealsRoutes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadRemoveFileForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.{FileUploadJourneyRepository, UserAnswersRepository}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, TimeMachine, ViewSpecHelper}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class UpscanRemoveFileControllerISpec extends ComponentSpecHelper with ViewSpecHelper with NavBarTesterHelper
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
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
    super.beforeEach()
  }

  Seq(
    false -> successfulIndividualAuthResponse,
    true -> successfulAgentAuthResponse
  ).foreach { case (isAgent, authResponse) =>

    s"when authenticating as an ${if (isAgent) "agent" else "individual"}" when {

      if(!isAgent) {
        testNavBar(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1") {
          stubAuth(OK, authResponse)
          fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
        }
      }

      s"GET /upload-supporting-evidence/remove-file" when {

        "the file does not exists" should {

          "redirect to the Upscan Check Answers page" in {
            stubAuth(OK, authResponse)

            val result = get(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)
            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.UpscanCheckAnswersController.onPageLoad().url)
          }
        }

        "the file is in the wrong state" should {

          "redirect to the Upscan Check Answers page" in {
            stubAuth(OK, authResponse)
            fileUploadRepo.upsertFileUpload(testJourneyId, waitingFile).futureValue

            val result = get(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)
            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.UpscanCheckAnswersController.onPageLoad().url)
          }
        }

        "the file exists in the READY state" should {

          "render the Remove File page with Yes No radio" in {
            stubAuth(OK, authResponse)
            fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

            val result = get(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)
            document.select("form").attr("action") shouldBe routes.UpscanRemoveFileController.onSubmit(fileRef1, 1).url
            document.select(BaseSelectors.legend).text() shouldBe NonJsRemoveFileMessages.English.headingAndTitle(1)
            document.select(BaseSelectors.radio(1)).text() shouldBe NonJsRemoveFileMessages.English.yes
            document.select(BaseSelectors.radio(2)).text() shouldBe NonJsRemoveFileMessages.English.no
          }
        }
      }

      "POST /upload-supporting-evidence/remove-file" when {

        "the file does not exists" should {

          "redirect to the Upscan Check Answers page" in {
            stubAuth(OK, authResponse)

            val result = post(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)(Map(UploadRemoveFileForm.key -> "true"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(appealsRoutes.ExtraEvidenceController.onPageLoad().url)
          }
        }

        "the file is in the wrong state" should {

          "redirect to the Upscan Check Answers page" in {
            stubAuth(OK, authResponse)
            fileUploadRepo.upsertFileUpload(testJourneyId, waitingFile).futureValue

            val result = post(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)(Map(UploadRemoveFileForm.key -> "true"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(appealsRoutes.ExtraEvidenceController.onPageLoad().url)
          }
        }

        "the file is in the READY state" when {

          "the user selects 'Yes' to delete the file" when {

            "it's the last file that's being removed" should {

              "redirect to the Extra Evidence page, removing the file" in {
                stubAuth(OK, authResponse)
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
                fileUploadRepo.getAllFiles(testJourneyId).futureValue.size shouldBe 1

                val result = post(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)(Map(UploadRemoveFileForm.key -> "true"))

                result.status shouldBe SEE_OTHER
                result.header("Location") shouldBe Some(appealsRoutes.ExtraEvidenceController.onPageLoad().url)

                fileUploadRepo.getAllFiles(testJourneyId).futureValue.size shouldBe 0
              }
            }

            "it's NOT the last file that's being removed" should {

              "redirect to the Upscan Check Answers page, removing the file" in {
                stubAuth(OK, authResponse)
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel2).futureValue
                fileUploadRepo.getAllFiles(testJourneyId).futureValue.size shouldBe 2

                val result = post(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)(Map(UploadRemoveFileForm.key -> "true"))

                result.status shouldBe SEE_OTHER
                result.header("Location") shouldBe Some(routes.UpscanCheckAnswersController.onPageLoad().url)

                fileUploadRepo.getAllFiles(testJourneyId).futureValue.size shouldBe 1
              }
            }
          }

          "the user selects 'No' to keep the file" should {

            "redirect to the Upscan Check Answers page, WITHOUT removing the file" in {
              stubAuth(OK, authResponse)
              fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
              fileUploadRepo.getAllFiles(testJourneyId).futureValue.size shouldBe 1

              val result = post(s"/upload-supporting-evidence/remove-file?fileReference=$fileRef1&index=1", isAgent = isAgent)(Map(UploadRemoveFileForm.key -> "false"))

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(routes.UpscanCheckAnswersController.onPageLoad().url)

              fileUploadRepo.getAllFiles(testJourneyId).futureValue.size shouldBe 1
            }
          }
        }
      }
    }
  }
}
