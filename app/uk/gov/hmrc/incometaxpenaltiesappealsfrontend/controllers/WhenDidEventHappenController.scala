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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhenDidEventHappenForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class WhenDidEventHappenController @Inject()(whenDidEventHappen: WhenDidEventHappenView,
                                             val authorised: AuthAction,
                                             withNavBar: NavBarRetrievalAction,
                                             withAnswers: UserAnswersAction,
                                             userAnswersService: UserAnswersService,
                                             override val controllerComponents: MessagesControllerComponents,
                                             override val errorHandler: ErrorHandler
                                            )(implicit ec: ExecutionContext, val appConfig: AppConfig, timeMachine: TimeMachine) extends BaseUserAnswersController {


  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    withAnswer(ReasonableExcusePage) { reasonableExcuse =>
      Future(Ok(whenDidEventHappen(
        form = fillForm(WhenDidEventHappenForm.form(reasonableExcuse), WhenDidEventHappenPage),
        reasonableExcuseMessageKey = reasonableExcuse,
        isLPP = false //TODO: Need to determine this as part of a future story
      )))
    }
  }

  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    withAnswer(ReasonableExcusePage) { reasonableExcuse =>
      WhenDidEventHappenForm.form(reasonableExcuse).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(whenDidEventHappen(
            reasonableExcuse,
            formWithErrors,
            isLPP = false //TODO: Need to determine this as part of a future story
          ))),
        dateOfEvent => {
          val updatedAnswers = user.userAnswers.setAnswer[LocalDate](WhenDidEventHappenPage, dateOfEvent)
          userAnswersService.updateAnswers(updatedAnswers).map { _ =>
            reasonableExcuse match {
              case "technicalIssues" =>
                Redirect(routes.WhenDidEventEndController.onPageLoad())
              case "bereavement" | "fireOrFlood" =>
                Redirect(routes.LateAppealController.onPageLoad())
              case "crime" =>
                Redirect(routes.CrimeReportedController.onPageLoad())
              case _ =>
                Redirect(routes.AppealStartController.onPageLoad())
            }
          }
        })
    }
  }
}
