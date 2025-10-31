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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.internal

import play.api.libs.json.JsValue
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.FailureReasonEnum.INVALID_FILENAME
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{FailureDetails, UploadJourney}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadStatusEnum.FAILED
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class UpscanCallbackController @Inject()(service: UpscanService,
                                         override val controllerComponents: MessagesControllerComponents)
                                        (implicit ec: ExecutionContext) extends FrontendBaseController {

  def callbackFromUpscan(journeyId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[UploadJourney] { callbackModel =>
      service.getFile(journeyId, callbackModel.reference).flatMap {
        case Some(file) =>
          file.failureDetails.map{details =>
            logger.warn(s"[UpscanCallbackController][callbackFromUpscan] Callback from Upscan received with failure details: ${details.failureReason} - ${details.message}")
          }
          val validatedCallbackModel = validateFilename(callbackModel.copy(uploadFields = file.uploadFields))
          service.upsertFileUpload(journeyId, validatedCallbackModel).map { _ =>
            NoContent
          }
        case _ =>
          logger.warn(s"[UpscanCallbackController][callbackFromUpscan] Callback from Upscan received for journeyId: $journeyId, fileReference: ${callbackModel.reference} that does not exist in the File Upload repository")
          Future.successful(Gone)
      }
    }
  }

  private val validFilenameRegex: Regex = "^[a-zA-Z0-9-_.]+$".r

  private[internal] def validateFilename(callbackModel: UploadJourney): UploadJourney =
    callbackModel.uploadDetails.fold(callbackModel) { uploadDetails =>
      validFilenameRegex.findFirstMatchIn(uploadDetails.fileName) match {
        case Some(_) => callbackModel
        case _ =>
          logger.warn(s"[UpscanCallbackController][validateFilename] Callback from Upscan received with invalid filename")
          logger.debug(s"[UpscanCallbackController][validateFilename] Invalid Filename: ${uploadDetails.fileName}")
          callbackModel.copy(
            fileStatus = FAILED,
            failureDetails = Some(FailureDetails(
              INVALID_FILENAME,
              s"Filename contains invalid characters, filename='${uploadDetails.fileName}'"
            ))
          )
      }
    }
}
