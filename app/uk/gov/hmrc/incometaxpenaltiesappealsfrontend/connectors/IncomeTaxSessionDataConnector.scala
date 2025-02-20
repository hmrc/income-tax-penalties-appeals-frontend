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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors

import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.IncomeTaxSessionDataHttpParser.GetSessionDataResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.{IncomeTaxSessionDataHttpParser, UnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncomeTaxSessionDataConnector @Inject()(httpClient: HttpClientV2,
                                              val appConfig: AppConfig) {

  def getSessionData()(implicit user: CurrentUserRequestWithAnswers[_], hc: HeaderCarrier, ec: ExecutionContext): Future[GetSessionDataResponse] = {

    implicit val reads: HttpReads[GetSessionDataResponse] = IncomeTaxSessionDataHttpParser.reads()

    httpClient
      .get(url"${appConfig.incomeTaxSessionDataBaseUrl}/income-tax-session-data")
      .execute[GetSessionDataResponse]
      .recover {
        case e: Exception =>
          val msg = s"[IncomeTaxSessionDataConnector][getSessionData] Unexpected Exception of type ${e.getClass.getSimpleName} occurred"
          logger.error(msg)
          Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, msg))
      }
  }
}
