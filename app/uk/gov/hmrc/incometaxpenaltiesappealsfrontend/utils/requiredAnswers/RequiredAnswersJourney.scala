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

import play.api.libs.json.Reads
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CheckMode, CrimeReportedEnum, ReasonableExcuse, ReviewMoreThan30DaysEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.CYAJourneyStep.*

import java.time.LocalDate

abstract class RequiredAnswersJourney(user: CurrentUserRequestWithAnswers[_]) {
  def startPage: CYAJourneyStep

  def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse]): CYAJourneyStep


  val mode = CheckMode
  protected def whenDidEventHappen(reasonableExcuse: ReasonableExcuse): QuestionPage[LocalDate] = QuestionPage(
    WhenDidEventHappenPage,
    routes.WhenDidEventHappenController.onPageLoad(reasonableExcuse, user.isAgent, mode)
  )(implicitly[Reads[LocalDate]])

  protected def whenDidEventEnd(reasonableExcuse: ReasonableExcuse): QuestionPage[LocalDate] = QuestionPage(
    WhenDidEventEndPage,
    routes.WhenDidEventEndController.onPageLoad(reasonableExcuse, user.isAgent, mode)
  )(implicitly[Reads[LocalDate]])

  protected val reasonForLateAppeal: QuestionPage[String] = QuestionPage(
    LateAppealPage,
    routes.LateAppealController.onPageLoad(user.isAgent, user.is2ndStageAppeal, mode)
  )(implicitly[Reads[String]])

  protected val honestyDeclaration: QuestionPage[Boolean] = QuestionPage(
    HonestyDeclarationPage,
    routes.HonestyDeclarationController.onPageLoad(user.isAgent, user.is2ndStageAppeal)
  )(implicitly[Reads[Boolean]])

  protected val hasCrimeBeenReported: QuestionPage[CrimeReportedEnum.Value] = QuestionPage(
    CrimeReportedPage,
    routes.CrimeReportedController.onPageLoad(user.isAgent, mode)
  )(implicitly[Reads[CrimeReportedEnum.Value]])

  protected val hasHospitalStayEnded: QuestionPage[Boolean] = QuestionPage(
    HasHospitalStayEndedPage,
    routes.HasHospitalStayEndedController.onPageLoad(user.isAgent, mode)
  )(implicitly[Reads[Boolean]])

  protected val uploadEvidence: QuestionPage[Boolean] = QuestionPage(
    ExtraEvidencePage, routes.ExtraEvidenceController.onPageLoad(user.isAgent, user.is2ndStageAppeal, mode)
  )(implicitly[Reads[Boolean]])

  protected val reasonForMissedDeadline: QuestionPage[String] = QuestionPage(
    MissedDeadlineReasonPage,
    routes.MissedDeadlineReasonController.onPageLoad(user.isLPP, user.isAgent, user.is2ndStageAppeal, mode)
  )(implicitly[Reads[String]])

  protected val reasonableExcuse: QuestionPage[ReasonableExcuse] = QuestionPage(
    ReasonableExcusePage,
    routes.ReasonableExcuseController.onPageLoad(user.isAgent, mode)
  )(implicitly[Reads[ReasonableExcuse]])

  protected val reviewMoreThan30Days: QuestionPage[ReviewMoreThan30DaysEnum.Value] = QuestionPage(
    ReviewMoreThan30DaysPage,
    routes.ReviewMoreThan30DaysController.onPageLoad(user.isAgent, mode)
  )(implicitly[Reads[ReviewMoreThan30DaysEnum.Value]])

  protected val jointAppeal: QuestionPage[Boolean] = QuestionPage(
    JointAppealPage,
    routes.JointAppealController.onPageLoad(user.isAgent, user.is2ndStageAppeal, mode)
  )(implicitly[Reads[Boolean]])
}