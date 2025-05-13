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

import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Bereavement
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository

class ConfirmationControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Bereavement)).futureValue
    super.beforeEach()
  }

  "GET /appeal-confirmation" should {

    testNavBar(url = "/check-your-answers")()

    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        val result = get("/appeal-confirmation")

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        val result = get("/agent-appeal-confirmation", isAgent = true)

        result.status shouldBe OK
      }
    }
//LSP
    "the page has the correct elements for first stage appeals" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

        val result = get("/appeal-confirmation")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Appeal received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Appeal received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late submission penalty point: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "Your appeal has been logged against your National Insurance number. Please quote this number if you call HMRC about this appeal."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print your appeal details (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to make decisions about appeals within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, you’ll be notified about the outcome of this appeal and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

        val result = get("/agent-appeal-confirmation", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Appeal received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Appeal received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late submission penalty point: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This appeal has been logged against your client’s National Insurance number. Please quote this number if you call HMRC about this appeal."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print your appeal details (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to make decisions about appeals within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, your client will be notified about the outcome of this appeal and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your client’s Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your client’s Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }
    }
//LPP
    "the page has the correct elements for first stage late payment penalty" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP).futureValue

        val result = get("/appeal-confirmation")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Appeal received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Appeal received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "Your appeal has been logged against your National Insurance number. Please quote this number if you call HMRC about this appeal."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print your appeal details (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to make decisions about appeals within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, you’ll be notified about the outcome of this appeal and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP).futureValue

        val result = get("/agent-appeal-confirmation", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Appeal received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Appeal received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This appeal has been logged against your client’s National Insurance number. Please quote this number if you call HMRC about this appeal."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print your appeal details (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to make decisions about appeals within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, your client will be notified about the outcome of this appeal and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your client’s Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your client’s Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }
    }
//LPP (multiple)
    "the page has the correct elements for first multiple payment penalties" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue

        val result = get("/appeal-confirmation")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Appeal received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Appeal received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "Your appeal has been logged against your National Insurance number. Please quote this number if you call HMRC about this appeal."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print your appeal details (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to make decisions about appeals within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, you’ll be notified about the outcome of this appeal and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue

        val result = get("/agent-appeal-confirmation", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Appeal received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Appeal received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This appeal has been logged against your client’s National Insurance number. Please quote this number if you call HMRC about this appeal."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print your appeal details (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to make decisions about appeals within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, your client will be notified about the outcome of this appeal and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your client’s Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your client’s Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }
    }
///LPP Review/Second stage (single)
    "the page has the correct elements for second stage appeal payment penalty (single)" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP2ndStage).futureValue

        val result = get("/appeal-confirmation")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Review request received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Review request received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This request for a review has been logged against your National Insurance number. Please quote this number if you call HMRC about this review."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print details of this review (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to complete reviews within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, you’ll be notified about the outcome of the review and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP2ndStage).futureValue

        val result = get("/agent-appeal-confirmation", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Review request received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Review request received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This request for a review has been logged against your client’s National Insurance number. Please quote this number if you call HMRC about this review."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print details of this review (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to complete reviews within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, your client will be notified about the outcome of the review and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your client’s Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your client’s Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }
    }
// LPP Review/Second stage (multiple)
    "the page has the correct elements for second stage appeal payment penalty (multiple)" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

        val result = get("/appeal-confirmation")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Review request received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Review request received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This request for a review has been logged against your National Insurance number. Please quote this number if you call HMRC about this review."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print details of this review (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to complete reviews within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, you’ll be notified about the outcome of the review and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

        val result = get("/agent-appeal-confirmation", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Review request received - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Review request received"
        document.getElementsByClass("govuk-panel__body").text() shouldBe "Late payment penalty: 1 January 2024 to 31 January 2024"
        document.getElementById("confirmationFistParagraph").text() shouldBe "You do not need a reference number."
        document.getElementById("confirmationSecondParagraph").text() shouldBe "This request for a review has been logged against your client’s National Insurance number. Please quote this number if you call HMRC about this review."
        document.getElementById("viewOrPrint-link").text() shouldBe "View or print details of this review (opens in new tab)"
        document.getElementById("whatHappensNext").text() shouldBe "What happens next"
        document.getElementById("confirmationThirdParagraph").text() shouldBe "We aim to complete reviews within 45 days."
        document.getElementById("confirmationFourthParagraph").text() shouldBe "When a decision has been made, your client will be notified about the outcome of the review and the reasons for our decision."
        document.getElementById("returnToSA-link").text() shouldBe "Return to your client’s Self Assessment penalties"
        document.getElementById("viewSA-link").text() shouldBe "View your client’s Self Assessment account"
        document.getElementById("beforeYouGo").text() shouldBe "Before you go"
        document.getElementById("confirmationFifthParagraph").text() shouldBe "Your feedback helps us make our service better."
        document.getElementById("confirmationSixthParagraph").text() shouldBe "Take a short survey to share your feedback on this service."
      }
    }
  }
}
