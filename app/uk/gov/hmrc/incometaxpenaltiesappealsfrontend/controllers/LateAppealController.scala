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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.LateAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.LateAppealPage
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class LateAppealController @Inject()(lateAppeal: LateAppealPage,
                                     val authorised: AuthAction,
                                     withNavBar: NavBarRetrievalAction,
                                     withAnswers: UserAnswersAction,
                                     override val errorHandler: ErrorHandler,
                                     override val controllerComponents: MessagesControllerComponents
                                    )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    withReasonableExcuseAnswer { reasonableExcuse =>
      //TODO: Retrieve current value and populate form on page load as part of next ticket
      Future(Ok(lateAppeal(LateAppealForm.form(), isLate = true, isAgent = user.isAgent, reasonableExcuse)))
    }
  }

  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    LateAppealForm.form().bindFromRequest().fold(
      formWithErrors =>
        withReasonableExcuseAnswer { reasonableExcuse =>
          Future(BadRequest(lateAppeal(formWithErrors, isLate = true, isAgent = user.isAgent, reasonableExcuse)))
        },
      lateAppealReason =>
        //TODO: Store value as part of next ticket
        Future(Redirect(routes.CheckYourAnswersController.onPageLoad()))
    )
  }
}
