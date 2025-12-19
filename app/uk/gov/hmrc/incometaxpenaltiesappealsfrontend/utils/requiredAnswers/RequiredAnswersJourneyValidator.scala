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

import play.api.mvc.Call
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.NormalMode
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.CYAJourneyStep.*

import javax.inject.Inject

class RequiredAnswersJourneyValidator @Inject()(implicit tm: TimeMachine, appConfig: AppConfig) {

  sealed trait ValidationResult

  case object Complete extends ValidationResult

  case class Incomplete(redirect: Call) extends ValidationResult

  def validateJourney(user: CurrentUserRequestWithAnswers[_], uploadedFileCount: Int): ValidationResult = {
    val journey = if (user.is2ndStageAppeal) SecondStageAppealJourney(user, uploadedFileCount) else FirstStageAppealJourney(user, uploadedFileCount)

    @scala.annotation.tailrec
    def parseJourney(currentStep: CYAJourneyStep): ValidationResult = currentStep match {
      case End => Complete
      case q: QuestionPage[_] =>
        val reason = user.userAnswers.getAnswer(ReasonableExcusePage)
        user.userAnswers.getAnswer(q.page)(q.reads) match
          case Some(answer) =>
            parseJourney(journey.next(q, answer, reason))
          case None =>
            Incomplete(q.redirect)
      case NoReasonableExcuse => Incomplete(routes.ReasonableExcuseController.onPageLoad(user.isAgent, NormalMode))
      case NoFileUploads => Incomplete(routes.ExtraEvidenceController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode))
    }

    parseJourney(journey.startPage)
  }
}
