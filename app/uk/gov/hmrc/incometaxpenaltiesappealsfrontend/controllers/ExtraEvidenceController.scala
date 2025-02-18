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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ExtraEvidenceForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ExtraEvidencePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.{UpscanService, UserAnswersService}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ExtraEvidenceController @Inject()(extraEvidence: ExtraEvidenceView,
                                        val authorised: AuthAction,
                                        upscanService: UpscanService,
                                        withNavBar: NavBarRetrievalAction,
                                        withAnswers: UserAnswersAction,
                                        userAnswersService: UserAnswersService,
                                        override val errorHandler: ErrorHandler,
                                        override val controllerComponents: MessagesControllerComponents
                                       )(implicit ec: ExecutionContext, timeMachine: TimeMachine, appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers) { implicit user =>
    Ok(extraEvidence(
      form = fillForm(ExtraEvidenceForm.form(), ExtraEvidencePage),
      isLate = user.isAppealLate(),
      isAgent = user.isAgent
    ))
  }

  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    ExtraEvidenceForm.form().bindFromRequest().fold(
      formWithErrors =>
        Future(BadRequest(extraEvidence(
          form = formWithErrors,
          isLate = user.isAppealLate(),
          isAgent = user.isAgent
        ))),
      value => {
        val updatedAnswers = user.userAnswers.setAnswer(ExtraEvidencePage, value)
        userAnswersService.updateAnswers(updatedAnswers).flatMap { _ =>
          if (value) {
            Future(Redirect(controllers.upscan.routes.UpscanCheckAnswersController.onPageLoad()))
          } else {
            upscanService.removeAllFiles(user.journeyId).map(_ =>
              if(user.isAppealLate()) {
                Redirect(routes.LateAppealController.onPageLoad())
              } else {
                Redirect(routes.CheckYourAnswersController.onPageLoad())
              }
            )
          }
        }
      }
    )
  }

}
