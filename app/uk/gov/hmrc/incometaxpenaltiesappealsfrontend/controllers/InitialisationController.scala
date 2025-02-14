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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.AuthAction
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.MultiplePenaltiesData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AppealData, CurrentUserRequest, PenaltyData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.{AppealService, UserAnswersService}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, UUIDGenerator}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InitialisationController @Inject()(val authorised: AuthAction,
                                         override val controllerComponents: MessagesControllerComponents,
                                         userAnswersService: UserAnswersService,
                                         appealService: AppealService,
                                         errorHandler: ErrorHandler,
                                         uuid: UUIDGenerator
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(penaltyId: String, isLPP: Boolean, isAdditional: Boolean): Action[AnyContent] = authorised.async { implicit user =>
    for {
      appealData <- appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP, isAdditional, user.mtdItId)
      multiPenaltyData <- if (isLPP) appealService.validateMultiplePenaltyDataForEnrolmentKey(penaltyId, user.mtdItId) else Future.successful(None)
      result <- appealData match {
        case Some(data) =>
          storyPenaltyDataAndRedirect(penaltyId, data, multiPenaltyData)
        case None =>
          logger.warn(s"[InitialisationController][onPageLoad] No appeal data found for penaltyId: $penaltyId")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
    } yield result
  }

  private def storyPenaltyDataAndRedirect(penaltyId: String,
                                          appealModel: AppealData,
                                          multiPenaltiesModel: Option[MultiplePenaltiesData] = None)(implicit user: CurrentUserRequest[_]): Future[Result] = {

    val journeyId = uuid.generateUUID
    logger.debug(s"[InitialisationController][onPageLoad] Starting journey for penaltyId: $penaltyId, created journeyId: $journeyId")

    val answers = UserAnswers(journeyId).setAnswerForKey[PenaltyData](
      IncomeTaxSessionKeys.penaltyData,
      PenaltyData(
        penaltyId,
        appealModel,
        multiPenaltiesModel
      )
    )

    userAnswersService.updateAnswers(answers).map { _ =>
      Redirect(routes.AppealStartController.onPageLoad())
        .addingToSession((IncomeTaxSessionKeys.journeyId, journeyId))
    }
  }
}
