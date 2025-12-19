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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{ReasonableExcuse, ReviewMoreThan30DaysEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.CYAJourneyStep.*

case class SecondStageAppealJourney(user: CurrentUserRequestWithAnswers[_], fileUploadCount: Int)(implicit val tm: TimeMachine, val appConfig: AppConfig) extends RequiredAnswersJourney(user) {
  override def startPage: CYAJourneyStep = honestyDeclaration

  override def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse] = None): CYAJourneyStep = {
    currentStep.page match
      case JointAppealPage => honestyDeclaration
      case HonestyDeclarationPage => reasonForMissedDeadline
      case MissedDeadlineReasonPage => uploadEvidence
      case ExtraEvidencePage =>
        if (answer.asInstanceOf[Boolean] && fileUploadCount < 1)
          NoFileUploads
        else
          reviewMoreThan30Days
      case ReviewMoreThan30DaysPage =>
        if (answer.asInstanceOf[ReviewMoreThan30DaysEnum.Value] == ReviewMoreThan30DaysEnum.yes) reasonForLateAppeal else End
      case LateAppealPage => End
      case _ => End
  }
}
