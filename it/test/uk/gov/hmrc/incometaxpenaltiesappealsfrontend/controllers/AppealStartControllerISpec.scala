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

class AppealStartControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {


  "GET /appeal-start" should {
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/appeal-start")

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/appeal-start", isAgent = true)

        result.status shouldBe OK
      }
    }
    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/appeal-start")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Appeal a Self Assessment penalty - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
        document.getParagraphs.get(1).text() shouldBe "To appeal a late submission penalty for Self Assessment, you'll need to ask HMRC to look at your case again."
        document.getParagraphs.get(2).text() shouldBe "This service is for appealing penalties given for individual submissions."
        document.getH3Elements.text() shouldBe "Before you start"
        document.getParagraphs.get(3).text() shouldBe "You'll need:"
        document.getBulletPoints.get(7).text() shouldBe "a reason why the quarterly update or tax return was not submitted by the due date (HMRC calls this 'a reasonable excuse')"
        document.getBulletPoints.get(8).text() shouldBe "the dates related to this reasonable excuse"
        document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
        document.getParagraphs.get(5).text() shouldBe "In some cases, you'll be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
        document.getParagraphs.get(6).text() shouldBe "If you are not asked for extra evidence, this is because we don't need any to make a decision in your particular case."
        document.getParagraphs.get(7).text() shouldBe "If we decide we do need extra evidence after reviewing your appeal, we will contact you."
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/appeal-start", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Appeal a Self Assessment penalty - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
        document.getParagraphs.get(1).text() shouldBe "To appeal a late submission penalty for Self Assessment, you'll need to ask HMRC to look at your case again."
        document.getParagraphs.get(2).text() shouldBe "This service is for appealing penalties given for individual submissions."
        document.getH3Elements.text() shouldBe "Before you start"
        document.getParagraphs.get(3).text() shouldBe "You'll need:"
        document.getBulletPoints.get(7).text() shouldBe "a reason why the quarterly update or tax return was not submitted by the due date (HMRC calls this 'a reasonable excuse')"
        document.getBulletPoints.get(8).text() shouldBe "the dates related to this reasonable excuse"
        document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
        document.getParagraphs.get(5).text() shouldBe "In some cases, you'll be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
        document.getParagraphs.get(6).text() shouldBe "If you are not asked for extra evidence, this is because we don't need any to make a decision in your particular case."
        document.getParagraphs.get(7).text() shouldBe "If we decide we do need extra evidence after reviewing your appeal, we will contact you."
        document.getSubmitButton.text() shouldBe "Continue"
      }
    }
  }

}
