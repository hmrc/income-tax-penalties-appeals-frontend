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

class WhenDidEventHappenControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  val bereavementReasonMessage: String = "When did the person die?"
  val cessationReasonMessage: String = "TBC cessationReason"
  val crimeReasonMessage: String = "When did the crime happen?"
  val fireOrFloodReasonReasonMessage: String = "When did the fire or flood happen?"
  val healthReasonMessage: String = "TBC healthReason"
  val technicalReasonMessage: String = "When did the software or technology issues begin?"
  val unexpectedHospitalReasonMessage: String = "TBC unexpectedHospitalReason"
  val otherReasonMessage: String = "TBC otherReason"

  val reasonsList: List[(String, String)] = List(
    ("bereavementReason", bereavementReasonMessage),
    ("cessationReason", cessationReasonMessage),
    ("crimeReason", crimeReasonMessage),
    ("fireOrFloodReason", fireOrFloodReasonReasonMessage),
    ("healthReason", healthReasonMessage),
    ("technicalReason", technicalReasonMessage),
    ("unexpectedHospitalReason", unexpectedHospitalReasonMessage),
    ("otherReason", otherReasonMessage)
  )

  for (reason <- reasonsList) {

    s"GET /when-did-the-event-happen with ${reason._1}" should {
      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/when-did-the-event-happen", reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/when-did-the-event-happen", isAgent = true, reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/when-did-the-event-happen", reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"${reason._2} - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe reason._2
          document.getElementById(s"${reason._1}-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", s"${reason._1}-day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", s"${reason._1}-month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", s"${reason._1}-year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/when-did-the-event-happen", isAgent = true, reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"${reason._2} - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe reason._2
          document.getElementById(s"${reason._1}-hint").text() shouldBe "For example, 12 3 2018"
          document.getElementsByAttributeValue("for", s"${reason._1}-day").text() shouldBe "Day"
          document.getElementsByAttributeValue("for", s"${reason._1}-month").text() shouldBe "Month"
          document.getElementsByAttributeValue("for", s"${reason._1}-year").text() shouldBe "Year"
          document.getSubmitButton.text() shouldBe "Continue"

        }
      }
    }
  }
}
