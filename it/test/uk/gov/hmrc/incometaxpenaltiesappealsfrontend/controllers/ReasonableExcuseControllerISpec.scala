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

class ReasonableExcuseControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {


  "GET /reason-for-missing-deadline" should {
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/reason-for-missing-deadline")
        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/reason-for-missing-deadline", isAgent = true)

        result.status shouldBe OK
      }
    }
    "have the correct page has correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/reason-for-missing-deadline")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "What was the reason for missing the submission deadline? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
        document.getHintText.get(0).text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your ability to meet the deadline."
        document.getElementsByAttributeValue("for", "reasonableExcuse").text() shouldBe "Bereavement (someone died)"
        document.getElementsByAttributeValue("for", "reasonableExcuse-2").text() shouldBe "Cessation of income source"
        document.getElementsByAttributeValue("for", "reasonableExcuse-3").text() shouldBe "Crime"
        document.getElementsByAttributeValue("for", "reasonableExcuse-4").text() shouldBe "Fire or flood"
        document.getElementsByAttributeValue("for", "reasonableExcuse-5").text() shouldBe "Serious or life-threatening ill health"
        document.getElementsByAttributeValue("for", "reasonableExcuse-6").text() shouldBe "Technology issues"
        document.getElementsByAttributeValue("for", "reasonableExcuse-7").text() shouldBe "Unexpected hospital stay"
        document.getElementsByAttributeValue("for", "reasonableExcuse-9").text() shouldBe "The reason does not fit into any of the other categories"
        document.getHintText.get(1).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/reason-for-missing-deadline", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "What was the reason for missing the submission deadline? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
        document.getHintText.get(0).text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your ability to meet the deadline."
        document.getElementsByAttributeValue("for", "reasonableExcuse").text() shouldBe "Bereavement (someone died)"
        document.getElementsByAttributeValue("for", "reasonableExcuse-2").text() shouldBe "Cessation of income source"
        document.getElementsByAttributeValue("for", "reasonableExcuse-3").text() shouldBe "Crime"
        document.getElementsByAttributeValue("for", "reasonableExcuse-4").text() shouldBe "Fire or flood"
        document.getElementsByAttributeValue("for", "reasonableExcuse-5").text() shouldBe "Serious or life-threatening ill health"
        document.getElementsByAttributeValue("for", "reasonableExcuse-6").text() shouldBe "Technology issues"
        document.getElementsByAttributeValue("for", "reasonableExcuse-7").text() shouldBe "Unexpected hospital stay"
        document.getElementsByAttributeValue("for", "reasonableExcuse-9").text() shouldBe "The reason does not fit into any of the other categories"
        document.getHintText.get(1).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

}
