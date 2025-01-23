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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanCallbackController @Inject()(service: UpscanService,
                                         override val controllerComponents: MessagesControllerComponents)
                                        (implicit ec: ExecutionContext) extends FrontendBaseController {

  def callbackFromUpscan(journeyId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[UploadJourney] { callbackModel =>
      service.getFile(journeyId, callbackModel.reference).flatMap {
        case Some(file) =>
          service.upsertFileUpload(journeyId, callbackModel.copy(uploadFields = file.uploadFields)).map { _ =>
            NoContent
          }
        case _ =>
          logger.warn(s"[UpscanCallbackController][callbackFromUpscan] Callback from Upscan received for journeyId: $journeyId, fileReference: ${callbackModel.reference} that does not exist in the File Upload repository")
          Future.successful(Gone)
      }
    }
  }
}
