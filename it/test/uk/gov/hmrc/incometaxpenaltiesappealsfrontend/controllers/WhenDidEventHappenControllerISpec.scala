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

import fixtures.messages.WhenDidEventHappenMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventHappenForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Bereavement, Crime, Other, TechnicalIssues}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{PenaltyData, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._

import java.time.LocalDate

class WhenDidEventHappenControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  class Setup(reason: ReasonableExcuse, isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val lateDays: Int = if(reason == Bereavement) appConfig.bereavementLateDays else appConfig.lateDays

    val userAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, reason)

    userAnswersRepo.upsertUserAnswer(userAnswers).futureValue
  }

  for (reason <- ReasonableExcuse.allReasonableExcuses) {

    s"GET /when-did-the-event-happen with $reason" should {

      testNavBar(url = "/honesty-declaration") {
        userAnswersRepo.upsertUserAnswer(emptyUerAnswersWithLSP.setAnswer(ReasonableExcusePage, reason)).futureValue
      }

      "return an OK with a view" when {
        "the user is an authorised individual AND the page has already been answered" in new Setup(reason) {
          stubAuth(OK, successfulIndividualAuthResponse)

          userAnswersRepo.upsertUserAnswer(userAnswers.setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 4, 2))).futureValue

          val result = get("/when-did-the-event-happen")

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)
          document.getElementById(s"${WhenDidEventHappenForm.key + ".day"}").`val`() shouldBe "2"
          document.getElementById(s"${WhenDidEventHappenForm.key + ".month"}").`val`() shouldBe "4"
          document.getElementById(s"${WhenDidEventHappenForm.key + ".year"}").`val`() shouldBe "2024"
        }

        "the user is an authorised agent AND page NOT already answered" in new Setup(reason) {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = get("/when-did-the-event-happen", isAgent = true)

          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getElementById(s"${WhenDidEventHappenForm.key + ".day"}").`val`() shouldBe ""
          document.getElementById(s"${WhenDidEventHappenForm.key + ".month"}").`val`() shouldBe ""
          document.getElementById(s"${WhenDidEventHappenForm.key + ".year"}").`val`() shouldBe ""
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup(reason) {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = get("/when-did-the-event-happen")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe WhenDidEventHappenMessages.English.serviceName
          document.title() shouldBe WhenDidEventHappenMessages.English.titleWithSuffix(WhenDidEventHappenMessages.English.headingAndTitle(reason, isLPP = false, isAgent = false, wasClientInformationIssue = false))
          document.getElementById("captionSpan").text() shouldBe WhenDidEventHappenMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe WhenDidEventHappenMessages.English.headingAndTitle(reason, isLPP = false, isAgent = false, wasClientInformationIssue = false)
          document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup(reason) {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = get("/when-did-the-event-happen", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe WhenDidEventHappenMessages.English.serviceName
          document.title() shouldBe WhenDidEventHappenMessages.English.titleWithSuffix(WhenDidEventHappenMessages.English.headingAndTitle(reason, isLPP = false, isAgent = true, wasClientInformationIssue = false))
          document.getElementById("captionSpan").text() shouldBe WhenDidEventHappenMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe WhenDidEventHappenMessages.English.headingAndTitle(reason, isLPP = false, isAgent = true, wasClientInformationIssue = false)
          document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }

    s"POST /when-did-the-event-happen $reason" when {

      "the date is valid" when {

        Seq(true, false).foreach { isLate =>

          s"the appeal isLate='$isLate'" should {

            val redirectLocation = reason match {
              case TechnicalIssues => routes.WhenDidEventEndController.onPageLoad().url
              case Crime => routes.CrimeReportedController.onPageLoad().url
              case Other => routes.MissedDeadlineReasonController.onPageLoad().url
              case _ =>
                if (isLate) routes.LateAppealController.onPageLoad().url
                else routes.CheckYourAnswersController.onPageLoad().url
            }

            s"save the value to UserAnswers AND redirect to $redirectLocation" in new Setup(reason, isLate) {

              stubAuth(OK, successfulIndividualAuthResponse)
              userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

              val result = post("/when-did-the-event-happen")(Map(
                WhenDidEventHappenForm.key + ".day" -> "02",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> "2024"))

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(redirectLocation)

              userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhenDidEventHappenPage)) shouldBe Some(LocalDate.of(2024, 4, 2))
            }
          }
        }
      }

      "the date is not valid - day missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required", args = Seq("day"))
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - month missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required", args = Seq("month"))
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
        }
      }

      "the date is not valid - year missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required", args = Seq("year"))
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".year"}"
        }
      }

      "the date is not valid - two fields missing - day and month" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required.two", args = Seq("day", "month"))
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - two fields missing - day and year" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required.two", args = Seq("day", "year"))
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - two fields missing - month and year" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "04",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required.two", args = Seq("month", "year"))
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
        }
      }

      "the date is not valid - all fields missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "required.all")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - Invalid format day" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "aa",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "invalid")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - Invalid format month" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "aa",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "invalid")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
        }
      }

      "the date is not valid - Invalid format year" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "aaaa"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "invalid")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".year"}"
        }
      }

      "the date is not valid - date is in the future" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reason) {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "2027"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason, "notInFuture")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }
    }
  }
}
