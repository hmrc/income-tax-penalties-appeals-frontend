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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}

import java.time.LocalDate

class RequiredAnswersJourneyValidatorSpec extends AnyWordSpec with Matchers with BaseFixtures with GuiceOneAppPerSuite {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  val validator = new RequiredAnswersJourneyValidator()

  class Setup(isLPP: Boolean, isSecondStage: Boolean = false, isLate: Boolean = false)(implicit val tm: TimeMachine, appConfig: AppConfig) {

    val penaltyData: PenaltyData = (isLPP, isSecondStage) match
      case (true, true) => penaltyDataLPP.copy(is2ndStageAppeal = true)
      case (true, false) => penaltyDataLPP
      case (false, true) => penaltyDataLSP.copy(is2ndStageAppeal = true)
      case (false, false) => penaltyDataLSP
    val appealData: AppealData = if (isLPP) latePaymentAppealData else lateSubmissionAppealData
    val userAnswers: UserAnswers = emptyUserAnswers
      .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData.copy(
        appealData = appealData.copy(
          dateCommunicationSent =
            if (isLate) timeMachine.getCurrentDate.minusDays(appConfig.lateDays + 1)
            else timeMachine.getCurrentDate.minusDays(1)
        )
      ))
  }

  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { isLPP =>
      val penaltyType = if (isLPP) "LPP" else "LSP"
      val userType = if (isAgent) "Agent" else "User"
      "validateJourney" when {
        s"$penaltyType - $userType - first stage appeal " should {
          val isSecondStageAppeal = false
          "return Complete" when {
            "all required answers are present for a standard Crime journey" in new Setup(isLPP, isSecondStageAppeal, false) {
              val completeAnswers = userAnswers
                .setAnswer(ReasonableExcusePage, ReasonableExcuse.Crime)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(WhenDidEventHappenPage, LocalDate.now())
                .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)

              val request = if (isAgent) agentUserRequestWithAnswers(completeAnswers) else userRequestWithAnswers(completeAnswers)
              val result = validator.validateJourney(request, 0)

              result shouldBe validator.Complete
            }

            "return Complete for Other reason with Evidence" in new Setup(isLPP, isSecondStageAppeal, false) {
              val completeAnswers = userAnswers
                .setAnswer(ReasonableExcusePage, ReasonableExcuse.Other)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(WhenDidEventHappenPage, LocalDate.now())
                .setAnswer(MissedDeadlineReasonPage, "example reason")
                .setAnswer(ExtraEvidencePage, true)

              val request = if (isAgent) agentUserRequestWithAnswers(completeAnswers) else userRequestWithAnswers(completeAnswers)
              val result = validator.validateJourney(request, 1)
              result shouldBe validator.Complete
            }
          }

          "return Incomplete" when {
            "the user stops after HonestyDeclaration" in new Setup(isLPP, isSecondStageAppeal, false) {
              val incompleteAnswers = userAnswers
                .setAnswer(ReasonableExcusePage, ReasonableExcuse.Crime)
                .setAnswer(HonestyDeclarationPage, true)

              val request = userRequestWithAnswers(incompleteAnswers)

              val result = validator.validateJourney(request, 0)
              result should matchPattern {
                case validator.Incomplete(call) if call.url.contains("when-did-the-crime-happen") =>

              }
            }

            "Reasonable Excuse is missing entirely" in new Setup(isLPP, isSecondStageAppeal, false) {
              val request = userRequestWithAnswers(userAnswers)
              val result = validator.validateJourney(request, 0)
              result should matchPattern {
                case validator.Incomplete(_) =>
              }
            }

            "Logic returns NoReasonableExcuse" in new Setup(isLPP, isSecondStageAppeal, false) {
              val weirdAnswers = userAnswers.setAnswer(HonestyDeclarationPage, true)
              val request = userRequestWithAnswers(weirdAnswers)

              val result = validator.validateJourney(request, 0)
              result should matchPattern {
                case validator.Incomplete(_) =>
              }
            }

            "User answered yes to extra evidence but NO uploaded files" in new Setup(isLPP, isSecondStageAppeal, false) {
              val completeAnswers = userAnswers
                .setAnswer(ReasonableExcusePage, ReasonableExcuse.Other)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(WhenDidEventHappenPage, LocalDate.now())
                .setAnswer(MissedDeadlineReasonPage, "example reason")
                .setAnswer(ExtraEvidencePage, true)

              val request = if (isAgent) agentUserRequestWithAnswers(completeAnswers) else userRequestWithAnswers(completeAnswers)
              val result = validator.validateJourney(request, 0)
              result should matchPattern {
                case validator.Incomplete(_) =>
              }

            }
          }
        }

        s"$penaltyType - $userType - second stage appeal " should {
          val isSecondStageAppeal = true
          "return Complete" when {
            "all required answers are present for a journey" in new Setup(isLPP, isSecondStageAppeal, true) {
              val completeAnswers = userAnswers
                .setAnswer(JointAppealPage, false)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(MissedDeadlineReasonPage, "missed deadline reason")
                .setAnswer(ExtraEvidencePage, true)
                .setAnswer(ReviewMoreThan30DaysPage, ReviewMoreThan30DaysEnum.yes)
                .setAnswer(LateAppealPage, "late appeal reason")

              val request = if (isAgent) agentUserRequestWithAnswers(completeAnswers) else userRequestWithAnswers(completeAnswers)
              val result = validator.validateJourney(request, 1)

              result shouldBe validator.Complete
            }
          }
          "return Incomplete" when {
            "the user has not answered all required questions" in new Setup(isLPP, isSecondStageAppeal, false) {
              val incompleteAnswers = userAnswers
                .setAnswer(JointAppealPage, false)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(MissedDeadlineReasonPage, "missed deadline reason")
                .setAnswer(ExtraEvidencePage, true)
                .setAnswer(ReviewMoreThan30DaysPage, ReviewMoreThan30DaysEnum.yes)

              val request = userRequestWithAnswers(incompleteAnswers)

              val result = validator.validateJourney(request, 1)
              result should matchPattern {
                case validator.Incomplete(call) if call.url.contains("making-a-late-review") =>

              }
            }

            "User answered yes to extra evidence but NO uploaded files" in new Setup(isLPP, isSecondStageAppeal, false) {
              val completeAnswers = userAnswers
                .setAnswer(JointAppealPage, false)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(MissedDeadlineReasonPage, "missed deadline reason")
                .setAnswer(ExtraEvidencePage, true)
                .setAnswer(ReviewMoreThan30DaysPage, ReviewMoreThan30DaysEnum.yes)
                .setAnswer(LateAppealPage, "late appeal reason")

              val request = if (isAgent) agentUserRequestWithAnswers(completeAnswers) else userRequestWithAnswers(completeAnswers)
              val result = validator.validateJourney(request, 0)
              result should matchPattern {
                case validator.Incomplete(_) =>
              }

            }
          }
        }

      }

    }
  }

}
