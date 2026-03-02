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
import fixtures.messages.ReasonableExcuseMessages
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ReasonableExcusesForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

import java.time.LocalDate


class ReasonableExcuseControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))
  
  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
    super.beforeEach()
  }

  "GET /reason-for-missing-deadline" should {
    testNavBar("/reason-for-missing-deadline")()

    "when the appeal is a 2nd Stage Appeal" when {
      "should save the reason as 'Other' and redirect" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP2ndStage).futureValue

          val result = get("/reason-for-missing-deadline")
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent = false, is2ndStageAppeal = true).url)
        }

        "the user is an authorised Agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP2ndStage).futureValue

          val result = get("/agent-reason-for-missing-deadline", isAgent = true)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent = true, is2ndStageAppeal = true).url)
        }
      }
    }

    "when the appeal is a 1st Stage Appeal" when {
      "return an OK with a view pre-populated" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(
            emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Bereavement)
          ).futureValue
          val result = get("/reason-for-missing-deadline")
          result.status shouldBe OK
          val document = Jsoup.parse(result.body)
          document.select(s"#$Bereavement").hasAttr("checked") shouldBe true
          document.select(s"#$Cessation").hasAttr("checked") shouldBe false
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)

          val result = get("/agent-reason-for-missing-deadline", isAgent = true)

          result.status shouldBe OK
        }
      }
      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          val result = get("/reason-for-missing-deadline")

          val document = Jsoup.parse(result.body)

          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "What was the reason for missing the submission deadline? - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe ReasonableExcuseMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
          document.getHintText.get(0).text() shouldBe "Select one reason. If more than one reason applies, choose the one that most affected your ability to meet the deadline."
          document.getElementsByAttributeValue("for", s"$Bereavement").text() shouldBe "Bereavement (someone died)"
          document.getElementsByAttributeValue("for", s"$Crime").text() shouldBe "Crime"
          document.getElementsByAttributeValue("for", s"$FireOrFlood").text() shouldBe "Fire or flood"
          document.getElementsByAttributeValue("for", s"$Health").text() shouldBe "Serious or life-threatening ill health"
          document.getElementsByAttributeValue("for", s"$TechnicalIssues").text() shouldBe "Software or technology issues"
          document.getElementsByAttributeValue("for", s"$UnexpectedHospital").text() shouldBe "Unexpected hospital stay"
          document.getElementsByAttributeValue("for", s"$Other").text() shouldBe "Other reason that is not covered by any other category"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          val result = get("/agent-reason-for-missing-deadline", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "What was the reason for missing the submission deadline? - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe ReasonableExcuseMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
          document.getHintText.get(0).text() shouldBe "Select one reason. If more than one reason applies, choose the one that most affected your client’s ability to meet the deadline."
          document.getElementsByAttributeValue("for", s"$Bereavement").text() shouldBe "Bereavement (someone died)"
          document.getElementsByAttributeValue("for", s"$Crime").text() shouldBe "Crime"
          document.getElementsByAttributeValue("for", s"$FireOrFlood").text() shouldBe "Fire or flood"
          document.getElementsByAttributeValue("for", s"$Health").text() shouldBe "Serious or life-threatening ill health"
          document.getElementsByAttributeValue("for", s"$TechnicalIssues").text() shouldBe "Software or technology issues"
          document.getElementsByAttributeValue("for", s"$UnexpectedHospital").text() shouldBe "Unexpected hospital stay"
          document.getElementsByAttributeValue("for", s"$Other").text() shouldBe "Other reason that is not covered by any other category"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }
  }


  "POST /reason-for-missing-deadline" when {

    "a valid radio option has been selected" when {

      "the answer is NOT different (or is newly captured)" should {

        "save the value to UserAnswers AND redirect to the Honesty Declaration page" in {

          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

          val result = post("/reason-for-missing-deadline")(Map(ReasonableExcusesForm.key -> Bereavement.toString))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent, is2ndStageAppeal).url)

        }
      }

      "the answer is different to a previously captured value" should {

        val existingAnswers = emptyUserAnswersWithLSP
          .setAnswer(ReasonableExcusePage, Bereavement)
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025,1,1))

        "save the value to UserAnswers, clear down existing journey answers AND redirect to the Honesty Declaration page" in {

          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(existingAnswers).futureValue

          val result = post("/reason-for-missing-deadline")(Map(ReasonableExcusesForm.key -> Crime.toString))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent, is2ndStageAppeal).url)

          val updatedData = userAnswersRepo.getUserAnswer(existingAnswers.journeyId).futureValue.map(_.data).get
          updatedData shouldBe emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Crime).data

        }
      }
    }

    "the selection for reasonable excuse is invalid" should {

      "render a bad request with the Form Error on the page with a link to the radios in error" in {

        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
        val result = post("/reason-for-missing-deadline")(Map(ReasonableExcusesForm.key -> ""))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)
        document.title() should include(ReasonableExcuseMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe ReasonableExcuseMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe ReasonableExcuseMessages.English.errorRequiredLsp
        error1Link.attr("href") shouldBe s"#bereavement"
      }
    }
  }


  "GET /reason-for-missing-deadline/check" should {
    testNavBar("/reason-for-missing-deadline/check")()

    "when the appeal is a 2nd Stage Appeal" when {
      "should save the reason as 'Other' and redirect" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP2ndStage).futureValue

          val result = get("/reason-for-missing-deadline/check")
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent = false, is2ndStageAppeal = true).url)
        }

        "the user is an authorised Agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP2ndStage).futureValue

          val result = get("/agent-reason-for-missing-deadline/check", isAgent = true)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent = true, is2ndStageAppeal = true).url)
        }
      }
    }

    "when the appeal is a 1st Stage Appeal" when {
      "return an OK with a view pre-populated" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(
            emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Bereavement)
          ).futureValue
          val result = get("/reason-for-missing-deadline/check")
          result.status shouldBe OK
          val document = Jsoup.parse(result.body)
          document.select(s"#$Bereavement").hasAttr("checked") shouldBe true
          document.select(s"#$Cessation").hasAttr("checked") shouldBe false
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)

          val result = get("/agent-reason-for-missing-deadline/check", isAgent = true)

          result.status shouldBe OK
        }
      }
      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          val result = get("/reason-for-missing-deadline/check")

          val document = Jsoup.parse(result.body)

          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "What was the reason for missing the submission deadline? - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe ReasonableExcuseMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
          document.getHintText.get(0).text() shouldBe "Select one reason. If more than one reason applies, choose the one that most affected your ability to meet the deadline."
          document.getElementsByAttributeValue("for", s"$Bereavement").text() shouldBe "Bereavement (someone died)"
          document.getElementsByAttributeValue("for", s"$Crime").text() shouldBe "Crime"
          document.getElementsByAttributeValue("for", s"$FireOrFlood").text() shouldBe "Fire or flood"
          document.getElementsByAttributeValue("for", s"$Health").text() shouldBe "Serious or life-threatening ill health"
          document.getElementsByAttributeValue("for", s"$TechnicalIssues").text() shouldBe "Software or technology issues"
          document.getElementsByAttributeValue("for", s"$UnexpectedHospital").text() shouldBe "Unexpected hospital stay"
          document.getElementsByAttributeValue("for", s"$Other").text() shouldBe "Other reason that is not covered by any other category"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          val result = get("/agent-reason-for-missing-deadline/check", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "What was the reason for missing the submission deadline? - Manage your Self Assessment - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe ReasonableExcuseMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
          document.getHintText.get(0).text() shouldBe "Select one reason. If more than one reason applies, choose the one that most affected your client’s ability to meet the deadline."
          document.getElementsByAttributeValue("for", s"$Bereavement").text() shouldBe "Bereavement (someone died)"
          document.getElementsByAttributeValue("for", s"$Crime").text() shouldBe "Crime"
          document.getElementsByAttributeValue("for", s"$FireOrFlood").text() shouldBe "Fire or flood"
          document.getElementsByAttributeValue("for", s"$Health").text() shouldBe "Serious or life-threatening ill health"
          document.getElementsByAttributeValue("for", s"$TechnicalIssues").text() shouldBe "Software or technology issues"
          document.getElementsByAttributeValue("for", s"$UnexpectedHospital").text() shouldBe "Unexpected hospital stay"
          document.getElementsByAttributeValue("for", s"$Other").text() shouldBe "Other reason that is not covered by any other category"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }
  }

  "POST /reason-for-missing-deadline/check" when {

    "a valid radio option has been selected" when {

      val existingAnswers = emptyUserAnswersWithLSP
        .setAnswer(ReasonableExcusePage, Bereavement)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025,1,1))

      "the answer is NOT different" should {

        "save the value to UserAnswers AND redirect to the Check Your Answers page" in {

          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(existingAnswers).futureValue
          val result = post("/reason-for-missing-deadline/check")(Map(ReasonableExcusesForm.key -> Bereavement.toString))
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad(isAgent).url)
        }
      }

      "the answer is different to a previously captured value" should {

        "save the value to UserAnswers, clear down existing journey answers AND redirect to the Honesty Declaration page" in {

          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(existingAnswers).futureValue

          val result = post("/reason-for-missing-deadline/check")(Map(ReasonableExcusesForm.key -> Crime.toString))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad(isAgent, is2ndStageAppeal).url)

          val updatedData = userAnswersRepo.getUserAnswer(existingAnswers.journeyId).futureValue.map(_.data).get
          updatedData shouldBe emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Crime).data
        }
      }
    }

    "the selection for reasonable excuse is invalid" should {

      "render a bad request with the Form Error on the page with a link to the radios in error" in {

        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
        val result = post("/reason-for-missing-deadline/check")(Map(ReasonableExcusesForm.key -> ""))

        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)
        document.title() should include(ReasonableExcuseMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe ReasonableExcuseMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe ReasonableExcuseMessages.English.errorRequiredLsp
        error1Link.attr("href") shouldBe s"#bereavement"
      }
    }
  }
}
