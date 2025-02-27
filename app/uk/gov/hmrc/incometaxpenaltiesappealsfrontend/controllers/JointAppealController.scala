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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.JointAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{JointAppealPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}
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
                                       )(implicit ec: ExecutionContext, timeMachine: TimeMachine, appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers) { implicit user =>

    user.userAnswers.getAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData) match {
      case Some(penaltyData) =>
        Ok(jointAppeal(
          form = fillForm(JointAppealForm.form(), JointAppealPage),
          isAgent = user.isAgent,
          penaltyData.multiplePenaltiesData.map(_.firstPenaltyAmount.toString).getOrElse(""),
          penaltyData.multiplePenaltiesData.map(_.secondPenaltyAmount.toString).getOrElse("")
        ))
    }



//    val secondPenalty = user.userAnswers.getAnswerForKey[PenaltyData](penaltyData.multiplePenaltiesData.map(_.secondPenaltyAmount).toString)

    Ok(jointAppeal(
      form = fillForm(JointAppealForm.form(), JointAppealPage),
      isAgent = user.isAgent,
      firstPenalty,
      secondPenalty
    ))
  }

  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
//    val firstPenalty = user.userAnswers.getAnswerForKey[PenaltyData](penaltyData.multiplePenaltiesData.map(_.firstPenaltyAmount).toString)
//    val secondPenalty = user.userAnswers.getAnswerForKey[PenaltyData](penaltyData.multiplePenaltiesData.map(_.secondPenaltyAmount).toString)

//    val firstPenalty = user.userAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData).toString
//    val secondPenalty = user.userAnswers.getAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData).toString
    val firstPenalty = user.userAnswers.getAnswerForKey[PenaltyData](penaltyData.multiplePenaltiesData.map(_.firstPenaltyAmount).toString)
    val secondPenalty = user.userAnswers.getAnswerForKey[PenaltyData](penaltyData.multiplePenaltiesData.map(_.secondPenaltyAmount).toString)

    JointAppealForm.form().bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(jointAppeal(
          form = formWithErrors,
          isAgent = user.isAgent,
          firstPenalty,
          secondPenalty
        ))),
      value => {
        val updatedAnswers = user.userAnswers.setAnswer(JointAppealPage, value)
        userAnswersService.updateAnswers(updatedAnswers).flatMap { _ =>
        //TODO: redirect to the single or multiple appeal page
            Future(Redirect(controllers.routes.ReasonableExcuseController.onPageLoad()))

      }
      }
    )
  }

}
