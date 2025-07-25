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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.errors.DuplicateAppealView

import javax.inject.Inject

class DuplicateAppealController @Inject()(duplicateAppealView: DuplicateAppealView,
                                          val authActions: AuthActions,
                                          override val errorHandler: ErrorHandler,
                                          override val controllerComponents: MessagesControllerComponents
                                         )(implicit val appConfig: AppConfig) extends BaseUserAnswersController {
  def onPageLoad(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent) { implicit currentUser =>
    Ok(duplicateAppealView())
  }
}