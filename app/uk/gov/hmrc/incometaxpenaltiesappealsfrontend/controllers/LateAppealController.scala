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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.LateAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.LateAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.LateAppealView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class LateAppealController @Inject()(lateAppeal: LateAppealView,
                                     val authActions: AuthActions,
                                     userAnswersService: UserAnswersService,
                                     override val errorHandler: ErrorHandler,
                                     override val controllerComponents: MessagesControllerComponents
                                    )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers() { implicit user =>
    Ok(lateAppeal(
      form = fillForm(LateAppealForm.form(user.isAppealingMultipleLPPs, user.is2ndStageAppeal), LateAppealPage),
      isLPP = user.isLPP,
      isAppealingMultipleLPPs = user.isAppealingMultipleLPPs,
      isSecondStageAppeal = user.is2ndStageAppeal

    ))
  }

  def submit(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers().async { implicit user =>
    LateAppealForm.form(user.isAppealingMultipleLPPs, user.is2ndStageAppeal).bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(lateAppeal(
          form = formWithErrors,
          isLPP = user.isLPP,
          isAppealingMultipleLPPs = user.isAppealingMultipleLPPs,
          isSecondStageAppeal = user.is2ndStageAppeal))),
      lateAppealReason => {
        val updatedAnswers = user.userAnswers.setAnswer(LateAppealPage, lateAppealReason)
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          Redirect(routes.CheckYourAnswersController.onPageLoad())
        }
      }
    )
  }
}
