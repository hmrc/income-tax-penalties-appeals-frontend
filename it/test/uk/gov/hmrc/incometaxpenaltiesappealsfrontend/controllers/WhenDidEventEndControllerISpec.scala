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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class WhenDidEventEndControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "GET /when-did-the-event-end" should {
    testNavBar(url = "/when-did-the-event-end", reasonableExcuse = Some("technicalReason"))()
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/when-did-the-event-end", reasonableExcuse = Some("technicalReason"))

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/when-did-the-event-happen", isAgent = true, reasonableExcuse = Some("technicalReason"))

        result.status shouldBe OK
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/when-did-the-event-end", reasonableExcuse = Some("technicalReason"))

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "When did the software or technology issues end? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "When did the software or technology issues end?"
        document.getElementById("technicalReason-hint").text() shouldBe "For example, 12 3 2018"
        document.getElementsByAttributeValue("for", "technicalReason-day").text() shouldBe "Day"
        document.getElementsByAttributeValue("for", "technicalReason-month").text() shouldBe "Month"
        document.getElementsByAttributeValue("for", "technicalReason-year").text() shouldBe "Year"
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/when-did-the-event-end", isAgent = true, reasonableExcuse = Some("technicalReason"))

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "When did the software or technology issues end? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "When did the software or technology issues end?"
        document.getElementById("technicalReason-hint").text() shouldBe "For example, 12 3 2018"
        document.getElementsByAttributeValue("for", "technicalReason-day").text() shouldBe "Day"
        document.getElementsByAttributeValue("for", "technicalReason-month").text() shouldBe "Month"
        document.getElementsByAttributeValue("for", "technicalReason-year").text() shouldBe "Year"
        document.getSubmitButton.text() shouldBe "Continue"
      }
    }
  }
}
