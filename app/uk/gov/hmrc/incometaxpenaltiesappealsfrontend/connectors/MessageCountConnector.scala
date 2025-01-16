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

import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.MessageCountHttpParser.GetMessageCountResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.MessageCountHttpParser.{GetMessageCountResponse, MessagesCountUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MessageCountConnector @Inject()(httpClient: HttpClientV2,
                                      val appConfig: AppConfig) {
  def getMessageCount()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetMessageCountResponse] = {
    implicit val hcwc: HeaderCarrier = hc.copy(extraHeaders = hc.headers(Seq(play.api.http.HeaderNames.COOKIE)))
    httpClient
      .get(url"${appConfig.messagesFrontendBaseUrl}/messages/count?read=No")(hcwc)
      .execute[GetMessageCountResponse]
      .recover {
        case e: Exception =>
          logger.error(s"[MessageCountConnector][getMessageCount] Unexpected Exception of type ${e.getClass.getSimpleName} occurred while retrieving Message Count from PTA" +
            s", returning Left(ISE)")
          Left(MessagesCountUnexpectedFailure(INTERNAL_SERVER_ERROR))
      }
  }
}
