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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventHappenForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Crime, Other, TechnicalIssues, UnexpectedHospital}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.WhenDidEventHappenPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class WhenDidEventHappenController @Inject()(whenDidEventHappen: WhenDidEventHappenView,
                                             val authActions: AuthActions,
                                             userAnswersService: UserAnswersService,
                                             override val controllerComponents: MessagesControllerComponents,
                                             override val errorHandler: ErrorHandler
                                            )(implicit ec: ExecutionContext, val appConfig: AppConfig, timeMachine: TimeMachine) extends BaseUserAnswersController {


  def onPageLoad(reasonableExcuse: ReasonableExcuse, isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    Future(Ok(whenDidEventHappen(
      form = fillForm(WhenDidEventHappenForm.form(reasonableExcuse), WhenDidEventHappenPage),
      reasonableExcuse = reasonableExcuse,
      isLPP = user.isLPP
    )))
  }

  def submit(reasonableExcuse: ReasonableExcuse, isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    WhenDidEventHappenForm.form(reasonableExcuse).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(whenDidEventHappen(
          reasonableExcuse,
          formWithErrors,
          isLPP = user.isLPP
        ))),
      dateOfEvent => {
        val updatedAnswers = user.userAnswers.setAnswer[LocalDate](WhenDidEventHappenPage, dateOfEvent)
        userAnswersService.updateAnswers(updatedAnswers).map { _ =>
          reasonableExcuse match {
            case TechnicalIssues =>
              Redirect(routes.WhenDidEventEndController.onPageLoad(reasonableExcuse, isAgent))
            case Crime =>
              Redirect(routes.CrimeReportedController.onPageLoad(isAgent = user.isAgent))
            case UnexpectedHospital =>
              Redirect(routes.HasHospitalStayEndedController.onPageLoad(isAgent = user.isAgent))
            case Other =>
              Redirect(routes.MissedDeadlineReasonController.onPageLoad(user.isLPP, isAgent, user.is2ndStageAppeal))
            case _ =>
              if (user.isAppealLate()) {
                Redirect(routes.LateAppealController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal))
              } else {
                Redirect(routes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent))
              }
          }
        }
      })

  }
}
