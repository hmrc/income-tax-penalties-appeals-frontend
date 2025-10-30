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

import fixtures.messages.{English, HonestyDeclarationMessages}
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, NormalMode, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{HonestyDeclarationPage, ReasonableExcusePage, WhatCausedYouToMissDeadlinePage, WhoPlannedToSubmitPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class HonestyDeclarationControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))


  val reasonsList: List[(ReasonableExcuse, String, String, String, String, String, String)]= List(
  (Bereavement, HonestyDeclarationMessages.English.bereavementMessageLSP, HonestyDeclarationMessages.English.bereavementMessageLPP, HonestyDeclarationMessages.English.agentBereavementMessageLPP, HonestyDeclarationMessages.English.clientPlannedBereavementMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedBereavementMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedBereavementMessageLSP),
  (Cessation, HonestyDeclarationMessages.English.cessationMessageLSP, HonestyDeclarationMessages.English.cessationMessageLPP, HonestyDeclarationMessages.English.agentCessationMessageLPP, HonestyDeclarationMessages.English.clientPlannedCessationMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedCessationMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedCessationMessageLSP),
  (Crime, HonestyDeclarationMessages.English.crimeMessageLSP, HonestyDeclarationMessages.English.crimeMessageLPP, HonestyDeclarationMessages.English.agentCrimeMessageLPP, HonestyDeclarationMessages.English.clientPlannedCrimeMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedCrimeMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedCrimeMessageLSP),
  (FireOrFlood, HonestyDeclarationMessages.English.fireOrFloodReasonMessageLSP, HonestyDeclarationMessages.English.fireOrFloodReasonMessageLPP, HonestyDeclarationMessages.English.agentFireOrFloodReasonMessageLPP, HonestyDeclarationMessages.English.clientPlannedFireOrFloodReasonMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedFireOrFloodReasonMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedFireOrFloodReasonMessageLSP),
  (Health, HonestyDeclarationMessages.English.healthMessageLSP, HonestyDeclarationMessages.English.healthMessageLPP, HonestyDeclarationMessages.English.agentHealthMessageLPP, HonestyDeclarationMessages.English.clientPlannedHealthMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedHealthMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedHealthMessageLSP),
  (TechnicalIssues, HonestyDeclarationMessages.English.technicalIssueMessageLSP, HonestyDeclarationMessages.English.technicalIssueMessageLPP, HonestyDeclarationMessages.English.agentTechnicalIssueMessageLPP, HonestyDeclarationMessages.English.clientPlannedTechnicalIssueMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedTechnicalIssueMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedTechnicalIssueMessageLSP),
  (UnexpectedHospital, HonestyDeclarationMessages.English.unexpectedHospitalMessageLSP, HonestyDeclarationMessages.English.unexpectedHospitalMessageLPP, HonestyDeclarationMessages.English.agentUnexpectedHospitalMessageLPP, HonestyDeclarationMessages.English.clientPlannedUnexpectedHospitalMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedUnexpectedHospitalMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedUnexpectedHospitalMessageLSP),
  (Other, HonestyDeclarationMessages.English.otherMessageLSP, HonestyDeclarationMessages.English.otherMessageLPP, HonestyDeclarationMessages.English.agentOtherMessageLPP, HonestyDeclarationMessages.English.clientPlannedOtherMessageLSP, HonestyDeclarationMessages.English.agentPlannedClientAffectedOtherMessageLSP, HonestyDeclarationMessages.English.agentPlannedAgentAffectedOtherMessageLSP)
  )

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    super.beforeEach()
  }

  for(reason <- reasonsList) {

    val userAnswersWithReasonLSP =
      emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, reason._1)

    val agentPlannedAgentAffectedUserAnswersWithReasonLSP =
      emptyUserAnswersWithLSP
        .setAnswer(ReasonableExcusePage, reason._1)
        .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
        .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.agent)

    val agentPlannedClientAffectedUserAnswersWithReasonLSP =
      emptyUserAnswersWithLSP
        .setAnswer(ReasonableExcusePage, reason._1)
        .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
        .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)

    val clientPlannedAgentUserAnswersWithReasonLSP =
      emptyUserAnswersWithLSP
        .setAnswer(ReasonableExcusePage, reason._1)
        .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)

    val userAnswersWithReasonLPP =
      emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, reason._1)

    val userAnswersWithReason2ndStage =
      emptyUserAnswersWithLSP2ndStage.setAnswer(ReasonableExcusePage, Other)


    s"GET /honesty-declaration with ${reason._1}" should {

      testNavBar(url = "/honesty-declaration") {
        userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue
      }

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

          val result = get("/honesty-declaration")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

          val result = get("/agent-honesty-declaration", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the journey is for a 1st Stage Appeal" when {
        "the page has the correct LSP elements" when {
          "the user is an authorised individual" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

            val result = get("/honesty-declaration")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            else if(reason._1 == UnexpectedHospital){document.getElementById("honestyDeclarationHospital").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHospital}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent - agent planned - agent affected" in {
            stubAuthRequests(true)
            userAnswersRepo.upsertUserAnswer(agentPlannedAgentAffectedUserAnswersWithReasonLSP).futureValue

            val result = get("/agent-honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._7
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent - agent planned - client affected" in {
            stubAuthRequests(true)
            userAnswersRepo.upsertUserAnswer(agentPlannedClientAffectedUserAnswersWithReasonLSP).futureValue

            val result = get("/agent-honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._6
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent - client planned" in {
            stubAuthRequests(true)
            userAnswersRepo.upsertUserAnswer(clientPlannedAgentUserAnswersWithReasonLSP).futureValue

            val result = get("/agent-honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._5
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }
        }

        "the page has the correct LPP elements" when {
          "the user is an authorised individual" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLPP).futureValue

            val result = get("/honesty-declaration")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._3
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            else if(reason._1 == UnexpectedHospital){document.getElementById("honestyDeclarationHospital").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHospital}
            document.getElementById("honestyDeclarationLPP").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationLPP
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent" in {
            stubAuthRequests(true)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLPP).futureValue

            val result = get("/agent-honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._4
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }
        }
      }

      "the journey is for a 2nd Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised individual" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason2ndStage).futureValue

            val result = get("/review-honesty-declaration")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfoReview
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent" in {
            stubAuthRequests(true)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason2ndStage).futureValue

            val result = get("/agent-review-honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfoReview
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }
        }
      }
    }
  }

  for(reason <- reasonsList) {

    val userAnswersWithReason = emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, reason._1)

    s"POST /honesty-declaration" should {

      s"redirect to the WhenDidEventHappen page and add the Declaration flag to UserAnswers with ${reason._1}" in {

      stubAuthRequests(false)
      userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

        val result = post("/honesty-declaration")(Json.obj())

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.WhenDidEventHappenController.onPageLoad(reason._1, isAgent = false, mode = NormalMode).url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(HonestyDeclarationPage)) shouldBe Some(true)
      }
    }
  }
}
