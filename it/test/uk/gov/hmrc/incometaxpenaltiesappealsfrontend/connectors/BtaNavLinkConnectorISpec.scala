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

import fixtures.BtaNavContentFixture
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.ExecutionContext

class BtaNavLinkConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods with BtaNavContentFixture {

  val connector: BtaNavLinksConnector = app.injector.instanceOf[BtaNavLinksConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "getBtaNavLinks" should {
    "return Some(NavContent)" when {
      "a successful response when the call succeeds and the body can be parsed" in {
        when(GET, uri = "/business-account/partial/nav-links").thenReturn(status = OK, body = btaNavContent)
        await(connector.getBtaNavLinks()) shouldBe Some(btaNavContent)
      }
    }

    "return None" when {
      "a successful response with no body is returned" in {
        when(GET, uri = "/business-account/partial/nav-links").thenReturn(status = OK, body = "")
        await(connector.getBtaNavLinks()) shouldBe None
      }

      "a 4xx is returned" in {
        when(GET, uri = "/business-account/partial/nav-links").thenReturn(status = BAD_REQUEST, body = Json.obj())
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getBtaNavLinks())
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_BTA.toString)) shouldBe true
            result shouldBe None
          }
        }
      }

      "a 5xx is returned" in {
        when(GET, uri = "/business-account/partial/nav-links").thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getBtaNavLinks())
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_BTA.toString)) shouldBe true
            result shouldBe None
          }
        }
      }
    }
  }
}
