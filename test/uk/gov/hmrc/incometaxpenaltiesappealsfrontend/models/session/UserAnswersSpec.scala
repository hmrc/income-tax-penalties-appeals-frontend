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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json, OFormat}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.Page
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.time.temporal.ChronoUnit

class UserAnswersSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  case class TestModel(hasDate: Boolean, date: Option[String])
  object TestModel {
    implicit val fmt: OFormat[TestModel] = Json.format[TestModel]
  }

  lazy val testPage2Value: TestModel = TestModel(hasDate = true, Some("bar"))

  lazy val testPage1: Page[String]    = new Page[String] { val key = "page1" }
  lazy val testPage2: Page[TestModel] = new Page[TestModel] { val key = "page2" }

  lazy val answersAsJson: JsObject = Json.obj(
    testPage1.key -> "foo",
    testPage2.key -> testPage2Value
  )

  lazy val now: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)

  "be writable to JSON" in {
    val model = UserAnswers("journey123", answersAsJson, now)
    val expectedResult = Json.obj(
      "journeyId" -> "journey123",
      "data" -> answersAsJson,
      "lastUpdated" ->  Json.toJson(now)(MongoJavatimeFormats.instantWrites)
    )

    Json.toJson(model) shouldBe expectedResult
  }

  "be readable from JSON" in {
    val json = Json.obj(
      "journeyId" -> "journey123",
      "data" -> answersAsJson,
      "lastUpdated" -> Json.toJson(now)(MongoJavatimeFormats.instantWrites)
    )
    val expectedModel = UserAnswers("journey123", answersAsJson, now)
    val result = Json.fromJson(json)(UserAnswers.format)
    result.isSuccess shouldBe true
    result.get shouldBe expectedModel
  }

  "setAnswer" should {
    "store the answer in the data field" in {
      val answers = UserAnswers("journey123", answersAsJson)
      val newAnswers = answers.setAnswer(testPage2, testPage2Value)
      newAnswers.getAnswer(testPage2) shouldBe Some(testPage2Value)
    }

    "overwrite the answer if already present in the data field" in {

      val answers = UserAnswers("journey123", answersAsJson)
      answers.getAnswer(testPage1) shouldBe Some("foo")

      val newAnswers = answers.setAnswer(testPage1, "bar")
      newAnswers.getAnswer(testPage1) shouldBe Some("bar")
    }
  }

  "getAnswer" should {
    "return Some" when {
      "the answer is in the data field" in {
        val answers = UserAnswers("journey123", answersAsJson)
        answers.getAnswer(testPage1) shouldBe Some("foo")
      }
    }

    "return None" when {
      "the answer is not in the data field" in {
        val answers = UserAnswers("journey123", answersAsJson).removeAnswer(testPage1)
        answers.getAnswer(testPage1) shouldBe None
      }
    }
  }
}
