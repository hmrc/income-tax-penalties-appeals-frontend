/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers

import fixtures.BaseFixtures
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.CYAJourneyStep.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}

import java.time.LocalDate

class SecondStageAppealJourneySpec extends AnyWordSpec with Matchers with MockFactory with BaseFixtures with GuiceOneAppPerSuite {



  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  class Setup(isAgent: Boolean, isLPP: Boolean, isLate: Boolean = false)(implicit val tm: TimeMachine, appConfig: AppConfig) {

    val penaltyData: PenaltyData = if(isLPP) penaltyDataLPP.copy(is2ndStageAppeal = true) else penaltyDataLSP.copy(is2ndStageAppeal = true)
    val appealData: AppealData = if(isLPP) latePaymentAppealData else lateSubmissionAppealData
    val userAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData.copy(
        appealData = appealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, ReasonableExcuse.Other)

    val request: CurrentUserRequestWithAnswers[?] = if(isAgent) agentUserRequestWithAnswers(userAnswers) else userRequestWithAnswers(userAnswers)
  }

  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { isLPP =>
      ".next" when {
        s"given the joint appeal page question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'honesty declaration' question page" in new Setup(isAgent, isLPP) {
            private val qPage = QuestionPages.jointAppeal(isAgent)
            private val testJourney = SecondStageAppealJourney(request, 1)
            testJourney.next(qPage, true, None) shouldBe QuestionPages.honestyDeclaration(isAgent)
          }
        }

        s"given the honesty declaration question page, isAgent=$isAgent, isLPP=$isLPP" when {
          "the reasonable excuse has been answered" should {
            "return the reason for missed deadline question page" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.honestyDeclaration(isAgent)
              val testJourney = SecondStageAppealJourney(request, 1)
              testJourney.next(qPage, true, None) shouldBe QuestionPages.reasonForMissedDeadline(isLPP, isAgent)
            }
          }
        }

        s"given the reason for missed deadline question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'upload evidence' question page" in new Setup(isAgent, isLPP) {
            val qPage = QuestionPages.reasonForMissedDeadline(isLPP, isAgent)
            val testJourney = SecondStageAppealJourney(request, 1)
            testJourney.next(qPage, "example reason", None) shouldBe QuestionPages.uploadEvidence(isAgent)
          }
        }

        s"given the upload extra evidence question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return NoFileUploads" in new Setup(isAgent, isLPP) {
            val qPage = QuestionPages.uploadEvidence(isAgent)
            val testJourney = SecondStageAppealJourney(request, 0)
            testJourney.next(qPage, true, None) shouldBe NoFileUploads
          }
          "return the 'review more than 30 days' question page" in new Setup(isAgent, isLPP) {
            val qPage = QuestionPages.uploadEvidence(isAgent)
            val testJourney = SecondStageAppealJourney(request, 1)
            testJourney.next(qPage, true, None) shouldBe QuestionPages.reviewMoreThan30Days(isAgent)
          }
        }
        s"given the review more than 30 days question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return 'reason for late appeal' question page" in new Setup(isAgent, isLPP) {
            val qPage = QuestionPages.reviewMoreThan30Days(isAgent)
            val testJourney = SecondStageAppealJourney(request, 1)
            testJourney.next(qPage, ReviewMoreThan30DaysEnum.yes, None) shouldBe QuestionPages.reasonForLateAppeal(isAgent)
          }

          "return End" in new Setup(isAgent, isLPP) {
            val qPage = QuestionPages.reviewMoreThan30Days(isAgent)
            val testJourney = SecondStageAppealJourney(request, 1)
            testJourney.next(qPage, ReviewMoreThan30DaysEnum.no, None) shouldBe End
          }
        }
      }
    }
  }


  object QuestionPages {
    private val mode = CheckMode
    private val isSecondStageAppeal = true

    def honestyDeclaration(isAgent: Boolean) = QuestionPage(HonestyDeclarationPage, routes.HonestyDeclarationController.onPageLoad(isAgent, isSecondStageAppeal))

    def uploadEvidence(isAgent: Boolean) = QuestionPage(ExtraEvidencePage, routes.ExtraEvidenceController.onPageLoad(isAgent, isSecondStageAppeal, mode))

    def reasonForMissedDeadline(isLPP: Boolean, isAgent: Boolean) = QuestionPage(MissedDeadlineReasonPage, routes.MissedDeadlineReasonController.onPageLoad(isLPP, isAgent, isSecondStageAppeal, mode))

    def reviewMoreThan30Days(isAgent: Boolean) = QuestionPage(ReviewMoreThan30DaysPage, routes.ReviewMoreThan30DaysController.onPageLoad(isAgent, mode))

    def jointAppeal(isAgent: Boolean) = QuestionPage(JointAppealPage, routes.JointAppealController.onPageLoad(isAgent, isSecondStageAppeal, mode))

    def reasonForLateAppeal(isAgent: Boolean) = QuestionPage(LateAppealPage, routes.LateAppealController.onPageLoad(isAgent, isSecondStageAppeal, mode))
  }
}