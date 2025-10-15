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
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.MessageCountConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.MessageCountHttpParser.GetMessageCountResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockMessageCountConnector extends MockFactory {
  _: TestSuite =>

  val mockMessageCountConnector: MessageCountConnector = mock[MessageCountConnector]

  def mockGetMessageCount()(response: Future[GetMessageCountResponse]): Unit =
    (mockMessageCountConnector.getMessageCount()(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *)
      .returning(response)

}
