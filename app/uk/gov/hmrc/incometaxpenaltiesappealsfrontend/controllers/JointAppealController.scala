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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.JointAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.JointAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class JointAppealController @Inject()(jointAppeal: JointAppealView,
                                      val authorised: AuthAction,
                                      withNavBar: NavBarRetrievalAction,
                                      withAnswers: UserAnswersAction,
                                      userAnswersService: UserAnswersService,
                                      override val errorHandler: ErrorHandler,
                                      override val controllerComponents: MessagesControllerComponents
                                     )(implicit ec: ExecutionContext) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers) { implicit user =>

    user.penaltyData.multiplePenaltiesData match {
      case Some(multiplePenaltiesData) =>
        Ok(jointAppeal(
          form = fillForm(JointAppealForm.form(user.is2ndStageAppeal), JointAppealPage),
          isAgent = user.isAgent,
          firstPenaltyAmount = multiplePenaltiesData.firstPenaltyAmount,
          secondPenaltyAmount = multiplePenaltiesData.secondPenaltyAmount,
          isSecondStageAppeal = user.is2ndStageAppeal
        ))
      case _ =>
        Redirect(controllers.routes.ReasonableExcuseController.onPageLoad())
    }
  }

  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>

    JointAppealForm.form(user.is2ndStageAppeal).bindFromRequest().fold(
      formWithErrors => {
        user.penaltyData.multiplePenaltiesData match {
          case Some(multiplePenaltiesData) =>
            Future.successful(BadRequest(jointAppeal(
              form = formWithErrors,
              isAgent = user.isAgent,
              firstPenaltyAmount = multiplePenaltiesData.firstPenaltyAmount,
              secondPenaltyAmount = multiplePenaltiesData.secondPenaltyAmount,
              isSecondStageAppeal = user.is2ndStageAppeal
            )))
          case _ =>
            Future.successful(Redirect(controllers.routes.ReasonableExcuseController.onPageLoad()))
        }
      },
      appealBothPenalties => {
        val updatedAnswers = user.userAnswers.setAnswer(JointAppealPage, appealBothPenalties)
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          Redirect(
            if(appealBothPenalties) routes.MultipleAppealsController.onPageLoad()
            else routes.SingleAppealConfirmationController.onPageLoad()
          )
        }
      }
    )
  }

}
