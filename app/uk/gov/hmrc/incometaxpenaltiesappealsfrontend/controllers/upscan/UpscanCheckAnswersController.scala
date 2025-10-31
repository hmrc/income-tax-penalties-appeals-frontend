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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CheckMode, Mode, NormalMode}
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

  def onPageLoad(isAgent: Boolean, is2ndStageAppeal: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    withNonEmptyReadyFiles(mode) { files =>
      Ok(nonJsCheckAnswers(
        UploadAnotherFileForm.form(),
        UploadedFilesViewModel(files),
        routes.UpscanCheckAnswersController.onSubmit(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode),
        mode
      ))
    }
  }

  def onSubmit(isAgent: Boolean, is2ndStageAppeal: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    withNonEmptyReadyFiles(mode) { files =>
      if (files.size < appConfig.upscanMaxNumberOfFiles) {
        UploadAnotherFileForm.form().bindFromRequest().fold(
          formWithErrors =>
            BadRequest(nonJsCheckAnswers(
              formWithErrors,
              UploadedFilesViewModel(files),
              routes.UpscanCheckAnswersController.onSubmit(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode),
              mode
            )),
          onwardRoute(_, mode)
        )
      } else {
        onwardRoute(uploadFile = false, mode)
      }
    }
  }

  private def onwardRoute(uploadFile: Boolean, mode: Mode)(implicit user: CurrentUserRequestWithAnswers[_]): Result =
    if(uploadFile) {
      Redirect(routes.UpscanInitiateController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal, mode = mode))
    } else {
      (mode, user.is2ndStageAppeal, user.isLateFirstStage()) match {
        case (CheckMode, _, _) =>
          Redirect(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent))
        case (NormalMode, true, _) =>
          Redirect(appealsRoutes.ReviewMoreThan30DaysController.onPageLoad(isAgent = user.isAgent, mode = mode))
        case (NormalMode, false, true) =>
          Redirect(appealsRoutes.LateAppealController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal, mode))
        case (NormalMode, false, false) =>
          Redirect(appealsRoutes.CheckYourAnswersController.onPageLoad(isAgent = user.isAgent))
      }
    }

  private def withNonEmptyReadyFiles(mode: Mode)(f: Seq[UploadJourney] => Result)
                                    (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    upscanService.getAllReadyFiles(user.journeyId).map {
      case files if files.nonEmpty =>
        f(files)
      case _ =>
        Redirect(routes.UpscanInitiateController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal, mode = mode))
    }
}
