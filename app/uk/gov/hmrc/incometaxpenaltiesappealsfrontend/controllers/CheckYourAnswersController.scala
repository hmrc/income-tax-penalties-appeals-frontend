/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.DuplicateAppealError
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{AppealConfirmationPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.{AppealService, UpscanService, UserAnswersService}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.requiredAnswers.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CheckYourAnswersController @Inject()(checkYourAnswers: CheckYourAnswersView,
                                           val authActions: AuthActions,
                                           appealService: AppealService,
                                           upscanService: UpscanService,
                                           requiredAnswersValidator: RequiredAnswersJourneyValidator,
                                           userAnswersService: UserAnswersService,
                                           override val errorHandler: ErrorHandler,
                                           override val controllerComponents: MessagesControllerComponents,
                                          )(implicit ec: ExecutionContext, val appConfig: AppConfig, val tm: TimeMachine) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    upscanService.getAllReadyFiles(user.journeyId).map { uploadedFiles =>
      requiredAnswersValidator.validateJourney(user, uploadedFiles.size) match
        case requiredAnswersValidator.Complete => Ok(checkYourAnswers(uploadedFiles))
        case requiredAnswersValidator.Incomplete(redirect) => Redirect(redirect)
    }
  }


  def submit(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async {
    implicit user => {
      withAnswer(ReasonableExcusePage) { reasonableExcuse =>
        appealService.submitAppeal(reasonableExcuse).flatMap(_.fold(
          status => {
            logger.error(s"[CheckYourAnswersController][submit] Received error status '$status' when submitting appeal for MTDITID: ${user.mtdItId}, journey: ${user.journeyId}")
            status match {
              case _: DuplicateAppealError =>
                Future.successful(Redirect(routes.DuplicateAppealController.onPageLoad(isAgent = user.isAgent)))

              case _ =>
                errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
            }
          },
          _ => {
            val updatedAnswers = user.userAnswers.setAnswer(AppealConfirmationPage, true)
            userAnswersService.updateAnswers(updatedAnswers).map { _ =>
              Redirect(routes.ConfirmationController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal))
            }
          }
        ))
      }
    }
  }
}
