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

import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import fixtures.messages.WhenDidEventHappenMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventHappenForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{PenaltyData, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._

import java.time.LocalDate

class WhenDidEventHappenControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val reasonsWithUrls: List[(ReasonableExcuse, String, String)] = List(
    (Bereavement, "/when-did-the-person-die",  "/agent-when-did-the-person-die"),
    (Cessation, "/cessation", "/agent-cessation"),
    (Crime, "/when-did-the-crime-happen", "/agent-when-did-the-crime-happen"),
    (FireOrFlood, "/when-did-the-fire-or-flood-happen", "/agent-when-did-the-fire-or-flood-happen"),
    (Health, "/when-did-the-health-issue-begin", "/agent-when-did-the-health-issue-begin"),
    (TechnicalIssues, "/when-did-the-technology-issues-begin", "/agent-when-did-the-technology-issues-begin"),
    (UnexpectedHospital, "/when-did-the-hospital-stay-begin", "/agent-when-did-the-hospital-stay-begin"),
    (LossOfStaff, "/lossOfStaff", "/agent-lossOfStaff"),
    (Other, "/when-did-the-issue-stop-you", "/agent-when-did-the-issue-stop-you")
  )

  class Setup(reason: ReasonableExcuse, isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val lateDays: Int = if(reason == Bereavement) appConfig.bereavementLateDays else appConfig.lateDays

    val userAnswersLSP: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, reason)

    val userAnswersLPP: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLPP.copy(
        appealData = latePaymentAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, reason)

    userAnswersRepo.upsertUserAnswer(userAnswersLSP).futureValue
  }

  for (reasonWithUrl <- reasonsWithUrls) {

    s"GET ${reasonWithUrl._2} with ${reasonWithUrl._1}" should {


      testNavBar(url = reasonWithUrl._2) {
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, reasonWithUrl._1)).futureValue
      }

      "return an OK with a view" when {
        s"the user is an authorised individual AND the page has already been answered with ${reasonWithUrl._1}" in new Setup(reasonWithUrl._1) {
          stubAuthRequests(false)

          userAnswersRepo.upsertUserAnswer(userAnswersLSP.setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 4, 2))).futureValue

          val result = get(reasonWithUrl._2)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)
          document.getElementById(s"${WhenDidEventHappenForm.key + ".day"}").`val`() shouldBe "2"
          document.getElementById(s"${WhenDidEventHappenForm.key + ".month"}").`val`() shouldBe "4"
          document.getElementById(s"${WhenDidEventHappenForm.key + ".year"}").`val`() shouldBe "2024"
        }

        "the user is an authorised agent AND page NOT already answered" in new Setup(reasonWithUrl._1) {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(userAnswersLSP).futureValue

          val result = get(reasonWithUrl._3, isAgent = true)

          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getElementById(s"${WhenDidEventHappenForm.key + ".day"}").`val`() shouldBe ""
          document.getElementById(s"${WhenDidEventHappenForm.key + ".month"}").`val`() shouldBe ""
          document.getElementById(s"${WhenDidEventHappenForm.key + ".year"}").`val`() shouldBe ""
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup(reasonWithUrl._1) {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(userAnswersLSP).futureValue

          val result = get(reasonWithUrl._2)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe WhenDidEventHappenMessages.English.serviceName
          document.title() shouldBe WhenDidEventHappenMessages.English.titleWithSuffix(WhenDidEventHappenMessages.English.headingAndTitle(reasonWithUrl._1, isLPP = false, isAgent = false, wasClientInformationIssue = false))
          document.getElementById("captionSpan").text() shouldBe WhenDidEventHappenMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe WhenDidEventHappenMessages.English.headingAndTitle(reasonWithUrl._1, isLPP = false, isAgent = false, wasClientInformationIssue = false)
          document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup(reasonWithUrl._1) {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(userAnswersLSP).futureValue

          val result = get(reasonWithUrl._3, isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe WhenDidEventHappenMessages.English.serviceName
          document.title() shouldBe WhenDidEventHappenMessages.English.titleWithSuffix(WhenDidEventHappenMessages.English.headingAndTitle(reasonWithUrl._1, isLPP = false, isAgent = true, wasClientInformationIssue = false))
          document.getElementById("captionSpan").text() shouldBe WhenDidEventHappenMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe WhenDidEventHappenMessages.English.headingAndTitle(reasonWithUrl._1, isLPP = false, isAgent = true, wasClientInformationIssue = false)
          document.getElementById("date-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", "date.day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", "date.month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", "date.year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }

    Seq(true, false).foreach { isAgent =>

      Seq(true, false).foreach { isLPP =>

        s"POST ${if (isAgent) reasonWithUrl._3 else reasonWithUrl._2} with ${reasonWithUrl._1} isAgent = $isAgent, isLPP = $isLPP" when {

          "the date is valid" when {


            Seq(true, false).foreach { isLate =>

              s"the appeal isLate='$isLate'" should {

                val redirectLocation = reasonWithUrl._1 match {
                  case TechnicalIssues => routes.WhenDidEventEndController.onPageLoad(reasonWithUrl._1, isAgent).url
                  case Crime => routes.CrimeReportedController.onPageLoad(isAgent).url
                  case UnexpectedHospital => routes.HasHospitalStayEndedController.onPageLoad(isAgent).url
                  case Other => routes.MissedDeadlineReasonController.onPageLoad(isLPP, isAgent).url
                  case _ =>
                    if (isLate) routes.LateAppealController.onPageLoad(isAgent).url
                    else routes.CheckYourAnswersController.onPageLoad(isAgent).url
                }

                s"save the value to UserAnswers AND redirect to $redirectLocation with ${reasonWithUrl._1}" in new Setup(reasonWithUrl._1, isLate) {

                  stubAuthRequests(isAgent)
                  userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

                  val result: WSResponse = post(if (isAgent) reasonWithUrl._3 else reasonWithUrl._2)(Map(
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

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> "2024"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required", args = Seq("day"), isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
            }
          }

          "the date is not valid - month missing" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "02",
                WhenDidEventHappenForm.key + ".month" -> "",
                WhenDidEventHappenForm.key + ".year" -> "2024"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required", args = Seq("month"), isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
            }
          }

          "the date is not valid - year missing" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "02",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> ""))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required", args = Seq("year"), isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".year"}"
            }
          }

          "the date is not valid - two fields missing - day and month" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "",
                WhenDidEventHappenForm.key + ".month" -> "",
                WhenDidEventHappenForm.key + ".year" -> "2024"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required.two", args = Seq("day", "month"), isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
            }
          }

          "the date is not valid - two fields missing - day and year" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> ""))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required.two", args = Seq("day", "year"), isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
            }
          }

          "the date is not valid - two fields missing - month and year" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "04",
                WhenDidEventHappenForm.key + ".month" -> "",
                WhenDidEventHappenForm.key + ".year" -> ""))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required.two", args = Seq("month", "year"), isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
            }
          }

          "the date is not valid - all fields missing" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "",
                WhenDidEventHappenForm.key + ".month" -> "",
                WhenDidEventHappenForm.key + ".year" -> ""))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "required.all", isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
            }
          }

          "the date is not valid - Invalid format day" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "aa",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> "2024"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "invalid", isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
            }
          }

          "the date is not valid - Invalid format month" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "02",
                WhenDidEventHappenForm.key + ".month" -> "aa",
                WhenDidEventHappenForm.key + ".year" -> "2024"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "invalid", isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".month"}"
            }
          }

          "the date is not valid - Invalid format year" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "02",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> "aaaa"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "invalid", isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".year"}"
            }
          }

          "the date is not valid - date is in the future" should {

            "render a bad request with the Form Error on the page with a link to the field in error" in new Setup(reasonWithUrl._1) {

              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(if(isLPP)userAnswersLPP else userAnswersLSP).futureValue

              val result = post(reasonWithUrl._2)(Map(
                WhenDidEventHappenForm.key + ".day" -> "02",
                WhenDidEventHappenForm.key + ".month" -> "04",
                WhenDidEventHappenForm.key + ".year" -> "2027"))

              result.status shouldBe BAD_REQUEST

              val document = Jsoup.parse(result.body)

              document.title() should include(WhenDidEventHappenMessages.English.errorPrefix)
              document.select(".govuk-error-summary__title").text() shouldBe WhenDidEventHappenMessages.English.thereIsAProblem

              val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
              error1Link.text() shouldBe WhenDidEventHappenMessages.English.errorMessageConstructor(reasonWithUrl._1, "notInFuture", isAgent = isAgent)
              error1Link.attr("href") shouldBe s"#${WhenDidEventHappenForm.key + ".day"}"
            }
          }
        }
      }
    }
  }
}
