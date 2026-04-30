/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import org.jsoup.Jsoup
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.{AuthStub, IncomeTaxSessionDataStub, MessagesStub}


trait NavBarTesterHelper extends AnyWordSpec with MessagesStub {
  this: ComponentSpecHelper with AuthStub with IncomeTaxSessionDataStub =>

  def testNavBar(url: String, queryParams: Map[String, String] = Map.empty)(runStubsAndUserAnswersSetup: => Unit = ()): Unit = {
    "Checking the Navigation Bar" when {
      "the origin is PTA" should {
        "render the PTA service navigation content" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          stubMessagesCount()(OK, Json.obj("count" -> 0))
          runStubsAndUserAnswersSetup
          val result = get(url, origin = Some("PTA"), queryParams = queryParams)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("ul#pta-service-navigation").isEmpty shouldBe false
          document.select("ul#pta-service-navigation li").size() shouldBe 3
        }
      }

      "the origin is BTA" should {
        "render the BTA service navigation content" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          stubMessagesCount()(OK, Json.obj("count" -> 0))
          runStubsAndUserAnswersSetup
          val result = get(url, origin = Some("BTA"), queryParams = queryParams)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("ul#bta-service-navigation").isEmpty shouldBe false
          document.select("ul#bta-service-navigation li").size() shouldBe 3
        }
      }

      "the origin is unknown" should {
        "render without PTA or BTA service navigation items" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          runStubsAndUserAnswersSetup
          val result = get(url, origin = None, queryParams = queryParams)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("ul#pta-service-navigation").isEmpty shouldBe true
          document.select("ul#bta-service-navigation").isEmpty shouldBe true
        }
      }

    }
  }

  def testNoNavBar(url: String, queryParams: Map[String, String] = Map.empty)(runStubs: => Unit = ()): Unit = {
    "the user is an Agent" should {
      "render without PTA or BTA service navigation items" in {
        stubAuth(OK, successfulAgentAuthResponse)
        stubGetIncomeTaxSessionDataSuccessResponse()
        runStubs
        val result = get(url, origin = Some("BTA"), isAgent = true, queryParams = queryParams)

        result.status shouldBe OK
        val document = Jsoup.parse(result.body)

        document.select("ul#pta-service-navigation").isEmpty shouldBe true
        document.select("ul#bta-service-navigation").isEmpty shouldBe true
      }
    }
  }
}
