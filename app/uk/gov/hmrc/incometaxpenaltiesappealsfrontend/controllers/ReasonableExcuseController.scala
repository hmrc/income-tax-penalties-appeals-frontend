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


import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ReasonableExcusesForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CheckMode, Mode, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.ReasonableExcuseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ReasonableExcuseController @Inject()(reasonableExcuse: ReasonableExcuseView,
                                           val authActions: AuthActions,
                                           userAnswersService: UserAnswersService,
                                           override val controllerComponents: MessagesControllerComponents,
                                           override val errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean, mode: Mode) = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    if (user.penaltyData.is2ndStageAppeal) {
      //TODO: This is the current working assumption, that 2nd Stage Appeals will be set to 'Other' by default.
      //      However, an API change may be needed for this to make the Reasonable Excuse optional in the appeal submission
      updateUserAnswersAndRedirect(Other, mode)
    } else {
      renderView(Ok, fillForm(ReasonableExcusesForm.form(user.isLPP), ReasonableExcusePage), mode)
    }
  }

  def submit(isAgent: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    ReasonableExcusesForm.form(user.isLPP).bindFromRequest().fold(
      renderView(BadRequest, _, mode),
      updateUserAnswersAndRedirect(_, mode)
    )
  }

  private def renderView(status: Status, form: Form[_], mode: Mode)(implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    Future(status(reasonableExcuse(user.isAgent, form, mode)))

  private def updateUserAnswersAndRedirect(newAnswer: ReasonableExcuse, mode: Mode)(implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] = {
    val maybeExistingAnswer = user.userAnswers.getAnswer(ReasonableExcusePage)
    val updatedAnswers = maybeExistingAnswer match {
      case Some(existingAnswer) if existingAnswer != newAnswer =>
        user.userAnswers
          .removeAppealReasonsData()
          .setAnswer(ReasonableExcusePage, newAnswer)
      case _ =>
        user.userAnswers.setAnswer(ReasonableExcusePage, newAnswer)
    }

    userAnswersService.updateAnswers(updatedAnswers).map { _ =>
      (maybeExistingAnswer.contains(newAnswer), mode) match {
        case (true, CheckMode) => Redirect(routes.CheckYourAnswersController.onPageLoad(user.isAgent))
        case _ => Redirect(routes.HonestyDeclarationController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal))
      }
    }
  }
}
