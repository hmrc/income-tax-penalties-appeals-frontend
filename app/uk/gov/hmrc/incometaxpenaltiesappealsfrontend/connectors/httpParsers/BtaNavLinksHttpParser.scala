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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.btaNavBar.NavContent
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys.{INVALID_JSON_RECEIVED_FROM_BTA, RECEIVED_4XX_FROM_BTA, RECEIVED_5XX_FROM_BTA}

trait BtaNavLinksHttpParser {

  implicit object BtaNavLinksReads extends HttpReads[Option[NavContent]] {
    override def read(method: String, url: String, response: HttpResponse): Option[NavContent] =
      response.status match {
        case OK =>
          logger.debug(s"[BtaNavLinksReads][read] Successful call to retrieve BTA Nav Links. Response: \n\n ${response.json}")
          response.json.validateOpt[NavContent] match {
            case JsSuccess(navLinks, _) =>
              navLinks
            case JsError(errors) =>
              PagerDutyHelper.log("BtaNavLinksHttpParser: BtaNavLinksReads", INVALID_JSON_RECEIVED_FROM_BTA)
              logger.debug(s"[BtaNavLinksReads][read] Failed to parse response from BTA to NavLinks model - failures: $errors")
              logger.error("[BtaNavLinksReads][read] Failed to parse response from BTA to NavLinks model, returning None to continue gracefully")
              None
          }
        case status =>
          PagerDutyHelper.logStatusCode("BtaNavLinksHttpParser: BtaNavLinksReads", status)(RECEIVED_4XX_FROM_BTA, RECEIVED_5XX_FROM_BTA)
          logger.error(s"[BtaNavLinksReads][read] Received unexpected response when calling BTA for NavLinks" +
            s", status code: $status and body: ${response.body}, returning None to continue gracefully")
          None
      }
  }
}

