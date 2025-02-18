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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import play.api.libs.json.Reads
import play.api.mvc.{Request, WrappedRequest}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Bereavement
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{Page, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import java.time.LocalDate

case class CurrentUserRequestWithAnswers[A](mtdItId: String,
                                            arn: Option[String] = None,
                                            override val navBar: Option[Html] = None,
                                            userAnswers: UserAnswers,
                                            penaltyData: PenaltyData)(implicit val request: Request[A]) extends WrappedRequest[A](request) with RequestWithNavBar {

  val journeyId: String = userAnswers.journeyId
  val isAgent: Boolean = arn.isDefined

  //Penalty Data
  val penaltyNumber: String = penaltyData.penaltyNumber
  val periodStartDate: LocalDate = penaltyData.appealData.startDate
  val periodEndDate: LocalDate = penaltyData.appealData.endDate
  val periodDueDate: LocalDate = penaltyData.appealData.dueDate
  val communicationSent: LocalDate = penaltyData.appealData.dateCommunicationSent
  val isLPP: Boolean = penaltyData.isLPP
  val isAdditional: Boolean = penaltyData.isAdditional

  //Multiple Penalties Data
  val firstPenaltyNumber: Option[String] = penaltyData.multiplePenaltiesData.map(_.firstPenaltyChargeReference)
  val secondPenaltyNumber: Option[String] = penaltyData.multiplePenaltiesData.map(_.secondPenaltyChargeReference)
  val firstPenaltyCommunicationDate: Option[LocalDate] = penaltyData.multiplePenaltiesData.map(_.firstPenaltyCommunicationDate)
  val secondPenaltyCommunicationDate: Option[LocalDate] = penaltyData.multiplePenaltiesData.map(_.secondPenaltyCommunicationDate)

  def getMandatoryAnswer[T](page: Page[T])(implicit reads: Reads[T]): T =
    userAnswers.getAnswer(page) match {
      case Some(value) => value
      case None =>
        logger.error(s"[AppealSubmission][mandatoryAnswer] Missing mandatory answer for page key ${page.key}, mtditid: $mtdItId")
        throw new NoSuchFieldError(s"Missing mandatory answer for page key ${page.key}, mtditid: $mtdItId")
    }

  def lateAppealDays()(implicit appConfig: AppConfig): Int =
    if(userAnswers.getAnswer(ReasonableExcusePage).contains(Bereavement)) appConfig.bereavementLateDays else appConfig.lateDays

  def isAppealLate()(implicit timeMachine: TimeMachine, appConfig: AppConfig): Boolean = {
    val dateWhereLateAppealIsApplicable: LocalDate = timeMachine.getCurrentDate.minusDays(lateAppealDays())

    //TODO: This will be replaced by UserAnswers value in future story when page is built
    if (request.session.get(IncomeTaxSessionKeys.doYouWantToAppealBothPenalties).contains("yes")) {
      firstPenaltyCommunicationDate.exists(_.isBefore(dateWhereLateAppealIsApplicable)) ||
        secondPenaltyCommunicationDate.exists(_.isBefore(dateWhereLateAppealIsApplicable))
    } else {
      communicationSent.isBefore(dateWhereLateAppealIsApplicable)
    }
  }
}

object CurrentUserRequestWithAnswers {
  def apply[A](userAnswers: UserAnswers, penaltyData: PenaltyData)(implicit userRequest: CurrentUserRequest[A]): CurrentUserRequestWithAnswers[A] =
    CurrentUserRequestWithAnswers(userRequest.mtdItId, userRequest.arn, userRequest.navBar, userAnswers, penaltyData)
}
