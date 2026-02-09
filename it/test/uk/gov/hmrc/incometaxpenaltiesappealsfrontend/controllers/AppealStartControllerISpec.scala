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

import fixtures.messages.{English, ReviewAppealStartMessages}
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, NormalMode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.WhoPlannedToSubmitPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString


class AppealStartControllerISpec extends ControllerISpecHelper {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  Map("/appeal-start" -> false, "/agent-appeal-start" -> true).foreach { case (path, isAgent) =>

    s"GET $path" when {

      if (!isAgent) {
        testNavBar("/appeal-start")(
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
        )
      }

      s"the journey is for a 1st Stage Appeal with isAgent = $isAgent and path = $path" when {
        "the penalty type is Late Submission Penalty (LSP)" should {
          "render the page has the correct elements" in {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

            val result = get(path, isAgent = isAgent)

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Appeal a Self Assessment penalty - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
            document.getParagraphs.get(0).text() shouldBe "To appeal a late submission penalty for Self Assessment, you will need to ask HMRC to look at your case again."
            document.getH2Elements.get(0).text() shouldBe "Before you start"
            document.getParagraphs.get(1).text() shouldBe "You will need:"
            document.getBulletPoints.get(0).text() shouldBe "a reason why the quarterly update or tax return was not submitted by the due date (HMRC calls this ‘a reasonable excuse’)"
            document.getBulletPoints.get(1).text() shouldBe "the dates related to this reasonable excuse"
            document.getBulletPoints.get(2).text() shouldBe "details of why you did not appeal sooner"
            document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
            document.getH2Elements.get(1).text() shouldBe "Income sources that have ceased"

            if(isAgent){
              document.getParagraphs.get(3).text() shouldBe "If your client has received a penalty for an update period that started after the income source had stopped permanently (ceased), they may be able to get that point removed."

            } else {
              document.getParagraphs.get(3).text() shouldBe "If you have received a penalty for an update period that started after the income source had stopped permanently (ceased), you may be able to get that point removed."
            }

            if(isAgent){
              document.getParagraphs.get(4).text() shouldBe "To do this, they will need to confirm the dates that a particular income source was ceased to HMRC."

            } else {
              document.getParagraphs.get(4).text() shouldBe "To do this, you will need to confirm the date that a particular income source was ceased to HMRC."
            }

            document.getLink("cessationLink").text() shouldBe "Add, manage or cease a business or income source"

            document.getH2Elements.get(2).text() shouldBe "Sending evidence with an appeal"
            document.getParagraphs.get(6).text() shouldBe "In some cases, you will be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
            document.getParagraphs.get(7).text() shouldBe "If you are not asked for extra evidence, this is because we do not need any to make a decision in your particular case."
            document.getWarningText.get(0).text() shouldBe "Warning If we decide we need extra evidence after reviewing your appeal, we will contact you."

            document.getSubmitButton.text() shouldBe "Start an appeal"
            document.getSubmitButton.attr("href") shouldBe {
              if (isAgent) routes.WhoPlannedToSubmitController.onPageLoad(mode = NormalMode).url
              else routes.ReasonableExcuseController.onPageLoad(isAgent, NormalMode).url
            }
          }
        }

        "the penalty type is Late Payment Penalty (LPP) (single Penalty)" should {
          "render the page with a link to Reasonable Excuse" in {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP).futureValue

            val result = get(path, isAgent = isAgent)
            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Appeal a Self Assessment penalty - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe English.lppCaptionAppealStart(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
            document.getParagraphs.get(0).text() shouldBe "To appeal a late payment penalty for Self Assessment, you will need to ask HMRC to look at your case again."
            document.getH2Elements.get(0).text() shouldBe "Before you start"
            document.getParagraphs.get(1).text() shouldBe "You will need:"
            document.getBulletPoints.get(0).text() shouldBe "a reason why the tax payment was not made by the due date (HMRC calls this ‘a reasonable excuse’)"
            document.getBulletPoints.get(1).text() shouldBe "the dates related to this reasonable excuse"
            document.getBulletPoints.get(2).text() shouldBe "details of why you did not appeal sooner"
            document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
            document.getParagraphs.get(3).text() shouldBe "In some cases, you will be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
            document.getParagraphs.get(4).text() shouldBe "If you are not asked for extra evidence, this is because we do not need any to make a decision in your particular case."
            document.getWarningText.get(0).text() shouldBe "Warning If we decide we need extra evidence after reviewing your appeal, we will contact you."
            document.getSubmitButton.text() shouldBe "Continue"
            document.getSubmitButton.attr("href") shouldBe routes.ReasonableExcuseController.onPageLoad(isAgent, NormalMode).url
          }
        }

        "the penalty type is Late Payment Penalty (LPP) (multiple Penalty)" should {
          "render the page with a link to Multiple Appeals page" in {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue

            val result = get(path, isAgent = isAgent)
            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Appeal a Self Assessment penalty - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe English.lppCaptionAppealStart(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
            document.getParagraphs.get(0).text() shouldBe "To appeal a late payment penalty for Self Assessment, you will need to ask HMRC to look at your case again."
            document.getH2Elements.get(0).text() shouldBe "Before you start"
            document.getParagraphs.get(1).text() shouldBe "You will need:"
            document.getBulletPoints.get(0).text() shouldBe "a reason why the tax payment was not made by the due date (HMRC calls this ‘a reasonable excuse’)"
            document.getBulletPoints.get(1).text() shouldBe "the dates related to this reasonable excuse"
            document.getBulletPoints.get(2).text() shouldBe "details of why you did not appeal sooner"
            document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
            document.getParagraphs.get(3).text() shouldBe "In some cases, you will be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
            document.getParagraphs.get(4).text() shouldBe "If you are not asked for extra evidence, this is because we do not need any to make a decision in your particular case."
            document.getWarningText.get(0).text() shouldBe "Warning If we decide we need extra evidence after reviewing your appeal, we will contact you."

            document.getSubmitButton.text() shouldBe "Continue"
            document.getSubmitButton.attr("href") shouldBe routes.JointAppealController.onPageLoad(isAgent, is2ndStageAppeal = false, mode = NormalMode).url
          }
        }
      }

      "the journey is for a 2nd Stage Appeal" when {
        "the penalty type is LPP and there are multiple penalties" should {
          "render the ReviewAppeal page with correct elements" in {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

            val result = get(path, isAgent = isAgent)

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe ReviewAppealStartMessages.English.serviceName
            document.title() shouldBe ReviewAppealStartMessages.English.titleWithSuffix(ReviewAppealStartMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lppCaptionAppealStart(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe ReviewAppealStartMessages.English.headingAndTitle
            document.getParagraphs.get(0).text() shouldBe ReviewAppealStartMessages.English.p1
            document.getParagraphs.get(1).text() shouldBe ReviewAppealStartMessages.English.p2
            document.getH2Elements.get(0).text() shouldBe ReviewAppealStartMessages.English.h2
            document.getParagraphs.get(2).text() shouldBe ReviewAppealStartMessages.English.p3
            document.getParagraphs.get(3).text() shouldBe ReviewAppealStartMessages.English.p4

            document.getSubmitButton.text() shouldBe ReviewAppealStartMessages.English.continue
            document.getSubmitButton.attr("href") shouldBe routes.JointAppealController.onPageLoad(isAgent, is2ndStageAppeal = true, mode = NormalMode).url
          }
        }

        "the penalty type is LPP and there is a single LPP" should {
          "render the ReviewAppeal page with correct elements" in {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLPP2ndStage).futureValue

            val result = get(path, isAgent = isAgent)

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe ReviewAppealStartMessages.English.serviceName
            document.title() shouldBe ReviewAppealStartMessages.English.titleWithSuffix(ReviewAppealStartMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lppCaptionAppealStart(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe ReviewAppealStartMessages.English.headingAndTitle
            document.getParagraphs.get(0).text() shouldBe ReviewAppealStartMessages.English.p1
            document.getParagraphs.get(1).text() shouldBe ReviewAppealStartMessages.English.p2
            document.getH2Elements.get(0).text() shouldBe ReviewAppealStartMessages.English.h2
            document.getParagraphs.get(2).text() shouldBe ReviewAppealStartMessages.English.p3
            document.getParagraphs.get(3).text() shouldBe ReviewAppealStartMessages.English.p4

            document.getSubmitButton.text() shouldBe ReviewAppealStartMessages.English.continue
            document.getSubmitButton.attr("href") shouldBe routes.ReasonableExcuseController.onPageLoad(isAgent, NormalMode).url

          }
        }

        "the penalty type is LSP" should {
          "render the ReviewAppeal page with correct elements" in {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP2ndStage).futureValue

            val result = get(path, isAgent = isAgent)

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe ReviewAppealStartMessages.English.serviceName
            document.title() shouldBe ReviewAppealStartMessages.English.titleWithSuffix(ReviewAppealStartMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe ReviewAppealStartMessages.English.headingAndTitle
            document.getParagraphs.get(0).text() shouldBe ReviewAppealStartMessages.English.p1
            document.getParagraphs.get(1).text() shouldBe ReviewAppealStartMessages.English.p2
            document.getH2Elements.get(0).text() shouldBe ReviewAppealStartMessages.English.h2
            document.getParagraphs.get(2).text() shouldBe ReviewAppealStartMessages.English.p3
            document.getParagraphs.get(3).text() shouldBe ReviewAppealStartMessages.English.p4
            document.getSubmitButton.text() shouldBe ReviewAppealStartMessages.English.continue
            document.getSubmitButton.attr("href") shouldBe {
              if (isAgent) routes.WhoPlannedToSubmitController.onPageLoad(mode = NormalMode).url
              else routes.ReasonableExcuseController.onPageLoad(isAgent, NormalMode).url
            }
          }
        }


        "When isClientResponsibleForSubmission is not empty" should {
          "link to Reasonable Excuse bypassing the Who planned to submit page" in {
            stubAuthRequests(isAgent = true)

            val answer = emptyUserAnswersWithLSP2ndStage.copy().setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
            userAnswersRepo.upsertUserAnswer(answer).futureValue

            val result = get("/agent-appeal-start", isAgent = true)
            val document = Jsoup.parse(result.body)

            document.getSubmitButton.attr("href") shouldBe routes.ReasonableExcuseController.onPageLoad(isAgent = true, NormalMode).url
          }
        }

      }
    }
  }
}
