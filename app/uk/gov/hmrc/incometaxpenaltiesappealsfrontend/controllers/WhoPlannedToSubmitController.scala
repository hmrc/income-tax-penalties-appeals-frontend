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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhoPlannedToSubmitForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, CheckMode, Mode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{WhatCausedYouToMissDeadlinePage, WhoPlannedToSubmitPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class WhoPlannedToSubmitController @Inject()(whoPlannedToSubmit: WhoPlannedToSubmitView,
                                             val authActions: AuthActions,
                                             userAnswersService: UserAnswersService,
                                             override val errorHandler: ErrorHandler,
                                             override val controllerComponents: MessagesControllerComponents
                                            )(implicit ec: ExecutionContext, timeMachine: TimeMachine, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(mode: Mode): Action[AnyContent] = authActions.asMTDAgentWithUserAnswers() { implicit user =>
    Ok(whoPlannedToSubmit(
      fillForm(WhoPlannedToSubmitForm.form(), WhoPlannedToSubmitPage),
      user.isLateFirstStage(),
      user.isAgent,
      mode
    ))
  }


  def submit(mode: Mode): Action[AnyContent] = authActions.asMTDAgentWithUserAnswers().async { implicit user =>
    WhoPlannedToSubmitForm.form().bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(whoPlannedToSubmit(
          formWithErrors,
          user.isLateFirstStage(),
          user.isAgent,
          mode
        ))),
      whoPlannedToSubmit => {
        val answerChanged = user.userAnswers.getAnswer(WhoPlannedToSubmitPage).fold(false)(_ != whoPlannedToSubmit)
        val updatedAnswers = if (answerChanged && whoPlannedToSubmit == AgentClientEnum.client) {
          user.userAnswers.setAnswer(WhoPlannedToSubmitPage, whoPlannedToSubmit)
            .removeAnswer(WhatCausedYouToMissDeadlinePage)
        } else {
          user.userAnswers.setAnswer(WhoPlannedToSubmitPage, whoPlannedToSubmit)
        }
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          whoPlannedToSubmit match {
            case AgentClientEnum.agent =>
              Redirect(routes.WhatCausedYouToMissDeadlineController.onPageLoad(mode))
            case _ if mode == CheckMode =>
              Redirect(routes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent))
            case _ =>
              Redirect(routes.ReasonableExcuseController.onPageLoad(isAgent = user.isAgent, mode))
          }
        }
      }
    )
  }
}
