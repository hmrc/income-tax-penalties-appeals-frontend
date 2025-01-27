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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.BaseUserAnswersController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadDocumentForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadFormFields
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadStatusEnum.{FAILED, READY}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsFileUploadPage
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanInitiateController @Inject()(nonJsUploadPage: NonJsFileUploadPage,
                                         upscanService: UpscanService,
                                         val authorised: AuthAction,
                                         withNavBar: NavBarRetrievalAction,
                                         withAnswers: UserAnswersAction,
                                         actor: ActorSystem,
                                         override val errorHandler: ErrorHandler,
                                         override val controllerComponents: MessagesControllerComponents
                                        )(implicit ec: ExecutionContext, appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(key: Option[String], errorCode: Option[String]): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
    val form = UploadDocumentForm.form
    withFileUploadFormFields(key) { formFields =>
      errorCode match {
        case Some(code) =>
          Future(BadRequest(nonJsUploadPage(form.withError(UploadDocumentForm.key, UploadDocumentForm.synchronousUpscanErrorMessages(code)), formFields)))
        case _ =>
          Future(Ok(nonJsUploadPage(form, formFields)))
      }
    }
  }

  private def withFileUploadFormFields(key: Option[String])(f: UploadFormFields => Future[Result])
                                      (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    key.fold(initiateNewUpload(f)) { fileReference =>
      logger.info(s"[UpscanInitiateController][withFileUploadFormFields] Attempting to retrieve existing form fields for fileReference: $fileReference, journeyId: ${user.journeyId}")
      for {
        existingFormFields <- upscanService.getFormFieldsForFile(user.journeyId, fileReference)
        _ = if(existingFormFields.isEmpty) logger.warn(s"[UpscanInitiateController][withFileUploadFormFields] No existing form fields found for fileReference: $fileReference, journeyId: ${user.journeyId}")
        result <- existingFormFields.fold(initiateNewUpload(f))(f)
      } yield result
    }

  private def initiateNewUpload(f: UploadFormFields => Future[Result])
                               (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] = {
    logger.info(s"[UpscanInitiateController][initiateNewUpload] Initiating new file upload for journeyId: ${user.journeyId}")
    upscanService.initiateNewFileUpload(user.journeyId).flatMap { upscanResponse =>
      f(upscanResponse.uploadRequest)
    }
  }


  def onSubmitSuccessRedirect(key: String): Action[AnyContent] = (authorised andThen withAnswers).async { implicit user =>
    after(appConfig.upscanDelaySuccessRedirect, actor.scheduler) {
      upscanService.getFile(user.journeyId, key).map( _.fold {
        logger.warn(s"[UpscanInitiateController][successRedirect] Redirecting to re-initiate with error message for journeyId: ${user.journeyId}")
        Redirect(routes.UpscanInitiateController.onPageLoad(errorCode = Some("UnableToUpload")).url)
      } { _.fileStatus match {
        case READY  => NotImplemented //TODO: Redirect to Success Page
        case FAILED => NotImplemented //TODO: Redirect to Error Page
        case _      => NotImplemented //TODO: Redirect to "It's taking longer than we expected" page
      }})
    }
  }
}
