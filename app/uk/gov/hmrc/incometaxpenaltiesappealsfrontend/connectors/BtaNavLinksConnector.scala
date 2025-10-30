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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors

import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.BtaNavLinksHttpParser
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.btaNavBar.NavContent
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BtaNavLinksConnector @Inject()(val http: HttpClientV2,
                                     val config: AppConfig) extends BtaNavLinksHttpParser {

  def getBtaNavLinks()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[NavContent]] = {
    implicit val hcwc: HeaderCarrier = hc.copy(extraHeaders = hc.headers(Seq(play.api.http.HeaderNames.COOKIE)))
    logger.debug(s"[BtaNavLinksConnector][getBtaNavLinks] - Requesting NavLinks from BTA")
    http
      .get(url"${config.btaBaseUrl}/business-account/partial/nav-links")(hcwc)
      .execute[Option[NavContent]]
  }

}
