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

import fixtures.messages.English
import org.jsoup.Jsoup
import play.api.http.Status.OK
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}


class AppealStartControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  "GET /appeal-start" should {

    testNavBar("/appeal-start")()

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
        document.getElementById("captionSpan").text() shouldBe English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
        document.getParagraphs.get(0).text() shouldBe "To appeal a late submission penalty for Self Assessment, you’ll need to ask HMRC to look at your case again."
        document.getParagraphs.get(1).text() shouldBe "This service is for appealing penalties given for individual submissions."
        document.getH2Elements.get(0).text() shouldBe "Before you start"
        document.getParagraphs.get(2).text() shouldBe "You’ll need:"
        document.getBulletPoints.get(0).text() shouldBe "a reason why the quarterly update or tax return was not submitted by the due date (HMRC calls this ‘a reasonable excuse’)"
        document.getBulletPoints.get(1).text() shouldBe "the dates related to this reasonable excuse"
        document.getBulletPoints.get(2).text() shouldBe "details of why you did not appeal sooner"
        document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
        document.getParagraphs.get(4).text() shouldBe "In some cases, you’ll be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
        document.getParagraphs.get(5).text() shouldBe "If you are not asked for extra evidence, this is because we don’t need any to make a decision in your particular case."
        document.getParagraphs.get(6).text() shouldBe "If we decide we need extra evidence after reviewing your appeal, we will contact you."
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/appeal-start", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Appeal a Self Assessment penalty - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"
        document.getParagraphs.get(0).text() shouldBe "To appeal a late submission penalty for Self Assessment, you’ll need to ask HMRC to look at your case again."
        document.getParagraphs.get(1).text() shouldBe "This service is for appealing penalties given for individual submissions."
        document.getH2Elements.get(0).text() shouldBe "Before you start"
        document.getParagraphs.get(2).text() shouldBe "You’ll need:"
        document.getBulletPoints.get(0).text() shouldBe "a reason why the quarterly update or tax return was not submitted by the due date (HMRC calls this ‘a reasonable excuse’)"
        document.getBulletPoints.get(1).text() shouldBe "the dates related to this reasonable excuse"
        document.getBulletPoints.get(2).text() shouldBe "details of why you did not appeal sooner"
        document.getLink("guidanceLink").text() shouldBe "Read the guidance about reasonable excuses (opens in new tab)"
        document.getParagraphs.get(4).text() shouldBe "In some cases, you’ll be asked if you want to upload evidence to support your appeal. You should gather this evidence before you continue, as you will not be able to save this appeal and complete it later."
        document.getParagraphs.get(5).text() shouldBe "If you are not asked for extra evidence, this is because we don’t need any to make a decision in your particular case."
        document.getParagraphs.get(6).text() shouldBe "If we decide we need extra evidence after reviewing your appeal, we will contact you."
        document.getSubmitButton.text() shouldBe "Continue"
      }
    }
  }

}
