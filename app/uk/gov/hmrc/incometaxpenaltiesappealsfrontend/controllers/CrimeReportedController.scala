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

import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.CrimeReportedForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CheckMode, Mode, NormalMode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.CrimeReportedPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CrimeReportedController @Inject()(hasTheCrimeBeenReported: HasTheCrimeBeenReportedView,
                                        val authActions: AuthActions,
                                        userAnswersService: UserAnswersService,
                                        override val errorHandler: ErrorHandler,
                                        override val controllerComponents: MessagesControllerComponents
                                       )(implicit ec: ExecutionContext, timeMachine: TimeMachine, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent) { implicit user =>
    Ok(hasTheCrimeBeenReported(
      form = fillForm(CrimeReportedForm.form(), CrimeReportedPage),
      isLate = user.isLateFirstStage(),
      isAgent = user.isAgent,
      mode
    ))
  }

  def submit(isAgent: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    CrimeReportedForm.form().bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(hasTheCrimeBeenReported(
          form = formWithErrors,
          isLate = user.isLateFirstStage(),
          isAgent = user.isAgent,
          mode
        ))),
      value => {
        val updatedAnswers = user.userAnswers.setAnswer(CrimeReportedPage, value)
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          Redirect(nextPage(mode))
        }
      }
    )
  }

  private def nextPage(mode: Mode)(implicit user: CurrentUserRequestWithAnswers[AnyContent]): Call =
    (user.isLateFirstStage(), mode) match {
      case (true, NormalMode) => routes.LateAppealController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal, mode = mode)
      case (false, NormalMode) => routes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent)
      case (_, CheckMode) => routes.CheckYourAnswersController.onPageLoad(user.isAgent)
    }
}
