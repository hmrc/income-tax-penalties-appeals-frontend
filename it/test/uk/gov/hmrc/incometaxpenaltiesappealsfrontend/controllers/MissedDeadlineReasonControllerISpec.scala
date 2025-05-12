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

import fixtures.messages.MissedDeadlineReasonMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.MissedDeadlineReasonForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{MissedDeadlineReasonPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class MissedDeadlineReasonControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]


  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  val userAnswersWithReasonLSP: UserAnswers =
    emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Other)

  val userAnswersWithReasonLPP: UserAnswers =
    emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, Other)

  val isLPPWithUrls: List[(Boolean, String, UserAnswers)] = List(
    (true, "/why-was-the-payment-late", userAnswersWithReasonLPP),
    (false, "/why-was-the-submission-late", userAnswersWithReasonLSP)
  )


  for (isLpp <- isLPPWithUrls) {
    s"GET ${isLpp._2} with isLPP = ${isLpp._1}" should {

      testNavBar(url = isLpp._2) {
        userAnswersRepo.upsertUserAnswer(isLpp._3).futureValue
      }

      "return an OK with a view pre-populated" when {
        "the user is an authorised individual AND the page has already been answered" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(
            isLpp._3.setAnswer(MissedDeadlineReasonPage, "Some reason")
          ).futureValue

          val result = get(isLpp._2)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.select(s"#${MissedDeadlineReasonForm.key}").text() shouldBe "Some reason"
        }

        "the user is an authorised agent AND the page has already been answered" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(
            isLpp._3.setAnswer(MissedDeadlineReasonPage, "Some reason")
          ).futureValue

          val result = get(isLpp._2, isAgent = true)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.select(s"#${MissedDeadlineReasonForm.key}").text() shouldBe "Some reason"
        }
      }

      val caption: String = if (isLpp._1) {
        MissedDeadlineReasonMessages.English.lppCaption(
          dateToString(latePaymentAppealData.startDate),
          dateToString(latePaymentAppealData.endDate)
        )
      } else {
        MissedDeadlineReasonMessages.English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
      }

      "the page has the correct elements for first stage appeals" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(isLpp._3).futureValue

          val result = get(isLpp._2)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = isLpp._1)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe caption

          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = isLpp._1)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintText(isLPP = isLpp._1)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(isLpp._3).futureValue

          val result = get(isLpp._2, isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = isLpp._1)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe caption

          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = isLpp._1)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintText(isLPP = isLpp._1)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }


//LPP (multiple)
      "the page has the correct elements for first stage multiple payment penalties" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue

          val result = get("/why-was-the-payment-late")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = true)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe MissedDeadlineReasonMessages.English.lppCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = true)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintText(isLPP = true)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue

          val result = get("/why-was-the-payment-late", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = true)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe MissedDeadlineReasonMessages.English.lppCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = true)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintText(isLPP = true)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
//LPP Review/Second stage (single)
      "the page has the correct elements for second stage appeal payment penalty (single)" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP2ndStage).futureValue

          val result = get("/why-was-the-payment-late")

          val document = Jsoup.parse(result.body)
          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe MissedDeadlineReasonMessages.English.lppCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintTextSecondStage(isLPP = true)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP2ndStage).futureValue

          val result = get("/why-was-the-payment-late", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe MissedDeadlineReasonMessages.English.lppCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintTextSecondStage(isLPP = true)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }

// LPP Review/Second stage (multiple)
      "the page has the correct elements for second stage appeal payment penalty (multiple)" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

          val result = get("/why-was-the-payment-late")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe MissedDeadlineReasonMessages.English.lppCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintTextSecondStage(isLPP = true)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

          val result = get("/why-was-the-payment-late", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)} - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe MissedDeadlineReasonMessages.English.lppCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitleSecondStage(isLPP = true)
          document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintTextSecondStage(isLPP = true)
          document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }
  }

  for (isLpp <- isLPPWithUrls) {
    s"POST ${isLpp._2} with isLPP = ${isLpp._1}" when {

    val userAnswersWithReason = emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Other)

    "the text area content is valid" should {

      "save the value to UserAnswers AND redirect to the Extra Evidence page" in {

        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

        val result = post(isLpp._2)(Map(MissedDeadlineReasonForm.key -> "Some reason"))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.ExtraEvidenceController.onPageLoad().url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(MissedDeadlineReasonPage)) shouldBe Some("Some reason")
      }
    }

    "the text area content is invalid" should {

      "render a bad request in first stage appeal with the Form Error on the page with a link to the field in error" in {

        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

        val result = post(isLpp._2)(Map(MissedDeadlineReasonForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(MissedDeadlineReasonMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe MissedDeadlineReasonMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe MissedDeadlineReasonMessages.English.errorRequired(isLPP = isLpp._1)
        error1Link.attr("href") shouldBe s"#${MissedDeadlineReasonForm.key}"
      }

      "render a bad request in second stage appeal with the Form Error on the page with a link to the field in error" in {

        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

        val result = post(isLpp._2)(Map(MissedDeadlineReasonForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(MissedDeadlineReasonMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe MissedDeadlineReasonMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe MissedDeadlineReasonMessages.English.errorRequiredSecondStage
        error1Link.attr("href") shouldBe s"#${MissedDeadlineReasonForm.key}"
        }
      }
    }
  }
}
