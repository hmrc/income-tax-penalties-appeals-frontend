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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CrimeReportedEnum, NormalMode, ReasonableExcuse, ReviewMoreThan30DaysEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import java.time.LocalDate
import javax.inject.Inject


sealed trait CYAJourneyStep

case class QuestionPage[A](page: Page[A], redirect: Call)(implicit val reads: Reads[A]) extends CYAJourneyStep

case object End extends CYAJourneyStep

case object NoReasonableExcuse extends CYAJourneyStep

case object NoFileUploads extends CYAJourneyStep


abstract class RequiredAnswersJourney(user: CurrentUserRequestWithAnswers[_]) {
  def startPage: CYAJourneyStep

  def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse]): CYAJourneyStep

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

  protected val reasonForMissedDeadline: QuestionPage[String] = QuestionPage(
    MissedDeadlineReasonPage,
    routes.MissedDeadlineReasonController.onPageLoad(user.isLPP, user.isAgent, user.is2ndStageAppeal, NormalMode)
  )(implicitly[Reads[String]])

  protected val reasonableExcuse: QuestionPage[ReasonableExcuse] = QuestionPage(
    ReasonableExcusePage,
    routes.ReasonableExcuseController.onPageLoad(user.isAgent, NormalMode)
  )(implicitly[Reads[ReasonableExcuse]])

  protected val reviewMoreThan30Days: QuestionPage[ReviewMoreThan30DaysEnum.Value] = QuestionPage(
    ReviewMoreThan30DaysPage,
    routes.ReviewMoreThan30DaysController.onPageLoad(user.isAgent, NormalMode)
  )(implicitly[Reads[ReviewMoreThan30DaysEnum.Value]])

  protected val jointAppeal: QuestionPage[Boolean] = QuestionPage(
    JointAppealPage,
    routes.JointAppealController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode)
  )(implicitly[Reads[Boolean]])
}


class RequiredAnswersJourneyValidator@Inject()(implicit tm: TimeMachine, appConfig: AppConfig) {

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
      case NoReasonableExcuse => Incomplete(routes.JointAppealController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode))
      case NoFileUploads => Incomplete(routes.ExtraEvidenceController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode))
    }

    parseJourney(journey.startPage)
  }
}

case class SecondStageAppealJourney(user: CurrentUserRequestWithAnswers[_], fileUploadCount: Int)(implicit val tm: TimeMachine, val appConfig: AppConfig) extends RequiredAnswersJourney(user) {
  override def startPage: CYAJourneyStep = jointAppeal

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
          reasonForMissedDeadline
        } else if (user.isLateFirstStage()) {
          reasonForLateAppeal
        } else {
          End
        }

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

