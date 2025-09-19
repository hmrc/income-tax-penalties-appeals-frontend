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

import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.{BaseUserAnswersController, routes => appealsRouts}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadRemoveFileForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.Mode
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.UploadedFilesViewModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsRemoveFileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanRemoveFileController @Inject()(nonJsRemoveFile: NonJsRemoveFileView,
                                           upscanService: UpscanService,
                                           val authActions: AuthActions,
                                           override val errorHandler: ErrorHandler,
                                           override val controllerComponents: MessagesControllerComponents
                                          )(implicit ec: ExecutionContext) extends BaseUserAnswersController {

  def onPageLoad(fileReference: String, index: Int, isAgent: Boolean, is2ndStageAppeal: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    renderView(Ok, UploadRemoveFileForm.form(), fileReference, index, mode)
  }


  def onSubmit(fileReference: String, index: Int, isAgent: Boolean, is2ndStageAppeal: Boolean, mode: Mode): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    UploadRemoveFileForm.form().bindFromRequest().fold(
      renderView(BadRequest, _, fileReference, index, mode), {
        case true =>
          for {
            _ <- upscanService.removeFile(user.journeyId, fileReference)
            count <- upscanService.countAllReadyFiles(user.journeyId)
          } yield Redirect(
            if(count > 0) routes.UpscanCheckAnswersController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode)
            else          appealsRouts.ExtraEvidenceController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode)
          )
        case false =>
          Future(Redirect(routes.UpscanCheckAnswersController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode)))
      }
    )
  }

  private def renderView(status: Status, form: Form[_], fileReference: String, index: Int, mode: Mode)(implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    upscanService.getFile(user.journeyId, fileReference).map(_.flatMap(UploadedFilesViewModel(_, index))).map {
      case Some(viewModel) =>
        status(nonJsRemoveFile(form, viewModel, routes.UpscanRemoveFileController.onSubmit(fileReference, index, user.isAgent, user.is2ndStageAppeal, mode = mode)))
      case _ =>
        logger.info("[UpscanRemoveFileController][onPageLoad] User attempted to remove a file that does not exist, bouncing back to Upscan Check Answers page")
        Redirect(routes.UpscanCheckAnswersController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal, mode = mode))
    }
}
