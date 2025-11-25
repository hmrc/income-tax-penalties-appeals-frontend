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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import play.api.libs.json.Reads
import play.api.mvc.Call
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CrimeReportedEnum, NormalMode, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import java.time.LocalDate


sealed trait JourneyStep

case class QuestionPage[A](page: Page[A], redirect: Call)(implicit val reads: Reads[A]) extends JourneyStep

case object End extends JourneyStep


abstract class RequiredPagesJourney(user: CurrentUserRequestWithAnswers[_]) {
  def startPage: JourneyStep

  def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: ReasonableExcuse): JourneyStep
  
  protected def whenDidEventHappen(reasonableExcuse: ReasonableExcuse): QuestionPage[LocalDate] = QuestionPage(
    WhenDidEventHappenPage,
    routes.WhenDidEventHappenController.onPageLoad(reasonableExcuse, user.isAgent, NormalMode)
  )(implicitly[Reads[LocalDate]])

  protected def whenDidEventEnd(reasonableExcuse: ReasonableExcuse): QuestionPage[LocalDate] = QuestionPage(
    WhenDidEventEndPage,
    routes.WhenDidEventEndController.onPageLoad(reasonableExcuse, user.isAgent, NormalMode)
  )(implicitly[Reads[LocalDate]])

  protected val reasonForLateAppeal: QuestionPage[String] = QuestionPage(
    LateAppealPage,
    routes.LateAppealController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode)
  )(implicitly[Reads[String]])

  protected val honestyDeclaration: QuestionPage[Boolean] = QuestionPage(
    HonestyDeclarationPage,
    routes.HonestyDeclarationController.onPageLoad(user.isAgent, user.is2ndStageAppeal)
  )(implicitly[Reads[Boolean]])

  protected val hasCrimeBeenReported: QuestionPage[CrimeReportedEnum.Value] = QuestionPage(
    CrimeReportedPage,
    routes.CrimeReportedController.onPageLoad(user.isAgent, NormalMode)
  )(implicitly[Reads[CrimeReportedEnum.Value]])

  protected val hasHospitalStayEnded: QuestionPage[Boolean] = QuestionPage(
    HasHospitalStayEndedPage,
    routes.HasHospitalStayEndedController.onPageLoad(user.isAgent, NormalMode)
  )(implicitly[Reads[Boolean]])

  protected val uploadEvidence: QuestionPage[Boolean] = QuestionPage(
    ExtraEvidencePage, routes.ExtraEvidenceController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode)
  )(implicitly[Reads[Boolean]])

  protected val uploadEvidenceCYA: QuestionPage[Boolean] = QuestionPage(
    ExtraEvidencePage, // TODO - create another page for this, save to session - appealReasons.uploadAnotherFile
    upscan.routes.UpscanCheckAnswersController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode)
  )(implicitly[Reads[Boolean]])

  protected val reasonForMissedDeadline: QuestionPage[String] = QuestionPage(
    MissedDeadlineReasonPage,
    routes.MissedDeadlineReasonController.onPageLoad(user.isLPP, user.isAgent, user.is2ndStageAppeal, NormalMode)
  )(implicitly[Reads[String]])

  protected val reasonableExcuse: QuestionPage[ReasonableExcuse] = QuestionPage(
    ReasonableExcusePage,
    routes.ReasonableExcuseController.onPageLoad(user.isAgent, NormalMode)
  )(implicitly[Reads[ReasonableExcuse]])
}


object JourneyValidator {

  sealed trait ValidationResult

  case object Complete extends ValidationResult

  case class Incomplete(redirect: Call) extends ValidationResult

  def validateJourney(user: CurrentUserRequestWithAnswers[_])(implicit tm: TimeMachine, appConfig: AppConfig): ValidationResult = {
    val journey = Journey(user)
    @scala.annotation.tailrec
    def parseJourney(currentStep: JourneyStep): ValidationResult = currentStep match {
      case End => Complete
      case q: QuestionPage[_] =>
        (user.userAnswers.getAnswer(q.page)(q.reads),
          user.userAnswers.getAnswer(ReasonableExcusePage)
        ) match
          case (Some(answer), Some(reason)) =>
            parseJourney(journey.next(q, answer, reason))
          case (None, Some(_)) =>
            Incomplete(q.redirect)
          case _ => Incomplete(routes.ReasonableExcuseController.onPageLoad(user.isAgent, NormalMode))
    }

    if (user.is2ndStageAppeal) Complete else parseJourney(journey.startPage)
  }
}

case class Journey(user: CurrentUserRequestWithAnswers[_])(implicit val tm: TimeMachine, val appConfig: AppConfig) extends RequiredPagesJourney(user) {

  override def startPage: JourneyStep = reasonableExcuse

  override def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: ReasonableExcuse): JourneyStep = {
    currentStep.page match
      case ReasonableExcusePage => honestyDeclaration
      case HonestyDeclarationPage => whenDidEventHappen(reasonableExcuse)
      case WhenDidEventHappenPage =>
        reasonableExcuse match
          case Crime => hasCrimeBeenReported
          case TechnicalIssues => whenDidEventEnd(reasonableExcuse)
          case UnexpectedHospital => hasHospitalStayEnded
          case Other => reasonForMissedDeadline
          case _ => if (user.isLateFirstStage()) reasonForLateAppeal else End

      case CrimeReportedPage => if (user.isLateFirstStage()) reasonForLateAppeal else End
      case HasHospitalStayEndedPage => if (user.isLateFirstStage()) reasonForLateAppeal else End
      case MissedDeadlineReasonPage => uploadEvidence
      case ExtraEvidencePage =>
        if (answer.asInstanceOf[Boolean])
          uploadEvidenceCYA
        else if (user.isLateFirstStage()) reasonForLateAppeal else End
      case _ => End
  }
}

