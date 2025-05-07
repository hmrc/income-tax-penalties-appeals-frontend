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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventEndForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventEndPage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class WhenDidEventEndController @Inject()(whenDidEventEnd: WhenDidEventEndView,
                                          val authActions: AuthActions,
                                          userAnswersService: UserAnswersService,
                                          override val controllerComponents: MessagesControllerComponents,
                                          override val errorHandler: ErrorHandler
                                         )(implicit ec: ExecutionContext,
                                           val appConfig: AppConfig, timeMachine: TimeMachine) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers().async { implicit user =>
    withAnswer(WhenDidEventHappenPage) { startDate =>
      withAnswer(ReasonableExcusePage) { reasonableExcuse =>
        Future(Ok(whenDidEventEnd(
          form = fillForm(WhenDidEventEndForm.form(reasonableExcuse, startDate), WhenDidEventEndPage),
          isAgent = user.isAgent,
          reasonableExcuse = reasonableExcuse
        )))
      }
    }
  }


  def submit(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers().async { implicit user =>
    withAnswer(WhenDidEventHappenPage) { startDate =>
      withAnswer(ReasonableExcusePage) { reasonableExcuse =>
        WhenDidEventEndForm.form(reasonableExcuse, startDate).bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(whenDidEventEnd(
              user.isAgent,
              reasonableExcuse,
              formWithErrors
            ))),
          dateOfEvent => {
            val updatedAnswers = user.userAnswers.setAnswer(WhenDidEventEndPage, dateOfEvent)
            userAnswersService.updateAnswers(updatedAnswers).map { _ =>
              if(user.isAppealLate()) {
                Redirect(routes.LateAppealController.onPageLoad())
              } else {
                Redirect(routes.CheckYourAnswersController.onPageLoad())
              }
            }
          })
      }
    }
  }
}
