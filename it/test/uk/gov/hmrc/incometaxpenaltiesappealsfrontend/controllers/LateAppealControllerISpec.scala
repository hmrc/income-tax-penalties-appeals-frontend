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

class LateAppealControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val reasonsList: List[(String, String)] = List(
    ("bereavementReason", "45"),
    ("cessationReason", "30"),
    ("crimeReason", "30"),
    ("fireOrFloodReason", "30"),
    ("healthReason", "30"),
    ("technicalReason", "30"),
    ("unexpectedHospitalReason", "30"),
    ("otherReason", "30")
  )

  for (reason <- reasonsList) {

    s"GET /making-a-late-appeal with ${reason._1}" should {

      testNavBar(url = "/making-a-late-appeal", reasonableExcuse = Some(reason._1))()

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/making-a-late-appeal", reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/making-a-late-appeal", isAgent = true, reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/making-a-late-appeal", reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"This penalty point was issued more than ${reason._2} days ago - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe s"This penalty point was issued more than ${reason._2} days ago"
          document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
          document.getElementsByAttributeValue("for", "delayReason").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
          document.getElementById("delayReason-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/making-a-late-appeal", isAgent = true, reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"This penalty point was issued more than ${reason._2} days ago - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe s"This penalty point was issued more than ${reason._2} days ago"
          document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
          document.getElementsByAttributeValue("for", "delayReason").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
          document.getElementById("delayReason-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }
  }
}
