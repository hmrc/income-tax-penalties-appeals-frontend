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

import fixtures.messages.ExtraEvidenceMessages
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import org.jsoup.select.Elements
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ExtraEvidenceForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ExtraEvidencePage, JointAppealPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}

class ExtraEvidenceControllerISpec extends ControllerISpecHelper {

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  class Setup(isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val otherAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else        timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, Other)

    userAnswersRepo.upsertUserAnswer(otherAnswers).futureValue
  }

  List(true, false).foreach { isAgent =>

    val url = if(isAgent) "/agent-upload-evidence-for-the-appeal" else "/upload-evidence-for-the-appeal"

    s"GET $url" should {

      if(!isAgent) {
        testNavBar(url = url)(
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Other)).futureValue
        )
      }

      "return an OK with a view" when {
        "the user is an authorised AND the page has already been answered" in new Setup() {
          stubAuthRequests(isAgent)
          userAnswersRepo.upsertUserAnswer(otherAnswers.setAnswer(ExtraEvidencePage, true)).futureValue

          val result: WSResponse = get(url)
          result.status shouldBe OK

          val document: nodes.Document = Jsoup.parse(result.body)
          document.select(s"#${ExtraEvidenceForm.key}").hasAttr("checked") shouldBe true
          document.select(s"#${ExtraEvidenceForm.key}-2").hasAttr("checked") shouldBe false
        }
      }

      "the journey is for a 1st Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised individual" in new Setup() {
            stubAuthRequests(isAgent)
            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Do you want to upload evidence to support your appeal? - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe ExtraEvidenceMessages.English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Do you want to upload evidence to support your appeal?"
            document.getElementById("extraEvidence-hint").text() shouldBe "We will still review your appeal if you do not upload evidence."
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}").text() shouldBe ExtraEvidenceMessages.English.yes
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}-2").text() shouldBe ExtraEvidenceMessages.English.no
            document.getSubmitButton.text() shouldBe "Continue"
          }
        }
      }

      "the journey is for a 2nd Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised" in new Setup() {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP2ndStage)

            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Do you want to upload evidence to support this review? - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe ExtraEvidenceMessages.English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Do you want to upload evidence to support this review?"
            document.getElementById("extraEvidence-hint").text() shouldBe "Uploading evidence is optional. We will still review the original appeal decision if you do not upload evidence."
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}").text() shouldBe ExtraEvidenceMessages.English.yes
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}-2").text() shouldBe ExtraEvidenceMessages.English.no
            document.getSubmitButton.text() shouldBe "Continue"
          }

          "the user is an authorised appealing a single LPPs" in new Setup() {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage.setAnswer(JointAppealPage, false))

            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Do you want to upload evidence to support this review? - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe ExtraEvidenceMessages.English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Do you want to upload evidence to support this review?"
            document.getElementById("extraEvidence-hint").text() shouldBe "Uploading evidence is optional. We will still review the original appeal decision if you do not upload evidence."
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}").text() shouldBe ExtraEvidenceMessages.English.yes
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}-2").text() shouldBe ExtraEvidenceMessages.English.no
            document.getSubmitButton.text() shouldBe "Continue"
          }

          "the user is an authorised appealing multiple LPPs" in new Setup() {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage.setAnswer(JointAppealPage, true))

            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Do you want to upload evidence to support this review? - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe ExtraEvidenceMessages.English.lppCaptionMultiple(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Do you want to upload evidence to support this review?"
            document.getElementById("extraEvidence-hint").text() shouldBe "Uploading evidence is optional. We will still review the original appeal decisions if you do not upload evidence."
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}").text() shouldBe ExtraEvidenceMessages.English.yes
            document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}-2").text() shouldBe ExtraEvidenceMessages.English.no
            document.getSubmitButton.text() shouldBe "Continue"
          }
        }
      }


    }

    s"POST $url" when {

      "the radio option posted is valid" should {

        "save the value to UserAnswers AND redirect to the UpscanCheckAnswers page if the answer is 'Yes'" in new Setup() {

          stubAuthRequests(isAgent)

          val result: WSResponse = post(url)(Map(ExtraEvidenceForm.key -> true))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(controllers.upscan.routes.UpscanCheckAnswersController.onPageLoad(isAgent).url)

          userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(ExtraEvidencePage)) shouldBe Some(true)
        }

        "save the value to UserAnswers AND redirect the answer is 'No'" when {

          "appeal is Late" should {

            "redirect to the LateAppeal page" in new Setup(isLate = true) {

              stubAuthRequests(isAgent)

              val result: WSResponse = post(url)(Map(ExtraEvidenceForm.key -> false))

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad(isAgent).url)

              userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(ExtraEvidencePage)) shouldBe Some(false)
            }
          }

          "appeal is NOT Late" should {

            "redirect to the CheckAnswers page" in new Setup() {

              stubAuthRequests(isAgent)

              val result: WSResponse = post(url)(Map(ExtraEvidenceForm.key -> false))

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad(isAgent).url)

              userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(ExtraEvidencePage)) shouldBe Some(false)
            }
          }
        }
      }

      "the radio option is invalid" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

          stubAuthRequests(isAgent)

          val result: WSResponse = post(url)(Map(ExtraEvidenceForm.key -> ""))
          result.status shouldBe BAD_REQUEST

          val document: nodes.Document = Jsoup.parse(result.body)
          document.title() should include(ExtraEvidenceMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe ExtraEvidenceMessages.English.thereIsAProblem

          val error1Link: Elements = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe ExtraEvidenceMessages.English.errorRequired
          error1Link.attr("href") shouldBe s"#${ExtraEvidenceForm.key}"
        }
      }
    }
  }

}
