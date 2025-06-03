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

import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.ExecutionContext

class IncomeTaxSessionDataConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods with FeatureSwitching {

  val connector: IncomeTaxSessionDataConnector = app.injector.instanceOf[IncomeTaxSessionDataConnector]
  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit val user: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "getSessionData()" should {

    "return a successful Right response when the call succeeds and the body can be parsed" when {

      "a Session Data model is returned" in {
        when(GET, uri = "/income-tax-session-data").thenReturn(status = OK, body = sessionData)
        await(connector.getSessionData()) shouldBe Right(Some(sessionData))
      }

      "a NOT_FOUND was returned, so return Right(None)" in {
        when(GET, uri = "/income-tax-session-data").thenReturn(status = NOT_FOUND, body = Json.obj())
        await(connector.getSessionData()) shouldBe Right(None)
      }
    }

    "return a Left when" when {

      "invalid Json is returned" in {
        when(GET, uri = "/income-tax-session-data").thenReturn(status = OK, body = Json.obj())
        await(connector.getSessionData()) shouldBe Left(InvalidJson)
      }

      "a 4xx is returned" in {
        when(GET, uri = "/income-tax-session-data").thenReturn(status = BAD_REQUEST, body = Json.obj())
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getSessionData())
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_INCOME_TAX_SESSION_DATA.toString)) shouldBe true
            result shouldBe Left(BadRequest)
          }
        }
      }

      "a 5xx is returned" in {
        when(GET, uri = "/income-tax-session-data").thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getSessionData())
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_INCOME_TAX_SESSION_DATA.toString)) shouldBe true
            result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "{}"))
          }
        }
      }
    }
  }
}
