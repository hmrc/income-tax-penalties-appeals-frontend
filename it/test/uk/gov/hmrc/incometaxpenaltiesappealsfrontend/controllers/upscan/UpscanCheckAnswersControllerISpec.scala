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
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.is2ndStageAppeal
import fixtures.messages.upscan.NonJsUploadCheckAnswersMessages
import fixtures.views.BaseSelectors
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.{ControllerISpecHelper, routes => appealsRoutes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadAnotherFileForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CheckMode, Mode, NormalMode, PenaltyData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.{FileUploadJourneyRepository, UserAnswersRepository}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class UpscanCheckAnswersControllerISpec extends ControllerISpecHelper
  with FileUploadFixtures {

  private lazy val configApp: Application =
    new GuiceApplicationBuilder().build()

  override lazy val appConfig: AppConfig = configApp.injector.instanceOf[AppConfig]

  lazy val timeMachine: TimeMachine = new TimeMachine(appConfig) {
    override def getCurrentDateTime: LocalDateTime = testDateTime
  }

  override lazy val app: Application = appWithOverrides(
    inject.bind[TimeMachine].toInstance(timeMachine)
  )


  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]
  lazy val fileUploadRepo: FileUploadJourneyRepository = app.injector.instanceOf[FileUploadJourneyRepository]

  val testDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

  class Setup(isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    fileUploadRepo.collection.deleteMany(Document()).toFuture().futureValue

    val userAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, Other)

    userAnswersRepo.upsertUserAnswer(userAnswers).futureValue
  }

  def url(isAgent: Boolean, mode: Mode): String = {
    val urlPathStart = if (isAgent) "/upload-evidence/agent-upload-another-file" else "/upload-evidence/upload-another-file"
    urlPathStart + {if(mode == CheckMode) "/check" else ""}
  }

  List(NormalMode, CheckMode).foreach { mode =>

    Seq(
      false,
      true
    ).foreach { case isAgent =>
      s"when authenticating as an ${if (isAgent) "agent" else "individual"} in $mode" when {

        if (!isAgent) {
          testNavBar(url(isAgent, mode)) {
            fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
          }
        }

        s"GET ${url(isAgent, mode)}" when {

          s"the number of files uploaded is < ${appConfig.upscanMaxNumberOfFiles}" should {

            "render the File Upload check answers page with a form action to add another file" in new Setup() {
              stubAuthRequests(isAgent)
              fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

              val result = get(url(isAgent, mode), isAgent = isAgent)
              result.status shouldBe OK

              val document = Jsoup.parse(result.body)
              document.select("form").attr("action") shouldBe routes.UpscanCheckAnswersController.onSubmit(isAgent, is2ndStageAppeal, mode).url
              document.select(BaseSelectors.legend).text() shouldBe NonJsUploadCheckAnswersMessages.English.uploadAnotherFileLegend
              document.select(BaseSelectors.radio(1)).text() shouldBe NonJsUploadCheckAnswersMessages.English.yes
              document.select(BaseSelectors.radio(2)).text() shouldBe NonJsUploadCheckAnswersMessages.English.no
            }
          }

          s"the number of files uploaded is == ${appConfig.upscanMaxNumberOfFiles}" should {

            "render the File Upload check answers page with a form action but without the 'Add another file' question" in new Setup() {
              stubAuthRequests(isAgent)

              (1 to appConfig.upscanMaxNumberOfFiles).foreach { i =>
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel.copy(reference = s"ref$i")).futureValue
              }

              val result = get(url(isAgent, mode), isAgent = isAgent)
              result.status shouldBe OK

              val document = Jsoup.parse(result.body)
              document.select("form").attr("action") shouldBe routes.UpscanCheckAnswersController.onSubmit(isAgent, is2ndStageAppeal, mode).url
              document.select(BaseSelectors.legend).isEmpty shouldBe true
              document.select(BaseSelectors.radio(1)).isEmpty shouldBe true
              document.select(BaseSelectors.radio(2)).isEmpty shouldBe true
            }
          }
        }

        s"POST ${url(isAgent, mode)}" when {

          s"number of files which has been uploaded is < ${appConfig.upscanMaxNumberOfFiles}" when {

            "the User selects 'Yes' to upload another file" should {

              "redirect to the UpscanInitiate page" in new Setup() {

                stubAuthRequests(isAgent)
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

                val result = post(url(isAgent, mode), isAgent = isAgent)(
                  Map(UploadAnotherFileForm.key -> "true")
                )

                result.status shouldBe SEE_OTHER
                result.header("Location") shouldBe Some(routes.UpscanInitiateController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode).url)
              }
            }

            "the User selects 'No' to NOT upload another file" when {

              "the appeal is late" should {

                if (mode == NormalMode) {
                  "redirect to Late Appeal page" in new Setup(isLate = true) {

                    stubAuthRequests(isAgent)
                    fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

                    val result = post(url(isAgent, mode), isAgent = isAgent)(
                      Map(UploadAnotherFileForm.key -> "false")
                    )

                    result.status shouldBe SEE_OTHER
                    result.header("Location") shouldBe Some(appealsRoutes.LateAppealController.onPageLoad(isAgent, is2ndStageAppeal).url)
                  }
                } else {
                  "redirect to Check Answers page" in new Setup(isLate = true) {

                    stubAuthRequests(isAgent)
                    fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

                    val result = post(url(isAgent, mode), isAgent = isAgent)(
                      Map(UploadAnotherFileForm.key -> "false")
                    )

                    result.status shouldBe SEE_OTHER
                    result.header("Location") shouldBe Some(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent).url)
                  }
                }
              }

              "the appeal is NOT late" should {

                "redirect to Check Answers page" in new Setup() {

                  stubAuthRequests(isAgent)
                  fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue

                  val result = post(url(isAgent, mode), isAgent = isAgent)(
                    Map(UploadAnotherFileForm.key -> "false")
                  )

                  result.status shouldBe SEE_OTHER
                  result.header("Location") shouldBe Some(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent).url)
                }
              }
            }
          }

          s"number of files which has been uploaded is == ${appConfig.upscanMaxNumberOfFiles}" when {

            "the appeal is late" should {

              if(mode == NormalMode) {
                "redirect to Late Appeal page" in new Setup(isLate = true) {

                  stubAuthRequests(isAgent)
                  (1 to appConfig.upscanMaxNumberOfFiles).foreach { i =>
                    fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel.copy(reference = s"ref$i")).futureValue
                  }

                  val result = post(url(isAgent, mode), isAgent = isAgent)(Map.empty[String, String])

                  result.status shouldBe SEE_OTHER
                  result.header("Location") shouldBe Some(appealsRoutes.LateAppealController.onPageLoad(isAgent, is2ndStageAppeal).url)
                }
              } else {
                "redirect to Check Answers page" in new Setup(isLate = true) {

                  stubAuthRequests(isAgent)
                  (1 to appConfig.upscanMaxNumberOfFiles).foreach { i =>
                    fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel.copy(reference = s"ref$i")).futureValue
                  }

                  val result = post(url(isAgent, mode), isAgent = isAgent)(Map.empty[String, String])

                  result.status shouldBe SEE_OTHER
                  result.header("Location") shouldBe Some(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent).url)
                }
              }
            }

            "the appeal is NOT late" should {

              "redirect to Check Answers page" in new Setup() {

                stubAuthRequests(isAgent)
                (1 to appConfig.upscanMaxNumberOfFiles).foreach { i =>
                  fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel.copy(reference = s"ref$i")).futureValue
                }

                val result = post(url(isAgent, mode), isAgent = isAgent)(Map.empty[String, String])

                result.status shouldBe SEE_OTHER
                result.header("Location") shouldBe Some(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent).url)
              }
            }
          }
        }
      }
    }
  }
}
