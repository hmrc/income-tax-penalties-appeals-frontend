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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

class LateAppealControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {


  "GET /making-a-late-appeal" should {
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/making-a-late-appeal", reasonableExcuse = Some("defaultReason"))

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/making-a-late-appeal", isAgent = true, reasonableExcuse = Some("defaultReason"))

        result.status shouldBe OK
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/making-a-late-appeal", reasonableExcuse = Some("defaultReason"))

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "This penalty point was issued more than 30 days ago - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "This penalty point was issued more than 30 days ago"
        document.getElementById("infoDaysParagraph").text() shouldBe "You usually need to appeal within 30 days of the date on the penalty notice."
        document.getElementsByAttributeValue("for", "delayReason").text() shouldBe "Tell us why you could not appeal within 30 days"
        document.getElementById("delayReason-info").text() shouldBe "You can enter up to 5000 characters"
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/making-a-late-appeal", isAgent = true, reasonableExcuse = Some("defaultReason"))

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "This penalty point was issued more than 30 days ago - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "This penalty point was issued more than 30 days ago"
        document.getElementById("infoDaysParagraph").text() shouldBe "You usually need to appeal within 30 days of the date on the penalty notice."
        document.getElementsByAttributeValue("for", "delayReason").text() shouldBe "Tell us why you could not appeal within 30 days"
        document.getElementById("delayReason-info").text() shouldBe "You can enter up to 5000 characters"
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }
}
