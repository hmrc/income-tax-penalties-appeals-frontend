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

import fixtures.messages.CrimeReportedMessages
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.CrimeReportedForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Crime
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CrimeReportedEnum, PenaltyData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{CrimeReportedPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}

class CrimeReportedControllerISpec extends ControllerISpecHelper {

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  class Setup(isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val crimeAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, Crime)

    userAnswersRepo.upsertUserAnswer(crimeAnswers).futureValue
  }

  "GET /has-this-crime-been-reported" should {

    testNavBar(url = "/has-this-crime-been-reported")(
      userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Crime)).futureValue
    )

    "return an OK with a view" when {
      "the user is an authorised individual AND the page has already been answered" in new Setup() {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(crimeAnswers.setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)).futureValue

        val result = get("/has-this-crime-been-reported")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.select(s"#${CrimeReportedForm.key}").hasAttr("checked") shouldBe true
        document.select(s"#${CrimeReportedForm.key}-2").hasAttr("checked") shouldBe false
        document.select(s"#${CrimeReportedForm.key}-3").hasAttr("checked") shouldBe false
      }

      "the user is an authorised agent AND page NOT already answered" in new Setup() {
        stubAuthRequests(true)

        val result = get("/agent-has-this-crime-been-reported", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.select(s"#${CrimeReportedForm.key}").hasAttr("checked") shouldBe false
        document.select(s"#${CrimeReportedForm.key}-2").hasAttr("checked") shouldBe false
        document.select(s"#${CrimeReportedForm.key}-3").hasAttr("checked") shouldBe false
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in new Setup() {
        stubAuthRequests(false)
        val result = get("/has-this-crime-been-reported")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Has this crime been reported to the police? - Manage your Self Assessment - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe CrimeReportedMessages.English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "Has this crime been reported to the police?"
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}").text() shouldBe CrimeReportedMessages.English.yes
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}-2").text() shouldBe CrimeReportedMessages.English.no
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in new Setup() {
        stubAuthRequests(true)
        val result = get("/agent-has-this-crime-been-reported", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Has this crime been reported to the police? - Manage your Self Assessment - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe CrimeReportedMessages.English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "Has this crime been reported to the police?"
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}").text() shouldBe CrimeReportedMessages.English.yes
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}-2").text() shouldBe CrimeReportedMessages.English.no
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

  "POST /has-this-crime-been-reported" when {

    "the radio option posted is valid" when {

      "the appeal is late" should {

        "save the value to UserAnswers AND redirect to the LateAppeal page" in new Setup(isLate = true) {

          stubAuthRequests(false)

          val result = post("/has-this-crime-been-reported")(Map(CrimeReportedForm.key -> CrimeReportedEnum.yes))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad(isAgent).url)

          userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(CrimeReportedPage)) shouldBe Some(CrimeReportedEnum.yes)
        }
      }

      "the appeal is NOT late" should {

        "save the value to UserAnswers AND redirect to the Check Answers page" in new Setup() {

          stubAuthRequests(false)

          val result = post("/has-this-crime-been-reported")(Map(CrimeReportedForm.key -> CrimeReportedEnum.yes))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad(isAgent).url)

          userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(CrimeReportedPage)) shouldBe Some(CrimeReportedEnum.yes)
        }
      }
    }

    "the radio option is invalid" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuthRequests(false)

        val result = post("/has-this-crime-been-reported")(Map(CrimeReportedForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)
        document.title() should include(CrimeReportedMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe CrimeReportedMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe CrimeReportedMessages.English.errorRequired
        error1Link.attr("href") shouldBe s"#${CrimeReportedForm.key}"
      }
    }
  }

}
