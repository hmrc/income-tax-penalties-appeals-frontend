/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.internal.{routes => internalRoutes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.upscan.{routes => upscanRoutes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.Mode

case class UpscanInitiateRequest(callbackUrl: String,
                                 successRedirect: Option[String] = None,
                                 errorRedirect: Option[String] = None,
                                 minimumFileSize: Option[Int] = None,
                                 maximumFileSize: Option[Int] = None)

object UpscanInitiateRequest {

  implicit val writes: Writes[UpscanInitiateRequest] = Json.writes[UpscanInitiateRequest]

  def apply(journeyId: String, appConfig: AppConfig, isAgent: Boolean, is2ndStageAppeal: Boolean, mode: Mode): UpscanInitiateRequest = UpscanInitiateRequest(
    callbackUrl     = appConfig.upscanCallbackBaseUrl + internalRoutes.UpscanCallbackController.callbackFromUpscan(journeyId).url,
    successRedirect = Some(appConfig.host + upscanRoutes.UpscanInitiateController.onSubmitSuccessRedirect("", isAgent, mode = mode).url.replace("?key=", "")),
    errorRedirect   = Some(appConfig.host + upscanRoutes.UpscanInitiateController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode).url),
    minimumFileSize = Some(appConfig.upscanMinFileSize),
    maximumFileSize = Some(appConfig.upscanMaxFileSize)
  )
}
