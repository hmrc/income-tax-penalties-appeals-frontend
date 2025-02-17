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

import fixtures.messages.WhenDidEventEndMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status._
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventEndForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventEndPage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, IncomeTaxSessionKeys, NavBarTesterHelper, TimeMachine, ViewSpecHelper}

import java.time.LocalDate

class WhenDidEventEndControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  val testStartDate: LocalDate = LocalDate.of(2024, 3, 2)

  class Setup(isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val userAnswer: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, "technicalIssues")
      .setAnswer(WhenDidEventHappenPage, testStartDate)

    userAnswersRepo.upsertUserAnswer(userAnswer).futureValue
  }

  "GET /when-did-the-event-end" should {

    testNavBar(url = "/when-did-the-event-end")(
      userAnswersRepo.upsertUserAnswer(emptyUerAnswersWithLSP
        .setAnswer(ReasonableExcusePage, "technicalIssues")
        .setAnswer(WhenDidEventHappenPage, testStartDate)
      ).futureValue
    )

    "return an OK with a view" when {

      "the user is an authorised individual AND the page has already been answered" in new Setup() {
        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(userAnswer.setAnswer(WhenDidEventEndPage, LocalDate.of(2024, 4, 2))).futureValue
        val result = get("/when-did-the-event-end")

        result.status shouldBe OK
        val document = Jsoup.parse(result.body)
        document.getElementById(s"${WhenDidEventEndForm.key + ".day"}").`val`() shouldBe "2"
        document.getElementById(s"${WhenDidEventEndForm.key + ".month"}").`val`() shouldBe "4"
        document.getElementById(s"${WhenDidEventEndForm.key + ".year"}").`val`() shouldBe "2024"
      }

      "the user is an authorised agent AND page NOT already answered" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/when-did-the-event-end", isAgent = true)

        result.status shouldBe OK
        val document = Jsoup.parse(result.body)
        document.getElementById(s"${WhenDidEventEndForm.key + ".day"}").`val`() shouldBe ""
        document.getElementById(s"${WhenDidEventEndForm.key + ".month"}").`val`() shouldBe ""
        document.getElementById(s"${WhenDidEventEndForm.key + ".year"}").`val`() shouldBe ""
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in new Setup() {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/when-did-the-event-end")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "When did the software or technology issues end? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe WhenDidEventEndMessages.English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "When did the software or technology issues end?"
        document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
        document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
        document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
        document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/when-did-the-event-end", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "When did the software or technology issues end? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe WhenDidEventEndMessages.English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "When did the software or technology issues end?"
        document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
        document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
        document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
        document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
        document.getSubmitButton.text() shouldBe "Continue"
      }
    }
  }

  "POST /when-did-the-event-end" when {

    "the date is valid" should {
      "save the value to UserAnswers AND redirect" when {
        "the appeal is late" should {
          "redirect to the LateAppeal page" in new Setup(isLate = true) {
            stubAuth(OK, successfulIndividualAuthResponse)

            val result = post("/when-did-the-event-end")(Map(
              WhenDidEventEndForm.key + ".day" -> "02",
              WhenDidEventEndForm.key + ".month" -> "04",
              WhenDidEventEndForm.key + ".year" -> "2024"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad().url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhenDidEventEndPage)) shouldBe Some(LocalDate.of(2024, 4, 2))
          }
        }

        "the appeal is NOT late" should {
          "redirect to the CheckAnswers page" in new Setup() {
            stubAuth(OK, successfulIndividualAuthResponse)

            val result = post("/when-did-the-event-end")(Map(
              WhenDidEventEndForm.key + ".day" -> "02",
              WhenDidEventEndForm.key + ".month" -> "04",
              WhenDidEventEndForm.key + ".year" -> "2024"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad().url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhenDidEventEndPage)) shouldBe Some(LocalDate.of(2024, 4, 2))
          }
        }
      }
    }

    "the date is not valid - day missing" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "",
          WhenDidEventEndForm.key + ".month" -> "04",
          WhenDidEventEndForm.key + ".year" -> "2024"))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required", "day")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".day"}"
      }
    }

    "the date is not valid - month missing" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "01",
          WhenDidEventEndForm.key + ".month" -> "",
          WhenDidEventEndForm.key + ".year" -> "2024"))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required", "month")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".month"}"
      }
    }

    "the date is not valid - year missing" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "01",
          WhenDidEventEndForm.key + ".month" -> "04",
          WhenDidEventEndForm.key + ".year" -> ""))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required", "year")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".year"}"
      }
    }

    "the date is not valid - two fields missing - day and month" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "",
          WhenDidEventEndForm.key + ".month" -> "",
          WhenDidEventEndForm.key + ".year" -> "2024"))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required.two", "day", "month")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".day"}"
      }
    }

    "the date is not valid - two fields missing - day and year" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "",
          WhenDidEventEndForm.key + ".month" -> "04",
          WhenDidEventEndForm.key + ".year" -> ""))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required.two", "day", "year")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".day"}"
      }
    }

    "the date is not valid - two fields missing - month and year" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "01",
          WhenDidEventEndForm.key + ".month" -> "",
          WhenDidEventEndForm.key + ".year" -> ""))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required.two", "month", "year")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".month"}"
      }
    }

    "the date is not valid - all fields missing" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "",
          WhenDidEventEndForm.key + ".month" -> "",
          WhenDidEventEndForm.key + ".year" -> ""))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("required.all")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".day"}"
      }
    }

    "the date is not valid - date is in the future" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "02",
          WhenDidEventEndForm.key + ".month" -> "04",
          WhenDidEventEndForm.key + ".year" -> "2027"))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("notInFuture")
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".day"}"
      }
    }

    "the date is not valid - end date is before start date" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/when-did-the-event-end")(Map(
          WhenDidEventEndForm.key + ".day" -> "02",
          WhenDidEventEndForm.key + ".month" -> "02",
          WhenDidEventEndForm.key + ".year" -> "2024"))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        val month = testStartDate.getMonthValue match {
          case 1 => "January"
          case 2 => "February"
          case 3 => "March"
          case 4 => "April"
          case 5 => "May"
          case 6 => "June"
          case 7 => "July"
          case 8 => "August"
          case 9 => "September"
          case 10 => "October"
          case 11 => "November"
          case 12 => "December"
        }

        val formattedDate: String = s"${testStartDate.getDayOfMonth} $month ${testStartDate.getYear}"

        document.title() should include(WhenDidEventEndMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventEndMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhenDidEventEndMessages.English.errorMessageConstructor("endDateLessThanStartDate", formattedDate)
        error1Link.attr("href") shouldBe s"#${WhenDidEventEndForm.key + ".day"}"
      }
    }
  }

}
