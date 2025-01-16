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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class CrimeReportedControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {


  "GET /has-this-crime-been-reported" should {
    testNavBar(url = "/has-this-crime-been-reported", reasonableExcuse = Some("crimeReason"))()
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/has-this-crime-been-reported", reasonableExcuse = Some("crimeReason"))

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/has-this-crime-been-reported", isAgent = true, reasonableExcuse = Some("crimeReason"))

        result.status shouldBe OK
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/has-this-crime-been-reported", reasonableExcuse = Some("crimeReason"))

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Has this crime been reported to the police? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Has this crime been reported to the police?"
        document.getElementsByAttributeValue("for", "crimeReason").text() shouldBe "Yes"
        document.getElementsByAttributeValue("for", "crimeReason-2").text() shouldBe "No"
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/has-this-crime-been-reported", isAgent = true, reasonableExcuse = Some("crimeReason"))

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Has this crime been reported to the police? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Has this crime been reported to the police?"
        document.getElementsByAttributeValue("for", "crimeReason").text() shouldBe "Yes"
        document.getElementsByAttributeValue("for", "crimeReason-2").text() shouldBe "No"
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

}
