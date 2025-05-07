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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.CrimeReportedForm
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

  def onPageLoad(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers() { implicit user =>
    Ok(hasTheCrimeBeenReported(
      form = fillForm(CrimeReportedForm.form(), CrimeReportedPage),
      isLate = user.isAppealLate(),
      isAgent = user.isAgent
    ))
  }

  def submit(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers().async { implicit user =>
    CrimeReportedForm.form().bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(hasTheCrimeBeenReported(
          form = formWithErrors,
          isLate = user.isAppealLate(),
          isAgent = user.isAgent
        ))),
      value => {
        val updatedAnswers = user.userAnswers.setAnswer(CrimeReportedPage, value)
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          if(user.isAppealLate()) {
            Redirect(routes.LateAppealController.onPageLoad())
          } else {
            Redirect(routes.CheckYourAnswersController.onPageLoad())
          }
        }
      }
    )
  }

}
