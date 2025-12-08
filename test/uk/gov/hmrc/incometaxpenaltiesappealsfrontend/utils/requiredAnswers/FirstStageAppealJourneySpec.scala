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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.CYAJourneyStep.{End, NoFileUploads, NoReasonableExcuse, QuestionPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}

import java.time.LocalDate

class FirstStageAppealJourneySpec extends AnyWordSpec with Matchers with MockFactory
  with BaseFixtures with GuiceOneAppPerSuite {

  private val reasonableExcuse = ReasonableExcuse.Other
  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  class Setup(isAgent: Boolean, isLPP: Boolean, isLate: Boolean = false, fileUploadCount: Int = 1)(implicit val tm: TimeMachine, appConfig: AppConfig) {

    val penaltyData: PenaltyData = if (isLPP) penaltyDataLPP else penaltyDataLSP
    val appealData: AppealData = if (isLPP) latePaymentAppealData else lateSubmissionAppealData
    val userAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData.copy(
        appealData = appealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else timeMachine.getCurrentDate.minusDays(1)
        )
      ))
      .setAnswer(ReasonableExcusePage, ReasonableExcuse.Other)

    val request: CurrentUserRequestWithAnswers[?] = if (isAgent) agentUserRequestWithAnswers(userAnswers) else userRequestWithAnswers(userAnswers)

    val testJourney: FirstStageAppealJourney = FirstStageAppealJourney(request, fileUploadCount)
  }

  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { isLPP =>
      ".next" when {
        s"given the 'reasonable excuse' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'honesty declaration' question page" in new Setup(isAgent, isLPP) {
            private val qPage: QuestionPage[ReasonableExcuse] = QuestionPages.reasonableExcuse(isAgent)
            private val testJourney = FirstStageAppealJourney(request, 1)
            testJourney.next(qPage, reasonableExcuse, None) shouldBe QuestionPages.honestyDeclaration(isAgent)
          }
        }

        s"given the 'honesty declaration' question page, isAgent=$isAgent, isLPP=$isLPP" when {
          "the reasonable excuse has been answered" should {
            "return the 'when did event happen' question page" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.honestyDeclaration(isAgent)
              testJourney.next(qPage, true, Some(reasonableExcuse)) shouldBe QuestionPages.whenDidEventHappen(reasonableExcuse, isAgent)
            }
          }
          "the reasonable excuse has NOT been answered" should {
            "return 'NoReasonableExcuse'" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.honestyDeclaration(isAgent)
              testJourney.next(qPage, true, None) shouldBe NoReasonableExcuse
            }
          }
        }

        s"given the 'when did event happen' question page, isAgent=$isAgent, isLPP=$isLPP" when {
          "the reasonable excuse is 'Crime'" should {
            "return the 'has crime been reported' question page" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.whenDidEventHappen(ReasonableExcuse.Crime, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, Some(ReasonableExcuse.Crime)) shouldBe QuestionPages.hasCrimeBeenReported(isAgent)
            }
          }
          "the reasonable excuse is 'TechnicalIssues'" should {
            "return the 'when did event end' question page" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.whenDidEventHappen(ReasonableExcuse.TechnicalIssues, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, Some(ReasonableExcuse.TechnicalIssues)) shouldBe QuestionPages.whenDidEventEnd(ReasonableExcuse.TechnicalIssues, isAgent)
            }
          }
          "the reasonable excuse is 'UnexpectedHospital'" should {
            "return the 'has hospital stay ended' question page" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.whenDidEventHappen(ReasonableExcuse.UnexpectedHospital, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, Some(ReasonableExcuse.UnexpectedHospital)) shouldBe QuestionPages.hasHospitalStayEnded(isAgent)
            }
          }
          "the reasonable excuse is 'Other'" should {
            "return the 'reason for missed deadline' question page" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.whenDidEventHappen(ReasonableExcuse.Other, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, Some(ReasonableExcuse.Other)) shouldBe QuestionPages.reasonForMissedDeadline(isLPP, isAgent)
            }
          }
          "the reasonable excuse has been answered" should {
            "return the 'reason for late appeal' question page" in new Setup(isAgent, isLPP, isLate = true) {
              val qPage = QuestionPages.whenDidEventHappen(ReasonableExcuse.Bereavement, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, Some(ReasonableExcuse.Bereavement)) shouldBe QuestionPages.reasonForLateAppeal(isAgent)
            }
            "return End" in new Setup(isAgent, isLPP, isLate = false) {
              val qPage = QuestionPages.whenDidEventHappen(ReasonableExcuse.Bereavement, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, Some(ReasonableExcuse.Bereavement)) shouldBe End
            }
          }
          "the reasonable excuse has NOT been answered" should {
            "return 'NoReasonableExcuse'" in new Setup(isAgent, isLPP) {
              val qPage = QuestionPages.whenDidEventHappen(reasonableExcuse, isAgent)
              val answer = LocalDate.now()
              testJourney.next(qPage, answer, None) shouldBe NoReasonableExcuse
            }
          }
        }

        s"given the 'has the crime been reported' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'reason for late appeal' question page" in new Setup(isAgent, isLPP, isLate = true) {
            val qPage = QuestionPages.hasCrimeBeenReported(isAgent)
            testJourney.next(qPage, CrimeReportedEnum.yes, Some(ReasonableExcuse.Crime)) shouldBe QuestionPages.reasonForLateAppeal(isAgent)
          }
          "return End" in new Setup(isAgent, isLPP, isLate = false) {
            val qPage = QuestionPages.hasCrimeBeenReported(isAgent)
            testJourney.next(qPage, CrimeReportedEnum.yes, Some(ReasonableExcuse.Crime)) shouldBe End
          }
        }

        s"given the 'has hospital stay ended' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'when did event end' question page" in new Setup(isAgent, isLPP) {
            val qPage = QuestionPages.hasHospitalStayEnded(isAgent)
            testJourney.next(qPage, true, Some(ReasonableExcuse.UnexpectedHospital)) shouldBe QuestionPages.whenDidEventEnd(ReasonableExcuse.UnexpectedHospital, isAgent)
          }
          "return the 'reason for late appeal' question page" in new Setup(isAgent, isLPP, isLate = true) {
            val qPage = QuestionPages.hasHospitalStayEnded(isAgent)
            testJourney.next(qPage, false, Some(ReasonableExcuse.UnexpectedHospital)) shouldBe QuestionPages.reasonForLateAppeal(isAgent)
          }
          "return End" in new Setup(isAgent, isLPP, isLate = false) {
            val qPage = QuestionPages.hasHospitalStayEnded(isAgent)
            testJourney.next(qPage, false, Some(ReasonableExcuse.UnexpectedHospital)) shouldBe End
          }
        }

        s"given the 'when did event end' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'reason for late appeal' question page" in new Setup(isAgent, isLPP, isLate = true) {
            val qPage = QuestionPages.whenDidEventEnd(ReasonableExcuse.UnexpectedHospital, isAgent)
            val answer = LocalDate.now()
            testJourney.next(qPage, answer, Some(ReasonableExcuse.UnexpectedHospital)) shouldBe QuestionPages.reasonForLateAppeal(isAgent)
          }
          "return 'End' " in new Setup(isAgent, isLPP, isLate = false){
            val qPage = QuestionPages.whenDidEventEnd(ReasonableExcuse.UnexpectedHospital, isAgent)
            val answer = LocalDate.now()
            testJourney.next(qPage, answer, Some(ReasonableExcuse.UnexpectedHospital)) shouldBe End
          }
          "return 'NoReasonableExcuse' " in new Setup(isAgent, isLPP){
            val qPage = QuestionPages.whenDidEventEnd(ReasonableExcuse.UnexpectedHospital, isAgent)
            val answer = LocalDate.now()
            testJourney.next(qPage, answer, None) shouldBe NoReasonableExcuse
          }
        }

        s"given the 'reason for missed deadline' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return the 'upload evidence' question page" in new Setup(isAgent, isLPP, isLate = true) {
            val qPage = QuestionPages.reasonForMissedDeadline(isLPP, isAgent)
            testJourney.next(qPage, "example reason", Some(ReasonableExcuse.Other)) shouldBe QuestionPages.uploadEvidence(isAgent)
          }
        }

        s"given the 'upload extra evidence' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return NoFileUploads" in new Setup(isAgent, isLPP, isLate = true, fileUploadCount = 0) {
            val qPage = QuestionPages.uploadEvidence(isAgent)
            testJourney.next(qPage, true, Some(ReasonableExcuse.Other)) shouldBe NoFileUploads
          }
          "return the 'reason for late appeal' question page" in new Setup(isAgent, isLPP, isLate = true) {
            val qPage = QuestionPages.uploadEvidence(isAgent)
            testJourney.next(qPage, true, Some(ReasonableExcuse.Other)) shouldBe QuestionPages.reasonForLateAppeal(isAgent)
          }
          "return End" in new Setup(isAgent, isLPP, isLate = false) {
            val qPage = QuestionPages.uploadEvidence(isAgent)
            testJourney.next(qPage, true, Some(ReasonableExcuse.Other)) shouldBe End
          }
        }

        s"given the 'reason for late appeal' question page, isAgent= $isAgent, isLPP=$isLPP" should {
          "return End" in new Setup(isAgent, isLPP, isLate = false) {
            val qPage = QuestionPages.reasonForLateAppeal(isAgent)
            testJourney.next(qPage, "example reason", Some(ReasonableExcuse.Other)) shouldBe End
          }
        }
      }
    }
  }


  object QuestionPages {
    private val mode = CheckMode
    private val isSecondStageAppeal = false

    def reasonableExcuse(isAgent: Boolean) = QuestionPage(ReasonableExcusePage, routes.ReasonableExcuseController.onPageLoad(isAgent, mode))

    def honestyDeclaration(isAgent: Boolean) = QuestionPage(HonestyDeclarationPage, routes.HonestyDeclarationController.onPageLoad(isAgent, isSecondStageAppeal))

    def whenDidEventHappen(excuse: ReasonableExcuse, isAgent: Boolean) = QuestionPage(WhenDidEventHappenPage, routes.WhenDidEventHappenController.onPageLoad(excuse, isAgent, mode))

    def hasCrimeBeenReported(isAgent: Boolean) = QuestionPage(CrimeReportedPage, routes.CrimeReportedController.onPageLoad(isAgent, mode))

    def hasHospitalStayEnded(isAgent: Boolean) = QuestionPage(
      HasHospitalStayEndedPage, routes.HasHospitalStayEndedController.onPageLoad(isAgent, mode)
    )

    def uploadEvidence(isAgent: Boolean) = QuestionPage(ExtraEvidencePage, routes.ExtraEvidenceController.onPageLoad(isAgent, isSecondStageAppeal, mode))

    def reasonForMissedDeadline(isLPP: Boolean, isAgent: Boolean) = QuestionPage(MissedDeadlineReasonPage,
      routes.MissedDeadlineReasonController.onPageLoad(isLPP, isAgent, isSecondStageAppeal, mode)
    )

    def reviewMoreThan30Days(isAgent: Boolean) = QuestionPage(ReviewMoreThan30DaysPage, routes.ReviewMoreThan30DaysController.onPageLoad(isAgent, mode))

    def jointAppeal(isAgent: Boolean) = QuestionPage(JointAppealPage, routes.JointAppealController.onPageLoad(isAgent, isSecondStageAppeal, mode))

    def whenDidEventEnd(excuse: ReasonableExcuse, isAgent: Boolean) = QuestionPage(WhenDidEventEndPage, routes.WhenDidEventEndController.onPageLoad(excuse, isAgent, mode))

    def reasonForLateAppeal(isAgent: Boolean) = QuestionPage(LateAppealPage, routes.LateAppealController.onPageLoad(isAgent, isSecondStageAppeal, mode))
  }
}
