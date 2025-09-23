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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.NormalMode
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.MultipleAppealsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class MultipleAppealsController @Inject()(multipleAppeals: MultipleAppealsView,
                                          val authActions: AuthActions,
                                          override val controllerComponents: MessagesControllerComponents,
                                          override val errorHandler: ErrorHandler
                                            )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean, is2ndStageAppeal: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
      Future(Ok(multipleAppeals(user.isAgent, isSecondStageAppeal = user.is2ndStageAppeal)))
  }

  def submit(isAgent: Boolean, is2ndStageAppeal: Boolean): Action[AnyContent] = authActions.asMTDUser(isAgent) { implicit user  =>
      Redirect(routes.ReasonableExcuseController.onPageLoad(isAgent = user.isAgent, NormalMode))
  }
}
