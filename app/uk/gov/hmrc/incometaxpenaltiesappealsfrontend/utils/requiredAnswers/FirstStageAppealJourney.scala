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

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Crime, Other, TechnicalIssues, UnexpectedHospital}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.CYAJourneyStep.*

case class FirstStageAppealJourney(user: CurrentUserRequestWithAnswers[_], fileUploadCount: Int)(implicit val tm: TimeMachine, val appConfig: AppConfig) extends RequiredAnswersJourney(user) {

  override def startPage: CYAJourneyStep = reasonableExcuse

  override def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse]): CYAJourneyStep = {
    currentStep.page match {
      case ReasonableExcusePage => honestyDeclaration
      case HonestyDeclarationPage =>
        reasonableExcuse.map(whenDidEventHappen).getOrElse(NoReasonableExcuse)
      case WhenDidEventHappenPage =>
        reasonableExcuse match {
          case Some(Crime) => hasCrimeBeenReported
          case Some(TechnicalIssues) => whenDidEventEnd(TechnicalIssues)
          case Some(UnexpectedHospital) => hasHospitalStayEnded
          case Some(Other) => reasonForMissedDeadline
          case Some(_) => if (user.isLateFirstStage()) reasonForLateAppeal else End
          case None => NoReasonableExcuse
        }
      case CrimeReportedPage =>
        if (user.isLateFirstStage()) reasonForLateAppeal else End

      case HasHospitalStayEndedPage =>
        if (answer.asInstanceOf[Boolean]) {
          whenDidEventEnd(UnexpectedHospital)
        } else if (user.isLateFirstStage()) {
          reasonForLateAppeal
        } else {
          End
        }

      case WhenDidEventEndPage =>
        reasonableExcuse match
          case Some(_) if user.isLateFirstStage() => reasonForLateAppeal
          case Some(_) => End
          case None => NoReasonableExcuse

      case MissedDeadlineReasonPage => uploadEvidence

      case ExtraEvidencePage =>
        if (answer.asInstanceOf[Boolean] && fileUploadCount < 1)
          NoFileUploads
        else if (user.isLateFirstStage())
          reasonForLateAppeal
        else
          End

      case _ => End
    }
  }
}