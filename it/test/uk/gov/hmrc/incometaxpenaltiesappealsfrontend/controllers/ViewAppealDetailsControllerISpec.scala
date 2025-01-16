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

class ViewAppealDetailsControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val bereavementReasonMessage: String = "When did the person die?"
  val cessationReasonMessage: String = "TBC cessationReason"
  val crimeReasonMessage: String = "When did the crime happen?"
  val fireOrFloodReasonReasonMessage: String = "When did the fire or flood happen?"
  val healthReasonMessage: String = "TBC healthReason"
  val technicalReasonMessage: String = "When did the software or technology issues begin?"
  val unexpectedHospitalReasonMessage: String = "TBC unexpectedHospitalReason"
  val otherReasonMessage: String = "TBC otherReason"

  val bereavementReasonValue = "Bereavement (someone died)"
  val cessationReasonValue = "Cessation of income source"
  val crimeReasonValue = "Crime"
  val fireOrFloodReasonValue = "Fire or flood"
  val healthReasonValue = "Serious or life-threatening ill health"
  val technicalReasonValue = "Software or technology issues"
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

    s"GET /appeal-details with ${reason._1}" should {
      testNavBar(url = "/appeal-details", reasonableExcuse = Some(reason._1))()
      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/appeal-details", reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/appeal-details", isAgent = true, reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/appeal-details", reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Appeal details - Appeal a Self Assessment penalty - GOV.UK"
          document.getH1Elements.text() shouldBe "Appeal details"
          document.select("#nationalInsurance > dt").text() shouldBe "National Insurance number"
          document.select("#nationalInsurance > dd").text() shouldBe "QQ123456B"
          document.select("#penaltyAppealed > dt").text() shouldBe "Penalty appealed"
          document.select("#penaltyAppealed > dd").text() shouldBe "Late payment penalty: 2027 to 2028 tax year"
          document.select("#appealDate > dt").text() shouldBe "Appeal date"
          document.select("#appealDate > dd").text() shouldBe "17 March 2029"
          document.select("#multiplePenalties > dt").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.select("#multiplePenalties > dd").text() shouldBe "No"
          document.select("#penaltyReason > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select("#penaltyReason > dd").text() shouldBe reason._2
          document.select("#startDate > dt").text() shouldBe reason._3
          document.select("#startDate > dd").text() shouldBe "20 January 2029"
          if(reason._1 == "technicalReason"){
            document.select("#endDate > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#endDate > dd").text() shouldBe "20 February 2029"
          }
          if(reason._1 == "crimeReason"){
            document.select("#reportedCrime > dt").text() shouldBe "Has this crime been reported to the police?"
            document.select("#reportedCrime > dd").text() shouldBe "Yes"
          }
          document.select("#printWarn > p").text() shouldBe "Print or download this page if you want to keep it. You will not be able to return to these appeal details later."
          document.getElementById("print-button").text shouldBe "Print this page"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/appeal-details", isAgent = true, reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Appeal details - Appeal a Self Assessment penalty - GOV.UK"
          document.getH1Elements.text() shouldBe "Appeal details"
          document.select("#nationalInsurance > dt").text() shouldBe "National Insurance number"
          document.select("#nationalInsurance > dd").text() shouldBe "QQ123456B"
          document.select("#penaltyAppealed > dt").text() shouldBe "Penalty appealed"
          document.select("#penaltyAppealed > dd").text() shouldBe "Late payment penalty: 2027 to 2028 tax year"
          document.select("#appealDate > dt").text() shouldBe "Appeal date"
          document.select("#appealDate > dd").text() shouldBe "17 March 2029"
          document.select("#whoPlanned > dt").text() shouldBe "Before the deadline, who planned to send the submission?"
          document.select("#whoPlanned > dd").text() shouldBe "I did"
          document.select("#multiplePenalties > dt").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.select("#multiplePenalties > dd").text() shouldBe "No"
          document.select("#penaltyReason > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select("#penaltyReason > dd").text() shouldBe reason._2
          document.select("#startDate > dt").text() shouldBe reason._3
          document.select("#startDate > dd").text() shouldBe "20 January 2029"
          if(reason._1 == "technicalReason"){
            document.select("#endDate > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#endDate > dd").text() shouldBe "20 February 2029"
          }
          if(reason._1 == "crimeReason"){
            document.select("#reportedCrime > dt").text() shouldBe "Has this crime been reported to the police?"
            document.select("#reportedCrime > dd").text() shouldBe "Yes"
          }
          document.select("#printWarn > p").text() shouldBe "Print or download this page if you want to keep it. You will not be able to return to these appeal details later."
          document.getElementById("print-button").text shouldBe "Print this page"
        }
      }
    }
  }

}
