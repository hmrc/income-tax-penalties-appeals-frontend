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

import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService

import scala.concurrent.Future

trait MockSessionService extends MockFactory { _: TestSuite =>

  val mockSessionService: UserAnswersService = mock[UserAnswersService]

  def mockGetUserAnswers(journeyId: String)(response: Future[Option[UserAnswers]]): Unit =
    (mockSessionService.getUserAnswers(_: String)).expects(journeyId).returning(response)

  def mockGetUserAnswersMongoFailure(journeyId: String): Unit =
    (mockSessionService.getUserAnswers(_:String)).expects(journeyId).returning(Future.failed(new Exception("error")))

}
