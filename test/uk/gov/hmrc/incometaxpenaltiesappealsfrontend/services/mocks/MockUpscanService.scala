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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks


import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService

import scala.concurrent.Future

trait MockUpscanService extends MockFactory {
  this: TestSuite =>

  val mockUpscanService: UpscanService = mock[UpscanService]

  def mockGetAllReadyFiles(journeyId: String)(response: Future[Seq[UploadJourney]]): CallHandler[Future[Seq[UploadJourney]]] =
    (mockUpscanService.getAllReadyFiles(_: String)).expects(journeyId).returning(response)

}
