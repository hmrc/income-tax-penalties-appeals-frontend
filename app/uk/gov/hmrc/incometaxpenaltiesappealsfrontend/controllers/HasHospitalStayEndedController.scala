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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.HasHospitalStayEndedForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{NormalMode, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{HasHospitalStayEndedPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class HasHospitalStayEndedController @Inject()(hospitalStayEnded: HasHospitalStayEndedView,
                                               val authActions: AuthActions,
                                               userAnswersService: UserAnswersService,
                                               override val errorHandler: ErrorHandler,
                                               override val controllerComponents: MessagesControllerComponents
                                              )(implicit ec: ExecutionContext, timeMachine: TimeMachine, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent) { implicit user =>
    Ok(hospitalStayEnded(
      form = fillForm(HasHospitalStayEndedForm.form(), HasHospitalStayEndedPage),
      isLate = user.isAppealLate(),
      isAgent = user.isAgent
    ))
  }

  def submit(isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    HasHospitalStayEndedForm.form().bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(hospitalStayEnded(
          form = formWithErrors,
          isLate = user.isAppealLate(),
          isAgent = user.isAgent
        ))),
      value => {
        val updatedAnswers = user.userAnswers.setAnswer(HasHospitalStayEndedPage, value)
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          if (value) {
            val reasonableExcuse: ReasonableExcuse = user.userAnswers.getAnswer(ReasonableExcusePage).getOrElse(ReasonableExcuse.Other)
            Redirect(routes.WhenDidEventEndController.onPageLoad(reasonableExcuse, user.isAgent, NormalMode))
          } else {
            if (user.isAppealLate()) {
              Redirect(routes.LateAppealController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal, mode = NormalMode))
            } else {
              Redirect(routes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent))
            }
          }
        }
      }
    )
  }

}
