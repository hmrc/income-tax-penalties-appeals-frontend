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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import fixtures.messages.CrimeReportedMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class CrimeReportedEnumSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "apply" should {
    "return valid Enum value" when {
      "string value is valid" in {
        CrimeReportedEnum("yes") shouldBe CrimeReportedEnum.yes
        CrimeReportedEnum("no") shouldBe CrimeReportedEnum.no
        CrimeReportedEnum("unknown") shouldBe CrimeReportedEnum.unknown
      }
    }
    "throw IllegalArgumentException" when {
      "string is not a valid Enum value" in {
        val error = intercept[IllegalArgumentException](CrimeReportedEnum("foo"))
        error.getMessage shouldBe "Invalid crime reported value of 'foo'"
      }
    }
  }

  "should serialise to JSON as expected" in {
    Json.toJson(CrimeReportedEnum.yes) shouldBe JsString("yes")
    Json.toJson(CrimeReportedEnum.no) shouldBe JsString("no")
    Json.toJson(CrimeReportedEnum.unknown) shouldBe JsString("unknown")
  }

  "should deserialise from JSON as expected" in {
    JsString("yes").as[CrimeReportedEnum.Value] shouldBe CrimeReportedEnum.yes
    JsString("no").as[CrimeReportedEnum.Value] shouldBe CrimeReportedEnum.no
    JsString("unknown").as[CrimeReportedEnum.Value] shouldBe CrimeReportedEnum.unknown
  }

  "calling .radioOptions()" when {

    Seq(CrimeReportedMessages.English, CrimeReportedMessages.Welsh).foreach { messagesForLanguage =>

      s"rendering in the language '${messagesForLanguage.lang.name}'" should {

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

        "render the expected radio options" in {

          CrimeReportedEnum.radioOptions() shouldBe Seq(
            RadioItem(
              content = Text(messagesForLanguage.yes),
              value = Some(CrimeReportedEnum.yes.toString)
            ),
            RadioItem(
              content = Text(messagesForLanguage.no),
              value = Some(CrimeReportedEnum.no.toString)
            ),
            RadioItem(
              content = Text(messagesForLanguage.unkownOption),
              value = Some(CrimeReportedEnum.unknown.toString)
            )
          )
        }
      }
    }
  }
}
