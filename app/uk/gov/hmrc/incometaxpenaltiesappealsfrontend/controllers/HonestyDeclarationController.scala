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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{HonestyDeclarationPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.{AgentHonestyDeclarationView, HonestyDeclarationView, ReviewHonestyDeclarationView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class HonestyDeclarationController @Inject()(honestyDeclaration: HonestyDeclarationView,
                                             reviewHonestyDeclarationView: ReviewHonestyDeclarationView,
                                             agentHonestyDeclarationView: AgentHonestyDeclarationView,
                                             val authActions: AuthActions,
                                             userAnswersService: UserAnswersService,
                                             override val controllerComponents: MessagesControllerComponents,
                                             override val errorHandler: ErrorHandler
                                            )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    withAnswer(ReasonableExcusePage) { reasonableExcuse =>
      Future(Ok(
        if(user.is2ndStageAppeal) reviewHonestyDeclarationView(user.isAgent, reasonableExcuse)
        else if(user.isAgent)agentHonestyDeclarationView(user.isAgent, reasonableExcuse, user.isLPP, user.whoPlannedToSubmit, user.whatCausedYouToMissDeadline)
        else honestyDeclaration(user.isAgent, reasonableExcuse, user.isLPP)))
    }
  }

  def submit(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    val updatedAnswers = user.userAnswers.setAnswer(HonestyDeclarationPage, true)
    userAnswersService.updateAnswers(updatedAnswers).map { _ =>
      Redirect(if(user.is2ndStageAppeal) {
        routes.MissedDeadlineReasonController.onPageLoad(user.isLPP, user.isAgent)
      } else {
        val reasonableExcuse: ReasonableExcuse = user.userAnswers.getAnswer(ReasonableExcusePage).getOrElse(ReasonableExcuse.Other)
        routes.WhenDidEventHappenController.onPageLoad(reasonableExcuse, user.isAgent)
      })
    }
  }
}
