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

import fixtures.BtaNavContentFixture
import org.jsoup.Jsoup
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.{AuthStub, BtaNavLinksStub, MessagesStub}


trait NavBarTesterHelper extends AnyWordSpec with BtaNavLinksStub with MessagesStub with BtaNavContentFixture { _: ComponentSpecHelper with AuthStub =>

  def testNavBar(url: String, queryParams: Map[String, String] = Map.empty, reasonableExcuse: Option[String] = None)(runStubsAndUserAnswersSetup: => Unit = ()): Unit = {
    "Checking the Navigation Bar" when {
      "the origin is PTA" should {
        "render the PTA content" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          stubMessagesCount()(OK, Json.obj("count" -> 0))
          runStubsAndUserAnswersSetup
          val result = get(url, origin = Some("PTA"), queryParams = queryParams, reasonableExcuse = reasonableExcuse)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav.hmrc-account-menu").isEmpty shouldBe false
        }
      }

      "the origin is BTA" should {
        "render the BTA content" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          runStubsAndUserAnswersSetup
          stubBtaNavLinks()(OK, Json.toJson(btaNavContent))
          val result = get(url, origin = Some("BTA"), queryParams = queryParams, reasonableExcuse = reasonableExcuse)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav-bta.hmrc-account-menu").isEmpty shouldBe false
        }
      }

      "the origin is unknown" should {
        "render without a Nav" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          runStubsAndUserAnswersSetup
          val result = get(url, origin = None, queryParams = queryParams, reasonableExcuse = reasonableExcuse)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav-bta.hmrc-account-menu").isEmpty shouldBe true
          document.select("nav#secondary-nav.hmrc-account-menu").isEmpty shouldBe true
        }
      }

      "the user is an Agent" should {
        "render without a Nav" in {
          stubAuth(OK, successfulAgentAuthResponse)
          runStubsAndUserAnswersSetup
          val result = get(url, isAgent = true, origin = None, queryParams = queryParams, reasonableExcuse = reasonableExcuse)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav-bta.hmrc-account-menu").isEmpty shouldBe true
          document.select("nav#secondary-nav.hmrc-account-menu").isEmpty shouldBe true
        }
      }
    }
  }
}
