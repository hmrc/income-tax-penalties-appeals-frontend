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


sealed trait JourneyStep

case class QuestionPage[A](page: Page[A], redirect: Call)(implicit val reads: Reads[A]) extends JourneyStep

case object End extends JourneyStep

case object ReasonableExcuseRequired extends JourneyStep


abstract class RequiredPagesJourney(user: CurrentUserRequestWithAnswers[_]) {
  def startPage: JourneyStep

  def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse]): JourneyStep

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


object JourneyValidator {

  sealed trait ValidationResult

  case object Complete extends ValidationResult

  case class Incomplete(redirect: Call) extends ValidationResult

  def validateJourney(user: CurrentUserRequestWithAnswers[_])(implicit tm: TimeMachine, appConfig: AppConfig): ValidationResult = {
    val journey = if (user.is2ndStageAppeal) SecondStageAppealJourney(user) else FirstStageAppealJourney(user)
    @scala.annotation.tailrec
    def parseJourney(currentStep: JourneyStep): ValidationResult = currentStep match {
      case End => Complete
      case q: QuestionPage[_] =>
        val reason = user.userAnswers.getAnswer(ReasonableExcusePage)
        user.userAnswers.getAnswer(q.page)(q.reads) match
          case Some(answer) =>
            parseJourney(journey.next(q, answer, reason))
          case None =>
            Incomplete(q.redirect)
      case ReasonableExcuseRequired => Incomplete(routes.JointAppealController.onPageLoad(user.isAgent, user.is2ndStageAppeal, NormalMode))
    }

    parseJourney(journey.startPage)
  }
}

case class SecondStageAppealJourney(user: CurrentUserRequestWithAnswers[_])(implicit  val tm: TimeMachine, val appConfig: AppConfig) extends RequiredPagesJourney(user){
  override def startPage: JourneyStep = jointAppeal

  override def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse] = None): JourneyStep = {
    currentStep.page match
      case JointAppealPage => honestyDeclaration
      case HonestyDeclarationPage => reasonForMissedDeadline
      case MissedDeadlineReasonPage => uploadEvidence
      case ExtraEvidencePage => reviewMoreThan30Days
      case ReviewMoreThan30DaysPage =>
        if (answer.asInstanceOf[ReviewMoreThan30DaysEnum.Value] == ReviewMoreThan30DaysEnum.yes) reasonForLateAppeal else End
      case LateAppealPage => End
      case _ => End
  }
}

case class FirstStageAppealJourney(user: CurrentUserRequestWithAnswers[_])(implicit val tm: TimeMachine, val appConfig: AppConfig) extends RequiredPagesJourney(user) {

  override def startPage: JourneyStep = reasonableExcuse

  override def next[A](currentStep: QuestionPage[A], answer: A, reasonableExcuse: Option[ReasonableExcuse]): JourneyStep = {
    currentStep.page match
      case ReasonableExcusePage => honestyDeclaration
      case HonestyDeclarationPage =>
        reasonableExcuse.map(whenDidEventHappen).getOrElse(ReasonableExcuseRequired)
      case WhenDidEventHappenPage =>
        reasonableExcuse match {
          case Some(Crime) => hasCrimeBeenReported
          case Some(TechnicalIssues) => whenDidEventEnd(TechnicalIssues)
          case Some(UnexpectedHospital) => hasHospitalStayEnded
          case Some(Other) => reasonForMissedDeadline
          case Some(_) => if (user.isLateFirstStage()) reasonForLateAppeal else End
          case None => ReasonableExcuseRequired
        }

      case CrimeReportedPage => if (user.isLateFirstStage()) reasonForLateAppeal else End
      case HasHospitalStayEndedPage => if (user.isLateFirstStage()) reasonForLateAppeal else End
      case MissedDeadlineReasonPage => uploadEvidence
      case ExtraEvidencePage => if (user.isLateFirstStage()) reasonForLateAppeal else End
      case _ => End
  }
}

