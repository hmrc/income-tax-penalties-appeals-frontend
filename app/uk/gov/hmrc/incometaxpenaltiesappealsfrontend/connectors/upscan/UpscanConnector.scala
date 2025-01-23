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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, StringContextOps}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.btaNavBar.NavContent
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanConnector @Inject()(http: HttpClientV2,
                                appConfig: AppConfig) {

  val postInitiateUrl: String = s"${appConfig.upscanInitiateBaseUrl}/upscan/v2/initiate"

  def initiateToUpscan(request: UpscanInitiateRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UpscanInitiateResponse] = {
    httpClient.POST(postInitiateUrl, request)(UpscanInitiateRequest.writes, UpscanInitiateResponseReads, hc, ec)
  }

  def initiateToUpscan(request: UpscanInitiateRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UpscanInitiateResponse] = {
    logger.debug(s"[UpscanConnector][initiateToUpscan] POST to $postInitiateUrl to start File Upload journey, with body:\n" + Json.toJson(request))
    http
      .get(url"$postInitiateUrl")
      .execute[Option[NavContent]]
      .recover {
        case e: Exception =>
          logger.error(s"[BtaNavLinksConnector][getBtaNavLinks] Unexpected Exception of type ${e.getClass.getSimpleName} occurred while retrieving NavLinks from BTA with" +
            s", returning None to gracefully continue without a NavBar being rendered.")
          None
      }
  }
}
