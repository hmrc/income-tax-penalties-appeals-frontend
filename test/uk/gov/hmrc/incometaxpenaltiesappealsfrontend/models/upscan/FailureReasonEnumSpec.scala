/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsString, Json}

class FailureReasonEnumSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "withName" should {
    "return valid Enum value" when {
      "string value is valid" in {
        FailureReasonEnum.withName("QUARANTINE") shouldBe FailureReasonEnum.QUARANTINE
        FailureReasonEnum.withName("REJECTED") shouldBe FailureReasonEnum.REJECTED
        FailureReasonEnum.withName("UNKNOWN") shouldBe FailureReasonEnum.UNKNOWN
      }
    }
    "throw NoSuchElementException" when {
      "string is not a valid Enum value" in {
        val error = intercept[NoSuchElementException](FailureReasonEnum.withName("foo"))
        error.getMessage shouldBe "No value found for 'foo'"
      }
    }
  }

  "should serialise to JSON as expected" in {
    Json.toJson(FailureReasonEnum.QUARANTINE) shouldBe JsString("QUARANTINE")
    Json.toJson(FailureReasonEnum.REJECTED) shouldBe JsString("REJECTED")
    Json.toJson(FailureReasonEnum.UNKNOWN) shouldBe JsString("UNKNOWN")
  }

  "should deserialise from JSON as expected" in {
    JsString("QUARANTINE").as[FailureReasonEnum.Value] shouldBe FailureReasonEnum.QUARANTINE
    JsString("REJECTED").as[FailureReasonEnum.Value] shouldBe FailureReasonEnum.REJECTED
    JsString("UNKNOWN").as[FailureReasonEnum.Value] shouldBe FailureReasonEnum.UNKNOWN
  }
}