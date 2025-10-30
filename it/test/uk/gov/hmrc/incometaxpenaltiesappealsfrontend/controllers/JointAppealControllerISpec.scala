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

import fixtures.messages.JointAppealMessages
import org.jsoup.select.Elements
import org.jsoup.{Jsoup, nodes}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.JointAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.JointAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class JointAppealControllerISpec extends ControllerISpecHelper {


  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  class Setup {
    deleteAll(userAnswersRepo)
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue
  }

  "GET /multiple-penalties-for-this-period" should {

    testNavBar(url = "/multiple-penalties-for-this-period")(
      userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue
    )

    "return an OK with a view" when {
      "the user is an authorised individual AND the page has already been answered" in new Setup() {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs.setAnswer(JointAppealPage, true)).futureValue

        val result: WSResponse = get("/multiple-penalties-for-this-period")
        result.status shouldBe OK

        val document: nodes.Document = Jsoup.parse(result.body)
        document.select(s"#${JointAppealForm.key}").hasAttr("checked") shouldBe true
        document.select(s"#${JointAppealForm.key}-2").hasAttr("checked") shouldBe false
      }

      "the user is an authorised agent AND page NOT already answered" in new Setup() {
        stubAuthRequests(true)

        val result: WSResponse = get("/agent-multiple-penalties-for-this-period", isAgent = true)
        result.status shouldBe OK

        val document: nodes.Document = Jsoup.parse(result.body)
        document.select(s"#${JointAppealForm.key}").hasAttr("checked") shouldBe false
        document.select(s"#${JointAppealForm.key}-2").hasAttr("checked") shouldBe false
      }
    }

    "the journey is for a 1st Stage Appeal" when {
      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup() {
          stubAuthRequests(false)
          val result: WSResponse = get("/multiple-penalties-for-this-period")

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "There are 2 penalties for this overdue tax charge - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaptionMultiple(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
          document.getElementById("paragraph1").text() shouldBe "These are:"
          document.select("#penaltiesList > li:nth-child(1)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.firstPenaltyAmount)} first late payment penalty"
          document.select("#penaltiesList > li:nth-child(2)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.secondPenaltyAmount)} second late payment penalty"
          document.getElementById("paragraph2").text() shouldBe "You can appeal both penalties at the same time if the reason why you did not make the tax payment is the same for each penalty."
          document.getElementsByClass("govuk-fieldset__legend").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup() {
          stubAuthRequests(true)
          val result: WSResponse = get("/agent-multiple-penalties-for-this-period", isAgent = true)

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "There are 2 penalties for this overdue tax charge - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaptionMultiple(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
          document.getElementById("paragraph1").text() shouldBe "These are:"
          document.select("#penaltiesList > li:nth-child(1)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.firstPenaltyAmount)} first late payment penalty"
          document.select("#penaltiesList > li:nth-child(2)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.secondPenaltyAmount)} second late payment penalty"
          document.getElementById("paragraph2").text() shouldBe "You can appeal both penalties at the same time if the reason why your client did not make the tax payment is the same for each penalty."
          document.getElementsByClass("govuk-fieldset__legend").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }

    "the journey is for a 2nd Stage Appeal" when {
      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup() {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage)

          val result: WSResponse = get("/multiple-penalties-for-this-period")

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "There are 2 penalties for this overdue tax charge - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaptionMultiple(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
          document.getElementById("paragraph1").text() shouldBe "These are:"
          document.select("#penaltiesList > li:nth-child(1)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.firstPenaltyAmount)} first late payment penalty"
          document.select("#penaltiesList > li:nth-child(2)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.secondPenaltyAmount)} second late payment penalty"
          document.getElementById("paragraph2").text() shouldBe "You can ask for these appeal decisions to be reviewed at the same time if your evidence applies to both of the original appeals."
          document.getElementsByClass("govuk-fieldset__legend").text() shouldBe "Do you want both appeal decisions to be reviewed at the same time?"
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup() {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage)

          val result: WSResponse = get("/agent-multiple-penalties-for-this-period", isAgent = true)

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "There are 2 penalties for this overdue tax charge - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaptionMultiple(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
          document.getElementById("paragraph1").text() shouldBe "These are:"
          document.select("#penaltiesList > li:nth-child(1)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.firstPenaltyAmount)} first late payment penalty"
          document.select("#penaltiesList > li:nth-child(2)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.secondPenaltyAmount)} second late payment penalty"
          document.getElementById("paragraph2").text() shouldBe "You can ask for these appeal decisions to be reviewed at the same time if your client’s evidence applies to both of the original appeals."
          document.getElementsByClass("govuk-fieldset__legend").text() shouldBe "Do you want both appeal decisions to be reviewed at the same time?"
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }
  }

  "GET /multiple-penalties-for-this-period/check" should {

    testNavBar(url = "/multiple-penalties-for-this-period/check")(
      userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue
    )

    "the journey is in CheckMode" when {
      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup() {
          stubAuthRequests(false)
          val result: WSResponse = get("/multiple-penalties-for-this-period/check")

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "There are 2 penalties for this overdue tax charge - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaptionMultiple(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
          document.getElementById("paragraph1").text() shouldBe "These are:"
          document.select("#penaltiesList > li:nth-child(1)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.firstPenaltyAmount)} first late payment penalty"
          document.select("#penaltiesList > li:nth-child(2)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.secondPenaltyAmount)} second late payment penalty"
          document.getElementById("paragraph2").text() shouldBe "You can appeal both penalties at the same time if the reason why you did not make the tax payment is the same for each penalty."
          document.getElementsByClass("govuk-fieldset__legend").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup() {
          stubAuthRequests(true)
          val result: WSResponse = get("/agent-multiple-penalties-for-this-period/check", isAgent = true)

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "There are 2 penalties for this overdue tax charge - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaptionMultiple(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
          document.getElementById("paragraph1").text() shouldBe "These are:"
          document.select("#penaltiesList > li:nth-child(1)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.firstPenaltyAmount)} first late payment penalty"
          document.select("#penaltiesList > li:nth-child(2)").text() shouldBe s"£${CurrencyFormatter.uiFormat(multiplePenaltiesModel.secondPenaltyAmount)} second late payment penalty"
          document.getElementById("paragraph2").text() shouldBe "You can appeal both penalties at the same time if the reason why your client did not make the tax payment is the same for each penalty."
          document.getElementsByClass("govuk-fieldset__legend").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
          document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }
  }

  "POST /multiple-penalties-for-this-period" when {

    "the radio option posted is valid" should {

      "save the value to UserAnswers AND redirect to the MultipleAppeals page if the answer is 'Yes'" in new Setup() {

        stubAuthRequests(false)

        val result: WSResponse = post("/multiple-penalties-for-this-period")(Map(JointAppealForm.key -> true))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(controllers.routes.MultipleAppealsController.onPageLoad(isAgent = false, is2ndStageAppeal = false).url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(JointAppealPage)) shouldBe Some(true)
      }

      "save the value to UserAnswers AND redirect to the SingleAppeal page if the answer is 'No'" in new Setup() {

        stubAuthRequests(false)
        val result: WSResponse = post("/multiple-penalties-for-this-period")(Map(JointAppealForm.key -> false))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(controllers.routes.SingleAppealConfirmationController.onPageLoad(isAgent = false, is2ndStageAppeal = false).url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(JointAppealPage)) shouldBe Some(false)
      }
    }
  }

  "POST /multiple-penalties-for-this-period/check" when {

    "the radio option posted is valid" should {

      "save the value to UserAnswers AND redirect to the CheckYourAnswers page if the mode is CheckMode and the answer is 'Yes'" in new Setup() {

        stubAuthRequests(false)

        val result: WSResponse = post("/multiple-penalties-for-this-period/check")(Map(JointAppealForm.key -> true))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(controllers.routes.CheckYourAnswersController.onPageLoad(isAgent = false).url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(JointAppealPage)) shouldBe Some(true)
      }

      "save the value to UserAnswers AND redirect to the CheckYourAnswers page if the mode is CheckMode and the answer is 'No'" in new Setup() {

        stubAuthRequests(false)
        val result: WSResponse = post("/multiple-penalties-for-this-period/check")(Map(JointAppealForm.key -> false))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(controllers.routes.CheckYourAnswersController.onPageLoad(isAgent = false).url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(JointAppealPage)) shouldBe Some(false)
      }
    }
  }

    "the radio option is invalid" should {
      "the journey is for a 1st Stage Appeal" when {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

          stubAuthRequests(false)

          val result: WSResponse = post("/multiple-penalties-for-this-period")(Map(JointAppealForm.key -> ""))
          result.status shouldBe BAD_REQUEST

          val document: nodes.Document = Jsoup.parse(result.body)
          document.title() should include(JointAppealMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe JointAppealMessages.English.thereIsAProblem

          val error1Link: Elements = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe JointAppealMessages.English.errorRequired
          error1Link.attr("href") shouldBe s"#${JointAppealForm.key}"
        }
      }
    }

    "the radio option is invalid" should {
      "the journey is for a 2nd Stage Appeal" when {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage)

          val result: WSResponse = post("/multiple-penalties-for-this-period")(Map(JointAppealForm.key -> ""))
          result.status shouldBe BAD_REQUEST

          val document: nodes.Document = Jsoup.parse(result.body)
          document.title() should include(JointAppealMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe JointAppealMessages.English.thereIsAProblem

          val error1Link: Elements = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe JointAppealMessages.English.errorRequiredReview
          error1Link.attr("href") shouldBe s"#${JointAppealForm.key}"
        }
      }
    }
  }
