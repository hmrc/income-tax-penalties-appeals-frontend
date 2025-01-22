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

class UploadStatusEnumSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "withName" should {
    "return valid Enum value" when {
      "string value is valid" in {
        UploadStatusEnum.withName("WAITING") shouldBe UploadStatusEnum.WAITING
        UploadStatusEnum.withName("READY") shouldBe UploadStatusEnum.READY
        UploadStatusEnum.withName("FAILED") shouldBe UploadStatusEnum.FAILED
      }
    }
    "throw NoSuchElementException" when {
      "string is not a valid Enum value" in {
        val error = intercept[NoSuchElementException](UploadStatusEnum.withName("foo"))
        error.getMessage shouldBe "No value found for 'foo'"
      }
    }
  }

  "should serialise to JSON as expected" in {
    Json.toJson(UploadStatusEnum.WAITING) shouldBe JsString("WAITING")
    Json.toJson(UploadStatusEnum.READY) shouldBe JsString("READY")
    Json.toJson(UploadStatusEnum.FAILED) shouldBe JsString("FAILED")
  }

  "should deserialise from JSON as expected" in {
    JsString("WAITING").as[UploadStatusEnum.Value] shouldBe UploadStatusEnum.WAITING
    JsString("READY").as[UploadStatusEnum.Value] shouldBe UploadStatusEnum.READY
    JsString("FAILED").as[UploadStatusEnum.Value] shouldBe UploadStatusEnum.FAILED
  }
}