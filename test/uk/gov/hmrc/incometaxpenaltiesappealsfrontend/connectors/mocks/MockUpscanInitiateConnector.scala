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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks


import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.ErrorResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.upscan.UpscanInitiateConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}

import scala.concurrent.{ExecutionContext, Future}

trait MockUpscanInitiateConnector {
  this: MockFactory =>

  lazy val mockUpscanInitiateConnector: UpscanInitiateConnector = mock[UpscanInitiateConnector]

  def mockInitiate(journeyId: String, initiateRequest: UpscanInitiateRequest)(response: Future[Either[ErrorResponse, UpscanInitiateResponse]]): Unit =
    (mockUpscanInitiateConnector.initiate(_: String, _: UpscanInitiateRequest)(_: HeaderCarrier, _: ExecutionContext))
      .expects(journeyId, initiateRequest, *, *)
      .returning(response)
}
