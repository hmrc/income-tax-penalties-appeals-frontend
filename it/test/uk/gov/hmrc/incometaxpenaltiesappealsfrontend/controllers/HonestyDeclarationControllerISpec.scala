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

class HonestyDeclarationControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val bereavementReasonMessage: String = "because I was affected by someone's death, I was unable to send the submission due on 5 November 2027"
  val cessationReasonMessage: String = "TBC cessationReason - I was unable to send the submission due on 5 November 2027"
  val crimeReasonMessage: String = "because I was affected by a crime, I was unable to send the submission due on 5 November 2027"
  val fireOrFloodReasonReasonMessage: String = "because of a fire or flood, I was unable to send the submission due on 5 November 2027"
  val healthReasonMessage: String = "TBC healthReason - I was unable to send the submission due on 5 November 2027"
  val technicalReasonMessage: String = "because of software or technology issues, I was unable to send the submission due on 5 November 2027"
  val unexpectedHospitalReasonMessage: String = "TBC unexpectedHospitalReason - I was unable to send the submission due on 5 November 2027"
  val otherReasonMessage: String = "TBC otherReason - I was unable to send the submission due on 5 November 2027"

  val reasonsList: List[(String, String)]= List(
    ("bereavementReason", bereavementReasonMessage),
    ("cessationReason", cessationReasonMessage),
    ("crimeReason", crimeReasonMessage),
    ("fireOrFloodReason", fireOrFloodReasonReasonMessage),
    ("healthReason", healthReasonMessage),
    ("technicalReason", technicalReasonMessage),
    ("unexpectedHospitalReason", unexpectedHospitalReasonMessage),
    ("otherReason", otherReasonMessage)
  )


  for(reason <- reasonsList) {

    s"GET /honesty-declaration with ${reason._1}" should {
      testNavBar(url = "/honesty-declaration", reasonableExcuse = Some(reason._1))()

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/honesty-declaration", reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/honesty-declaration", isAgent = true, reasonableExcuse = Some(reason._1))

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result = get("/honesty-declaration", reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Honesty declaration - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe "Honesty declaration"
          document.getElementById("honestyDeclarationConfirm").text() shouldBe "I confirm that:"
          document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
          document.getElementById("honestyDeclaration").text() shouldBe "I will provide honest and accurate information in this appeal"
          document.getSubmitButton.text() shouldBe "Accept and continue"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          val result = get("/honesty-declaration", isAgent = true, reasonableExcuse = Some(reason._1))

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Honesty declaration - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe "Honesty declaration"
          document.getElementById("honestyDeclarationConfirm").text() shouldBe "I confirm that:"
          document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
          document.getElementById("honestyDeclaration").text() shouldBe "I will provide honest and accurate information in this appeal"
          document.getSubmitButton.text() shouldBe "Accept and continue"
        }
      }
    }
  }

}
