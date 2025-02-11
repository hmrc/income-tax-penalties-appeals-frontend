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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventHappenForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

import java.time.LocalDate

class WhenDidEventHappenControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val bereavementMessage: String = "When did the person die?"
  val cessationMessage: String = "TBC cessation"
  val crimeMessage: String = "When did the crime happen?"
  val fireOrFloodReasonMessage: String = "When did the fire or flood happen?"
  val healthMessage: String = "TBC health"
  val technicalIssueMessage: String = "When did the software or technology issues begin?"
  val unexpectedHospitalMessage: String = "TBC unexpectedHospital"
  val otherMessage: String = "TBC other"

  val reasonsList: List[(String, String)] = List(
    ("bereavement", bereavementMessage),
    ("cessation", cessationMessage),
    ("crime", crimeMessage),
    ("fireOrFlood", fireOrFloodReasonMessage),
    ("health", healthMessage),
    ("technicalIssues", technicalIssueMessage),
    ("unexpectedHospital", unexpectedHospitalMessage),
    ("other", otherMessage)
  )

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  for (reason <- reasonsList) {

    val userAnswersWithReason =
      UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, reason._1)

    s"GET /when-did-the-event-happen with ${reason._1}" should {

      testNavBar(url = "/honesty-declaration") {
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
      }

      "return an OK with a view" when {
        "the user is an authorised individual AND the page has already been answered" in {
          stubAuth(OK, successfulIndividualAuthResponse)

          userAnswersRepo.upsertUserAnswer(userAnswersWithReason.setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 4, 2))).futureValue

          val result = get("/when-did-the-event-happen")

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)
          document.getElementById(s"${WhenDidEventHappenForm.key + ".day"}").`val`() shouldBe "2"
          document.getElementById(s"${WhenDidEventHappenForm.key + ".month"}").`val`() shouldBe "4"
          document.getElementById(s"${WhenDidEventHappenForm.key + ".year"}").`val`() shouldBe "2024"
        }

        "the user is an authorised agent AND page NOT already answered" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/when-did-the-event-happen", isAgent = true)

          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getElementById(s"${WhenDidEventHappenForm.key + ".day"}").`val`() shouldBe ""
          document.getElementById(s"${WhenDidEventHappenForm.key + ".month"}").`val`() shouldBe ""
          document.getElementById(s"${WhenDidEventHappenForm.key + ".year"}").`val`() shouldBe ""
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/when-did-the-event-happen")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"${reason._2} - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe reason._2
          document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/when-did-the-event-happen", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"${reason._2} - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe reason._2
          document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }

    s"POST /when-did-the-event-happen ${reason._1}" when {

      "the date is valid" should {

        if (reason._1 == "bereavement" | reason._1 == "fireOrFlood") {
          "save the value to UserAnswers AND redirect to the making-a-late-appeal page" in {

            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

            val result = post("/when-did-the-event-happen")(Map(
              WhenDidEventHappenForm.key + ".day" -> "02",
              WhenDidEventHappenForm.key + ".month" -> "04",
              WhenDidEventHappenForm.key + ".year" -> "2024"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad().url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhenDidEventHappenPage)) shouldBe Some(LocalDate.of(2024, 4, 2))
          }
        } else if (reason._1 == "crime") {
          "save the value to UserAnswers AND redirect to the has-this-crime-been-reported page" in {

            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

            val result = post("/when-did-the-event-happen")(Map(
              WhenDidEventHappenForm.key + ".day" -> "02",
              WhenDidEventHappenForm.key + ".month" -> "04",
              WhenDidEventHappenForm.key + ".year" -> "2024"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.CrimeReportedController.onPageLoad().url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhenDidEventHappenPage)) shouldBe Some(LocalDate.of(2024, 4, 2))
          }
        } else if (reason._1 == "technicalIssue") {
          "save the value to UserAnswers AND redirect to the when-did-the-event-end page" in {

            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

            val result = post("/when-did-the-event-happen")(Map(
              WhenDidEventHappenForm.key + ".day" -> "02",
              WhenDidEventHappenForm.key + ".month" -> "04",
              WhenDidEventHappenForm.key + ".year" -> "2024"))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.WhenDidEventEndController.onPageLoad().url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhenDidEventHappenPage)) shouldBe Some(LocalDate.of(2024, 4, 2))
          }
        }
      }

      "the date is not valid - day missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

            val result = post("/when-did-the-event-happen")(Map(
              WhenDidEventHappenForm.key + ".day" -> "",
              WhenDidEventHappenForm.key + ".month" -> "04",
              WhenDidEventHappenForm.key + ".year" -> "2024"))

            result.status shouldBe BAD_REQUEST

            val document = Jsoup.parse(result.body)

            document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
            document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

            val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
            error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required", "day")
            error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - month missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

            val result = post("/when-did-the-event-happen")(Map(
              WhenDidEventHappenForm.key + ".day" -> "02",
              WhenDidEventHappenForm.key + ".month" -> "",
              WhenDidEventHappenForm.key + ".year" -> "2024"))

            result.status shouldBe BAD_REQUEST

            val document = Jsoup.parse(result.body)

            document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
            document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

            val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
            error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required", "month")
            error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
        }
      }

      "the date is not valid - year missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required", "year")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".year"}"
        }
      }

      "the date is not valid - two fields missing - day and month" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required.two", "day", "month")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - two fields missing - day and year" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required.two", "day", "year")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - two fields missing - month and year" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "04",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required.two", "month", "year")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
        }
      }

      "the date is not valid - all fields missing" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "",
            WhenDidEventHappenForm.key + ".month" -> "",
            WhenDidEventHappenForm.key + ".year" -> ""))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "required.all")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - Invalid format day" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "aa",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "invalid")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }

      "the date is not valid - Invalid format month" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "aa",
            WhenDidEventHappenForm.key + ".year" -> "2024"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "invalid")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
        }
      }

      "the date is not valid - Invalid format year" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "aaaa"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "invalid")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".year"}"
        }
      }

      "the date is not valid - date is in the future" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = post("/when-did-the-event-happen")(Map(
            WhenDidEventHappenForm.key + ".day" -> "02",
            WhenDidEventHappenForm.key + ".month" -> "04",
            WhenDidEventHappenForm.key + ".year" -> "2027"))

          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reason._1, "notInFuture")
          error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
        }
      }
    }
  }
}
