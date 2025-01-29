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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms

import fixtures.messages.WhenDidEventEndMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import java.time.LocalDate

class WhenDidEventEndFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with FormBehaviours {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(WhenDidEventEndMessages.English, WhenDidEventEndMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      val form: Form[LocalDate] = WhenDidEventEndForm.form("technicalReason", LocalDate.of(2021, 1, 1))(messages, appConfig, timeMachine)

      s"WhenDidEventEndForm with technicalReason" should {
        behave like dateForm(form, "date", errorType => s"technicalReason.end.date.error.$errorType", messagesForLanguage.lang.name)

        "not bind when the date entered is earlier than the date provided previously" in {
          val result = form.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "12",
              "date.year" -> "2020"
            )
          )
          result.errors.size shouldBe 1

          messagesForLanguage.lang.name match {
            case "English" =>
              result.errors.head shouldBe FormError("date.day",
                "The date the software or technology issues ended must be 1\u00A0January\u00A02021 or later", Seq("day", "month", "year"))
            case "Cymraeg" =>
              result.errors.head shouldBe FormError("date.day",
                "The date the software or technology issues ended must be 1\u00A0January (Welsh)\u00A02021 or later (Welsh)", Seq("diwrnod", "mis", "blwyddyn"))
          }
        }
      }
    }
  }
}
