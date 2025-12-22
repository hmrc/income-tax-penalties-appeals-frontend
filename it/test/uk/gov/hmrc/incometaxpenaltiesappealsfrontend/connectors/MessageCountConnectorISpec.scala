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
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.MessageCountHttpParser.{GetMessageCountResponse, MessagesCountResponseBadRequest, MessagesCountResponseMalformed, MessagesCountUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend, UseStubForMessageFrontend}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.messageCount.MessageCount
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.ExecutionContext

class MessageCountConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods with FeatureSwitching {

  val connector: MessageCountConnector = app.injector.instanceOf[MessageCountConnector]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForMessageFrontend)
  }

  "getMessageCount" should {
    "return a successful Right response when the call succeeds and the body can be parsed" when {
      "a message count is returned" in {
        when(GET, uri = s"/messages/count\\?read=No").thenReturn(status = OK, body = MessageCount(1))

        val result: GetMessageCountResponse = await(connector.getMessageCount())

        result shouldBe Right(MessageCount(1))
      }
    }

    "return a Left when" when {

      "invalid Json is returned" in {
        when(GET, uri = s"/messages/count\\?read=No").thenReturn(status = OK, body = "")

        val result = await(connector.getMessageCount())

        result shouldBe Left(MessagesCountResponseMalformed)
      }

      "a 4xx is returned" in {
        when(GET, uri = s"/messages/count\\?read=No").thenReturn(status = BAD_REQUEST, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getMessageCount())
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_MESSAGE_FRONTEND.toString)) shouldBe true
            result shouldBe Left(MessagesCountResponseBadRequest)
          }
        }
      }

      "a 5xx is returned" in {
        when(GET, uri = s"/messages/count\\?read=No").thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getMessageCount())
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_MESSAGE_FRONTEND.toString)) shouldBe true
            result shouldBe Left(MessagesCountUnexpectedFailure(INTERNAL_SERVER_ERROR))
          }
        }
      }
    }
  }
}
