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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ReasonableExcusesForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CurrentUserRequestWithAnswers, PenaltyData, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.ReasonableExcuseView
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ReasonableExcuseController @Inject()(reasonableExcuse: ReasonableExcuseView,
                                           val authorised: AuthAction,
                                           withNavBar: NavBarRetrievalAction,
                                           withAnswers: UserAnswersAction,
                                           userAnswersService: UserAnswersService,
                                           override val controllerComponents: MessagesControllerComponents,
                                           override val errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    if(user.penaltyData.is2ndStageAppeal) {
      //TODO: This is the current working assumption, that 2nd Stage Appeals will be set to 'Other' by default.
      //      However, an API change may be needed for this to make the Reasonable Excuse optional in the appeal submission
      updateUserAnswersAndRedirect(Other)
    } else {
      renderView(Ok, fillForm(ReasonableExcusesForm.form(), ReasonableExcusePage))
    }
  }

  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    ReasonableExcusesForm.form().bindFromRequest().fold(
      renderView(BadRequest, _),
      updateUserAnswersAndRedirect
    )
  }

  private def renderView(status: Status, form: Form[_])(implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    Future(status(reasonableExcuse(user.isAgent, form)))

  private def updateUserAnswersAndRedirect(newAnswer: ReasonableExcuse)(implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] = {
    val updatedAnswers = user.userAnswers.getAnswer(ReasonableExcusePage) match {
      case Some(existingAnswer) if existingAnswer != newAnswer =>
        user.userAnswers
//          .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, user.penaltyData)
          .removeAppealReasonsData()
          .setAnswer(ReasonableExcusePage, newAnswer)
      case _ =>
        user.userAnswers.setAnswer(ReasonableExcusePage, newAnswer)
    }

    userAnswersService.updateAnswers(updatedAnswers).map { _ =>
      Redirect(routes.HonestyDeclarationController.onPageLoad())
    }
  }
}
