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

import fixtures.messages.WhoPlannedToSubmitMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class AgentClientEnumSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "withName" should {
    "return valid Enum value" when {
      "string value is valid" in {
        AgentClientEnum.withName("client") shouldBe AgentClientEnum.client
        AgentClientEnum.withName("agent") shouldBe AgentClientEnum.agent
      }
    }
    "throw NoSuchElementException" when {
      "string is not a valid Enum value" in {
        val error = intercept[NoSuchElementException](AgentClientEnum.withName("foo"))
        error.getMessage shouldBe "No value found for 'foo'"
      }
    }
  }

  "should serialise to JSON as expected" in {
    Json.toJson(AgentClientEnum.agent) shouldBe JsString("agent")
    Json.toJson(AgentClientEnum.client) shouldBe JsString("client")
  }

  "should deserialise from JSON as expected" in {
    JsString("agent").as[AgentClientEnum.Value] shouldBe AgentClientEnum.agent
    JsString("client").as[AgentClientEnum.Value] shouldBe AgentClientEnum.client
  }

  "calling .radioOptions() for the WhoPlannedToSubmit page" when {

    Seq(WhoPlannedToSubmitMessages.English, WhoPlannedToSubmitMessages.Welsh).foreach { messagesForLanguage =>

      s"rendering in the language '${messagesForLanguage.lang.name}'" should {

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

        "render the expected radio options" in {

          AgentClientEnum.radioOptions("whoPlannedToSubmit") shouldBe Seq(
            RadioItem(
              content = Text(messagesForLanguage.agent),
              value = Some(AgentClientEnum.agent.toString)
            ),
            RadioItem(
              content = Text(messagesForLanguage.client),
              value = Some(AgentClientEnum.client.toString)
            )
          )
        }
      }
    }
  }
}