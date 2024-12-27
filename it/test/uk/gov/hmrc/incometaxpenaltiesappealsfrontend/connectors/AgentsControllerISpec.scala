/*
 * Copyright 2024 HM Revenue & Customs
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

import org.jsoup.Jsoup
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

class AgentsControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {


  "GET /who-planned-to-submit" should {
    "return an OK with a view" when {

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/who-planned-to-submit", isAgent = true)

        result.status shouldBe OK
      }
    }

    "the page has the correct elements" when {

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/who-planned-to-submit", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Before the deadline, who planned to send the submission? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Before the deadline, who planned to submit the return?"
        document.getElementsByAttributeValue("for", "whoPlannedToSubmit").text() shouldBe "I did"
        document.getElementsByAttributeValue("for", "whoPlannedToSubmit-2").text() shouldBe "My client did"
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

  "GET /what-caused-you-to-miss-the-deadline" should {
    "return an OK with a view" when {

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/what-caused-you-to-miss-the-deadline", isAgent = true)

        result.status shouldBe OK
      }
    }

    "the page has the correct elements" when {

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/what-caused-you-to-miss-the-deadline", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "What caused you to miss the deadline? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "What caused you to miss the deadline?"
        document.getElementsByAttributeValue("for", "whatCausedYouToMissTheDeadline").text() shouldBe "My client did not get information to me on time"
        document.getElementsByAttributeValue("for", "whatCausedYouToMissTheDeadline-2").text() shouldBe "Something else happened to delay me"
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

}
