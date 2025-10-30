/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.{is2ndStageAppeal, isAgent}
import fixtures.messages.{English, ReasonableExcuseMessages}
import fixtures.views.BaseSelectors
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.FireOrFlood
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{HonestyDeclarationPage, ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.PenaltiesStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

import java.time.LocalDate

class CheckYourAnswersControllerISpec extends ControllerISpecHelper with PenaltiesStub {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  
  object Selectors extends BaseSelectors {
    override val prefix: String = "main"
  }

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    super.beforeEach()
  }

  for (reason <- ReasonableExcuse.allReasonableExcuses) {

    val userAnswersWithReason = emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, reason)

    s"GET /check-your-answers with reasonableExcuse='$reason'" should {

      testNavBar(url = "/check-your-answers")(
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
      )

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/check-your-answers")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/agent-check-your-answers", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/check-your-answers")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Check your answers - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "Check your answers"
          document.select(Selectors.h2(1)).text() shouldBe "Appeal details"
          document.select(Selectors.summaryRowKey(1)).text() shouldBe ReasonableExcuseMessages.English.cyaKey
          document.select(Selectors.summaryRowValue(1)).text() shouldBe ReasonableExcuseMessages.English.cyaValue(reason)
          document.select(Selectors.summaryRowAction(1,1)).text() shouldBe ReasonableExcuseMessages.English.change + " " + ReasonableExcuseMessages.English.cyaHidden
          document.select(Selectors.h2(2)).text() shouldBe "Declaration"
          document.select(Selectors.warning).text() shouldBe "Warning By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. A false declaration can result in prosecution."
          document.getSubmitButton.text() shouldBe "Accept and send"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/agent-check-your-answers", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Check your answers - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "Check your answers"
          document.select(Selectors.h2(1)).text() shouldBe "Appeal details"
          document.select(Selectors.summaryRowKey(1)).text() shouldBe ReasonableExcuseMessages.English.cyaKey
          document.select(Selectors.summaryRowValue(1)).text() shouldBe ReasonableExcuseMessages.English.cyaValue(reason)
          document.select(Selectors.summaryRowAction(1,1)).text() shouldBe ReasonableExcuseMessages.English.change + " " + ReasonableExcuseMessages.English.cyaHidden
          document.select(Selectors.h2(2)).text() shouldBe "Declaration"
          document.select(Selectors.warning).text() shouldBe "Warning By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. A false declaration can result in prosecution."
          document.getSubmitButton.text() shouldBe "Accept and send"
        }
      }
    }
  }

  "POST /check-your-answers" when {
    "the user is an authorised individual" when {
      "the Appeals Submission model can be constructed successfully" when {

        lazy val userAnswers =
          emptyUserAnswersWithLSP
            .setAnswer(ReasonableExcusePage, FireOrFlood)
            .setAnswer(HonestyDeclarationPage, true)
            .setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 1, 1))

        "a successful response is returned from the downstream service" should {
          "redirect to the confirmation page" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

            successfulAppealSubmission(testNino, isLPP = false, penaltyDataLSP.penaltyNumber)

            val result = post("/check-your-answers")(Json.obj())

            result.status shouldBe SEE_OTHER
            result.header(LOCATION) shouldBe Some(routes.ConfirmationController.onPageLoad(isAgent, is2ndStageAppeal).url)
          }
        }

        "an Error response is returned from the downstream service" should {
          "render an ISE" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

            failedAppealSubmission(testNino, isLPP = false, penaltyDataLSP.penaltyNumber)

            val result = post("/check-your-answers")(Json.obj())

            result.status shouldBe INTERNAL_SERVER_ERROR
            result.body should include("Sorry, there is a problem with the service")
          }
        }
      }

      "the Appeals Submission model can NOT be constructed successfully" when {

        lazy val userAnswers =
          emptyUserAnswersWithLSP
            .setAnswer(HonestyDeclarationPage, true)
            .setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 1, 1))

        "render an ISE" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/check-your-answers")(Json.obj())

          result.status shouldBe INTERNAL_SERVER_ERROR
          result.body should include("Sorry, there is a problem with the service")
        }
      }
    }
  }
}
