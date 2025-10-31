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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.upscan

import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.{ErrorResponse, UpscanInitiateHttpParser}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.ExceptionHandlingUtil
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class UpscanInitiateConnector @Inject()(http: HttpClientV2,
                                        appConfig: AppConfig) extends ExceptionHandlingUtil with UpscanInitiateHttpParser {

  private lazy val postInitiateUrl: String = s"${appConfig.upscanInitiateBaseUrl}/upscan/v2/initiate"

  def initiate(journeyId: String, request: UpscanInitiateRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorResponse, UpscanInitiateResponse]] =
    withExceptionHandling("initiate", Map("journeyId" -> journeyId)) {
      logger.debug(s"[UpscanConnector][initiate] POST to $postInitiateUrl to start File Upload journey, for journeyId: $journeyId with body:\n" + Json.toJson(request))
      implicit val reads: HttpReads[Either[ErrorResponse, UpscanInitiateResponse]] = read(journeyId)
      http
        .post(url"$postInitiateUrl")
        .withBody(Json.toJson(request))
        .execute[Either[ErrorResponse, UpscanInitiateResponse]]
    }
}
