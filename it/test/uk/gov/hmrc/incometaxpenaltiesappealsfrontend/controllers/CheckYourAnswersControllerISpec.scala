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

class CheckYourAnswersControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {

  val bereavementReasonMessage: String = "When did the person die"
  val cessationReasonMessage: String = "TBC cessationReason"
  val crimeReasonMessage: String = "When did the crime happen"
  val fireOrFloodReasonReasonMessage: String = "When did the fire or flood happen"
  val healthReasonMessage: String = "TBC healthReason"
  val technicalReasonMessage: String = "When did the technology issues begin"
  val unexpectedHospitalReasonMessage: String = "TBC unexpectedHospitalReason"
  val otherReasonMessage: String = "TBC otherReason"

  val bereavementReasonValue = "Bereavement (someone died)"
  val cessationReasonValue = "Cessation of income source"
  val crimeReasonValue = "Crime"
  val fireOrFloodReasonValue = "Fire or flood"
  val healthReasonValue = "Serious or life-threatening ill health"
  val technicalReasonValue = "Technology issues"
  val unexpectedHospitalReasonValue = "Unexpected hospital stay"
  val otherReasonValue = "The reason does not fit into any of the other categories"

  val reasonsList: List[(String, String, String)] = List(
    ("bereavementReason", bereavementReasonValue, bereavementReasonMessage),
    ("cessationReason", cessationReasonValue, cessationReasonMessage),
    ("crimeReason", crimeReasonValue, crimeReasonMessage),
    ("fireOrFloodReason", fireOrFloodReasonValue, fireOrFloodReasonReasonMessage),
    ("healthReason", healthReasonValue, healthReasonMessage),
    ("technicalReason", technicalReasonValue, technicalReasonMessage),
    ("unexpectedHospitalReason", unexpectedHospitalReasonValue, unexpectedHospitalReasonMessage),
    ("otherReason", otherReasonValue, otherReasonMessage)
  )


  for (reason <- reasonsList) {

    s"GET /check-your-answers with ${reason._1}" should {
      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/check-your-answers", reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/check-your-answers", isAgent = true, reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }
      }

      "have the correct page has correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/check-your-answers", reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Check your answers - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe "Check your answers"
          document.getElementById("appealDetails").text() shouldBe "Appeal details"
          document.select(s"#${reason._1} > div:nth-child(1) > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select(s"#${reason._1} > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe reason._2
          document.select(s"#${reason._1} > div:nth-child(1) > dd.govuk-summary-list__actions > a").text() shouldBe "Change Reason for missing the submission deadline"
          document.select(s"#${reason._1} >  div:nth-child(2) > dt").text() shouldBe reason._3
          document.select(s"#${reason._1} > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "04 October 2027"
          document.select(s"#${reason._1} > div:nth-child(2) > dd.govuk-summary-list__actions > a").text() shouldBe s"Change ${reason._3}"
          document.getElementById("declaration").text() shouldBe "Declaration"
          document.getElementById(s"${reason._1}-Warning").text() shouldBe "! Warning By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. A false declaration can result in prosecution."
          document.getSubmitButton.text() shouldBe "Accept and send"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/check-your-answers", isAgent = true, reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Check your answers - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe "Check your answers"
          document.getElementById("appealDetails").text() shouldBe "Appeal details"
          document.select(s"#${reason._1} > div:nth-child(1) > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select(s"#${reason._1} > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe reason._2
          document.select(s"#${reason._1} > div:nth-child(1) > dd.govuk-summary-list__actions > a").text() shouldBe "Change Reason for missing the submission deadline"
          document.select(s"#${reason._1} >  div:nth-child(2) > dt").text() shouldBe reason._3
          document.select(s"#${reason._1} > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "04 October 2027"
          document.select(s"#${reason._1} > div:nth-child(2) > dd.govuk-summary-list__actions > a").text() shouldBe s"Change ${reason._3}"
          document.getElementById("declaration").text() shouldBe "Declaration"
          document.getElementById(s"${reason._1}-Warning").text() shouldBe "! Warning By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. A false declaration can result in prosecution."
          document.getSubmitButton.text() shouldBe "Accept and send"
        }
      }
    }
  }

}
