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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadDocumentForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadStatusEnum.{FAILED, READY, WAITING}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{UploadFormFields, UploadJourney}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsFileUploadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanInitiateController @Inject()(nonJsFileUpload: NonJsFileUploadView,
                                         upscanService: UpscanService,
                                         val authActions: AuthActions,
                                         actor: ActorSystem,
                                         override val errorHandler: ErrorHandler,
                                         override val controllerComponents: MessagesControllerComponents
                                        )(implicit ec: ExecutionContext, appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(key: Option[String], errorCode: Option[String], isAgent: Boolean, is2ndStageAppeal: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    val form = UploadDocumentForm.form
    withFileUploadFormFields(key) { formFields =>
      errorCode match {
        case Some(code) =>
          Future(BadRequest(nonJsFileUpload(form.withError(UploadDocumentForm.key, UploadDocumentForm.errorMessages(code)), formFields)))
        case _ =>
          Future(Ok(nonJsFileUpload(form, formFields)))
      }
    }
  }

  private def withFileUploadFormFields(key: Option[String])(f: UploadFormFields => Future[Result])
                                      (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] =
    key.fold(initiateNewUpload(f)) { fileReference =>
      logger.info(s"[UpscanInitiateController][withFileUploadFormFields] Attempting to retrieve existing form fields for fileReference: $fileReference, journeyId: ${user.journeyId}")
      for {
        existingFormFields <- upscanService.reinitialiseFileAndReturnFormFields(user.journeyId, fileReference)
        _ = if(existingFormFields.isEmpty) logger.warn(s"[UpscanInitiateController][withFileUploadFormFields] No existing form fields found for fileReference: $fileReference, journeyId: ${user.journeyId}")
        result <- existingFormFields.fold(initiateNewUpload(f))(f)
      } yield result
    }

  private def initiateNewUpload(f: UploadFormFields => Future[Result])
                               (implicit user: CurrentUserRequestWithAnswers[_]): Future[Result] = {
    logger.info(s"[UpscanInitiateController][initiateNewUpload] Initiating new file upload for journeyId: ${user.journeyId}")
    upscanService.initiateNewFileUpload(user.journeyId, user.isAgent, user.is2ndStageAppeal).flatMap { upscanResponse =>
      f(upscanResponse.uploadRequest)
    }
  }


  def onSubmitSuccessRedirect(key: String, isAgent: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    waitForUpscanResponse(user.isAgent, user.is2ndStageAppeal, user.journeyId, key) { uploadJourney =>
      (uploadJourney.fileStatus, uploadJourney.failureDetails) match {
        case (READY, _) =>
          Future.successful(Redirect(routes.UpscanCheckAnswersController.onPageLoad(isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal)))
        case (FAILED, Some(error)) =>
          Future.successful(Redirect(routes.UpscanInitiateController.onPageLoad(Some(key), Some(error.failureReason.toString), isAgent = user.isAgent, is2ndStageAppeal = user.is2ndStageAppeal)))
        case _ =>
          Future.successful(NotImplemented) //TODO: Redirect to "It's taking longer than we expected" page
      }
    }
  }

  private def waitForUpscanResponse(isAgent: Boolean, is2ndStageAppeal: Boolean, journeyId: String, fileReference: String, startTime: Long = System.currentTimeMillis())
                                   (f: UploadJourney => Future[Result]): Future[Result] =
    after(appConfig.upscanCheckInterval, actor.scheduler) {
      upscanService.getFile(journeyId, fileReference).flatMap(_.fold {
        logger.warn(s"[UpscanInitiateController][waitForUpscanResponse] Redirecting to re-initiate with error message for journeyId: $journeyId, fileReference: $fileReference")
        Future(Redirect(routes.UpscanInitiateController.onPageLoad(errorCode = Some("UnableToUpload"), isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal).url))
      } { uploadJourney =>
        uploadJourney.fileStatus match {
          case WAITING if (System.currentTimeMillis() - startTime) <= appConfig.upscanTimeout.toMillis =>
            waitForUpscanResponse(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, journeyId, fileReference, startTime)(f)
          case status =>
            if(status == WAITING) {
              logger.warn(s"[UpscanInitiateController][waitForUpscanResponse] Upscan file was still $status after ${appConfig.upscanTimeout.toMillis}ms for journeyId: $journeyId, fileReference: $fileReference")
            } else {
              logger.info(s"[UpscanInitiateController][waitForUpscanResponse] Upscan file status is $status after ${System.currentTimeMillis() - startTime}ms for journeyId: $journeyId, fileReference: $fileReference")
            }
            f(uploadJourney)
        }})
    }
}
