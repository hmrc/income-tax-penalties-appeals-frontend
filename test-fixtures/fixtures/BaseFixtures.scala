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

package fixtures

import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CurrentUserRequest, CurrentUserRequestWithAnswers}

trait BaseFixtures {

  val testMtdItId = "123456789"
  val testJourneyId: String = "journeyId123"
  val emptyUserAnswers: UserAnswers = UserAnswers(testJourneyId)
  def userRequestWithAnswers(userAnswers: UserAnswers): CurrentUserRequestWithAnswers[_] =
    CurrentUserRequestWithAnswers(userAnswers)(CurrentUserRequest(testMtdItId, None)(FakeRequest()))

}
