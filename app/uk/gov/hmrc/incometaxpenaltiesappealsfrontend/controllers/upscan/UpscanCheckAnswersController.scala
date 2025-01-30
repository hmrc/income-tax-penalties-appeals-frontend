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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.BaseUserAnswersController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadAnotherFileForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.UploadedFilesViewModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsUploadCheckAnswersPage
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanCheckAnswersController @Inject()(nonJsCheckAnswersPage: NonJsUploadCheckAnswersPage,
                                             upscanService: UpscanService,
                                             val authorised: AuthAction,
                                             withNavBar: NavBarRetrievalAction,
                                             withAnswers: UserAnswersAction,
                                             override val errorHandler: ErrorHandler,
                                             override val controllerComponents: MessagesControllerComponents
                                            )(implicit ec: ExecutionContext, appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    withNonEmptyReadyFiles { files =>
      Future(Ok(nonJsCheckAnswersPage(
        UploadAnotherFileForm.form(),
        UploadedFilesViewModel(files),
        routes.UpscanCheckAnswersController.onSubmit()
      )))
    }
  }

  def onSubmit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    withNonEmptyReadyFiles { files =>
      if (files.size < appConfig.upscanMaxNumberOfFiles) {
        UploadAnotherFileForm.form().bindFromRequest().fold(
          formWithErrors =>
            Future(BadRequest(nonJsCheckAnswersPage(
              formWithErrors,
              UploadedFilesViewModel(files),
              routes.UpscanCheckAnswersController.onSubmit()
            ))),
          {
            case true =>
              Future(Redirect(routes.UpscanInitiateController.onPageLoad()))
            case false =>
              Future(NotImplemented)
          }
        )
      } else {
        Future(NotImplemented)
      }
    }
  }

  private def withNonEmptyReadyFiles(f: Seq[UploadJourney] => Future[Result])
                                    (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    upscanService.getAllReadyFiles(user.journeyId).flatMap {
      case files if files.nonEmpty =>
        f(files)
      case _ =>
        Future(Redirect(routes.UpscanInitiateController.onPageLoad()))
    }
}
