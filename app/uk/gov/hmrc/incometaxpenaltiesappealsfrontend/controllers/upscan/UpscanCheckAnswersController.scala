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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.upscan

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.{BaseUserAnswersController, routes => appealsRoutes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadAnotherFileForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.UploadedFilesViewModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsUploadCheckAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanCheckAnswersController @Inject()(nonJsCheckAnswers: NonJsUploadCheckAnswersView,
                                             upscanService: UpscanService,
                                             val authActions: AuthActions,
                                             override val errorHandler: ErrorHandler,
                                             override val controllerComponents: MessagesControllerComponents
                                            )(implicit ec: ExecutionContext, appConfig: AppConfig, timeMachine: TimeMachine) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean, is2ndStageAppeal: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    withNonEmptyReadyFiles { files =>
      Ok(nonJsCheckAnswers(
        UploadAnotherFileForm.form(),
        UploadedFilesViewModel(files),
        routes.UpscanCheckAnswersController.onSubmit(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal)
      ))
    }
  }

  def onSubmit(isAgent: Boolean, is2ndStageAppeal: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    withNonEmptyReadyFiles { files =>
      if (files.size < appConfig.upscanMaxNumberOfFiles) {
        UploadAnotherFileForm.form().bindFromRequest().fold(
          formWithErrors =>
            BadRequest(nonJsCheckAnswers(
              formWithErrors,
              UploadedFilesViewModel(files),
              routes.UpscanCheckAnswersController.onSubmit(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal)
            )),
          onwardRoute(_)
        )
      } else {
        onwardRoute(uploadFile = false)
      }
    }
  }

  private def onwardRoute(uploadFile: Boolean)(implicit user: CurrentUserRequestWithAnswers[_]): Result =
    if(uploadFile) {
      Redirect(routes.UpscanInitiateController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal))
    } else {
      if (user.isAppealLate()) {
        Redirect(appealsRoutes.LateAppealController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal))
      } else {
        Redirect(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent))
      }
    }

  private def withNonEmptyReadyFiles(f: Seq[UploadJourney] => Result)
                                    (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    upscanService.getAllReadyFiles(user.journeyId).map {
      case files if files.nonEmpty =>
        f(files)
      case _ =>
        Redirect(routes.UpscanInitiateController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal))
    }
}
