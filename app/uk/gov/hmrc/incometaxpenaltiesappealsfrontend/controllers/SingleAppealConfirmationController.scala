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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject

class SingleAppealConfirmationController @Inject()(singleAppealConfirmationView: SingleAppealConfirmationView,
                                                   val authActions: AuthActions,
                                                   override val errorHandler: ErrorHandler,
                                                   override val controllerComponents: MessagesControllerComponents) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers() { implicit user =>

    user.penaltyData.multiplePenaltiesData match {
      case Some(multiplePenaltiesData) =>
        Ok(singleAppealConfirmationView(
          isLPP2 = user.isLPP2,
          amount =
            if(user.isLPP2) multiplePenaltiesData.secondPenaltyAmount
            else multiplePenaltiesData.firstPenaltyAmount,
          isSecondStageAppeal = user.is2ndStageAppeal
        ))
      case _ =>
        Redirect(controllers.routes.ReasonableExcuseController.onPageLoad(isAgent = user.isAgent))
    }
  }

  def submit(): Action[AnyContent] = authActions.authoriseAndRetrieve { implicit user  =>
    Redirect(controllers.routes.ReasonableExcuseController.onPageLoad(isAgent = user.isAgent))
  }

}