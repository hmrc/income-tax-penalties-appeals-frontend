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

import fixtures.FileUploadFixtures
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.{BadRequest, InvalidJson, UnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.upscan.UpscanInitiateConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.ExecutionContext

class UpscanInitiateConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods
  with FileUploadFixtures {

  val connector: UpscanInitiateConnector = app.injector.instanceOf[UpscanInitiateConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "initiate" when {

    "a success response is returned from upscan" should {

      "return a Right(UpscanResponse)" in {
        when(POST, uri = "/upscan/v2/initiate").thenReturn(status = OK, body = initiateResponse)
        await(connector.initiate(testJourneyId, initiateRequest)) shouldBe Right(initiateResponse)
      }
    }

    "return a Left when" when {

      "invalid Json is returned" in {
        when(POST, uri = "/upscan/v2/initiate").thenReturn(status = OK, body = "")
        await(connector.initiate(testJourneyId, initiateRequest)) shouldBe Left(InvalidJson)
      }

      "a 4xx is returned" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          when(POST, uri = "/upscan/v2/initiate").thenReturn(status = BAD_REQUEST, body = "")
          await(connector.initiate(testJourneyId, initiateRequest)) shouldBe Left(BadRequest)

          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_UPSCAN.toString)) shouldBe true
        }
      }

      "a 5xx is returned" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          when(POST, uri = "/upscan/v2/initiate").thenReturn(status = INTERNAL_SERVER_ERROR, body = "")
          await(connector.initiate(testJourneyId, initiateRequest)) shouldBe
            Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))

          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_UPSCAN.toString)) shouldBe true
        }
      }
    }
  }
}
