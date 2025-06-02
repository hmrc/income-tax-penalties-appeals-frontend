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


    val userAnswersWithReasonLSP: UserAnswers =
      emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Other)

    val userAnswersWithReasonLPP: UserAnswers =
      emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, Other)

    val userAnswersWithReasonWithMultipleLPPs: UserAnswers =
      emptyUserAnswersWithMultipleLPPs.setAnswer(ReasonableExcusePage, Other)
        .setAnswer(JointAppealPage, true)
    //  second stage appeals
    val userAnswersWithReasonLSP2ndStage: UserAnswers =
      emptyUserAnswersWithLSP2ndStage.setAnswer(ReasonableExcusePage, Other)

    val userAnswersWithReasonLPP2ndStage: UserAnswers =
      emptyUserAnswersWithLPP2ndStage.setAnswer(ReasonableExcusePage, Other)

    val userAnswersWithReasonWithMultipleLPPs2ndStage: UserAnswers =
      emptyUserAnswersWithMultipleLPPs2ndStage.setAnswer(ReasonableExcusePage, Other)
        .setAnswer(JointAppealPage, true)


    def userAnswers(isLPP: Boolean, is2ndStageAppeal: Boolean, isJointAppeal: Boolean): UserAnswers = {
      if (is2ndStageAppeal) {
        if (isLPP) {
          if (isJointAppeal) {
            userAnswersWithReasonWithMultipleLPPs2ndStage
          } else {
            userAnswersWithReasonLPP2ndStage
          }
        } else userAnswersWithReasonLSP2ndStage

      } else if (isLPP) {
        if (isJointAppeal) {
          userAnswersWithReasonWithMultipleLPPs
        } else {
          userAnswersWithReasonLPP
        }
      } else userAnswersWithReasonLSP
    }

    val otherAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(
        appealData = lateSubmissionAppealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, Other)

    userAnswersRepo.upsertUserAnswer(otherAnswers).futureValue


    def caption(isLPP: Boolean): String = if (isLPP) {
      ExtraEvidenceMessages.English.lppCaption(
        dateToString(latePaymentAppealData.startDate),
        dateToString(latePaymentAppealData.endDate)
      )
    } else {
      ExtraEvidenceMessages.English.lspCaption(
        dateToString(lateSubmissionAppealData.startDate),
        dateToString(lateSubmissionAppealData.endDate)
      )
    }
  }

  def url(is2ndStageAppeal: Boolean, isAgent: Boolean): String = {
    if (is2ndStageAppeal) {
      if (isAgent) "/agent-upload-evidence-for-the-review" else "/upload-evidence-for-the-review"
    } else if (isAgent) "/agent-upload-evidence-for-the-appeal" else "/upload-evidence-for-the-appeal"
  }


  //  LPP penalty type (First stage appeal/Second stage appeal  -  Single appeal/Joint appeal)
  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { is2ndStageAppeal =>
      Seq(true, false).foreach { isJointAppeal =>


        s"GET LPP ${url(is2ndStageAppeal, isAgent)} with is2ndStageAppeal = $is2ndStageAppeal, isAgent = $isAgent, isJointAppeal = $isJointAppeal" should {

          if (!isAgent) {
            testNavBar(url = url(is2ndStageAppeal, isAgent))(
              userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, Other)).futureValue
            )
          }

          "return an OK with a view" when {
            s"the user is an authorised AND the page has already been answered with is2ndStageAppeal = $is2ndStageAppeal, isAgent = $isAgent, isJointAppeal = $isJointAppeal" in new Setup() {
              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal).setAnswer(ExtraEvidencePage, true)).futureValue

              val result: WSResponse = get(url(is2ndStageAppeal, isAgent))
              result.status shouldBe OK

              val document: nodes.Document = Jsoup.parse(result.body)
              document.select(s"#${ExtraEvidenceForm.key}").hasAttr("checked") shouldBe true
              document.select(s"#${ExtraEvidenceForm.key}-2").hasAttr("checked") shouldBe false
            }
            s"the user is an authorised AND the page has NOT been answered with is2ndStageAppeal = $is2ndStageAppeal, isAgent = $isAgent, isJointAppeal = $isJointAppeal" in new Setup() {
              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)).futureValue

              val result: WSResponse = get(url(is2ndStageAppeal, isAgent))
              result.status shouldBe OK

              val document: nodes.Document = Jsoup.parse(result.body)
              document.select(s"#${ExtraEvidenceForm.key}").hasAttr("checked") shouldBe false
              document.select(s"#${ExtraEvidenceForm.key}-2").hasAttr("checked") shouldBe false
            }
          }

          s"the journey is for with is2ndStageAppeal = $is2ndStageAppeal isAgent = $isAgent isJointAppeal = $isJointAppeal" when {
            "the page has the correct elements" when {
              "the user is an authorised" in new Setup() {
                stubAuthRequests(isAgent)
                userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal).setAnswer(ExtraEvidencePage, true)).futureValue

                val result: WSResponse = get(url(is2ndStageAppeal, isAgent))

                val document: nodes.Document = Jsoup.parse(result.body)

                document.getServiceName.text() shouldBe "Manage your Self Assessment"
                document.title() shouldBe s"${ExtraEvidenceMessages.English.headingAndTitle(is2ndStageAppeal)} - Manage your Self Assessment - GOV.UK"
                document.getElementById("captionSpan").text() shouldBe caption(true)

                document.getH1Elements.text() shouldBe ExtraEvidenceMessages.English.headingAndTitle(is2ndStageAppeal)
                document.getElementById("extraEvidence-hint").text() shouldBe ExtraEvidenceMessages.English.hintText(is2ndStageAppeal, isJointAppeal)
                document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}").text() shouldBe ExtraEvidenceMessages.English.yes
                document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}-2").text() shouldBe ExtraEvidenceMessages.English.no
                document.getSubmitButton.text() shouldBe "Continue"
              }
            }
          }
        }
      }
    }
  }

  //  LSP penalty type (First stage appeal/Second stage appeal)
  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { is2ndStageAppeal =>

      s"GET LSP ${url(is2ndStageAppeal, isAgent)} with is2ndStageAppeal = $is2ndStageAppeal, isAgent = $isAgent" should {

        if (!isAgent) {
          testNavBar(url = url(is2ndStageAppeal, isAgent))(
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, Other)).futureValue
          )
        }

        "return an OK with a view" when {
          s"the user is an authorised AND the page has already been answered with is2ndStageAppeal = $is2ndStageAppeal, isAgent = $isAgent" in new Setup() {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = false, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false).setAnswer(ExtraEvidencePage, true)).futureValue

            val result: WSResponse = get(url(is2ndStageAppeal, isAgent))
            result.status shouldBe OK

            val document: nodes.Document = Jsoup.parse(result.body)
            document.select(s"#${ExtraEvidenceForm.key}").hasAttr("checked") shouldBe true
            document.select(s"#${ExtraEvidenceForm.key}-2").hasAttr("checked") shouldBe false
          }
          s"the user is an authorised AND the page has NOT been answered with is2ndStageAppeal = $is2ndStageAppeal, isAgent = $isAgent" in new Setup() {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = false, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false)).futureValue

            val result: WSResponse = get(url(is2ndStageAppeal, isAgent))
            result.status shouldBe OK

            val document: nodes.Document = Jsoup.parse(result.body)
            document.select(s"#${ExtraEvidenceForm.key}").hasAttr("checked") shouldBe false
            document.select(s"#${ExtraEvidenceForm.key}-2").hasAttr("checked") shouldBe false
          }
        }

        s"the journey is for with is2ndStageAppeal = $is2ndStageAppeal isAgent = $isAgent" when {
          "the page has the correct elements" when {
            "the user is an authorised" in new Setup() {
              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = false, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false).setAnswer(ExtraEvidencePage, true)).futureValue

              val result: WSResponse = get(url(is2ndStageAppeal, isAgent))

              val document: nodes.Document = Jsoup.parse(result.body)

              document.getServiceName.text() shouldBe "Manage your Self Assessment"
              document.title() shouldBe s"${ExtraEvidenceMessages.English.headingAndTitle(is2ndStageAppeal)} - Manage your Self Assessment - GOV.UK"
              document.getElementById("captionSpan").text() shouldBe caption(false)

              document.getH1Elements.text() shouldBe ExtraEvidenceMessages.English.headingAndTitle(is2ndStageAppeal)
              document.getElementById("extraEvidence-hint").text() shouldBe ExtraEvidenceMessages.English.hintText(is2ndStageAppeal)
              document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}").text() shouldBe ExtraEvidenceMessages.English.yes
              document.getElementsByAttributeValue("for", s"${ExtraEvidenceForm.key}-2").text() shouldBe ExtraEvidenceMessages.English.no
              document.getSubmitButton.text() shouldBe "Continue"
            }
          }
        }
      }
    }
  }
  
  Seq(true, false).foreach { isAgent =>

    s"POST ${url(is2ndStageAppeal = false, isAgent = isAgent)}" when {

      "the radio option posted is valid" should {

        "save the value to UserAnswers AND redirect to the UpscanCheckAnswers page if the answer is 'Yes'" in new Setup() {

          stubAuthRequests(isAgent)

          val result: WSResponse = post(url(is2ndStageAppeal = false, isAgent = isAgent))(Map(ExtraEvidenceForm.key -> true))

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(controllers.upscan.routes.UpscanCheckAnswersController.onPageLoad(isAgent, is2ndStageAppeal = false).url)

          userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(ExtraEvidencePage)) shouldBe Some(true)
        }

        "save the value to UserAnswers AND redirect the answer is 'No'" when {

          "appeal is Late" should {

            "redirect to the LateAppeal page" in new Setup(isLate = true) {

              stubAuthRequests(isAgent)

              val result: WSResponse = post(url(is2ndStageAppeal = false, isAgent = isAgent))(Map(ExtraEvidenceForm.key -> false))

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad(isAgent, is2ndStageAppeal = false).url)

              userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(ExtraEvidencePage)) shouldBe Some(false)
            }
          }

          "appeal is NOT Late" should {

            "redirect to the CheckAnswers page" in new Setup() {

              stubAuthRequests(isAgent)

              val result: WSResponse = post(url(is2ndStageAppeal = false, isAgent = isAgent))(Map(ExtraEvidenceForm.key -> false))

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

          val result: WSResponse = post(url(is2ndStageAppeal = false, isAgent = isAgent))(Map(ExtraEvidenceForm.key -> ""))
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
