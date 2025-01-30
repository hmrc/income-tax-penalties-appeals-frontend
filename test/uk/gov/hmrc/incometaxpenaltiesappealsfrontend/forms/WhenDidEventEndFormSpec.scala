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

import fixtures.messages.{MonthMessages, WhenDidEventEndMessages}
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

  Seq(
    WhenDidEventEndMessages.English -> MonthMessages.English,
    WhenDidEventEndMessages.Welsh -> MonthMessages.Welsh
  ).foreach { case (messagesForLanguage, monthMessages) =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      val form: Form[LocalDate] = WhenDidEventEndForm.form("technicalReason", LocalDate.of(2021, 1, 1))(messages, appConfig, timeMachine)

      s"WhenDidEventEndForm with technicalReason" should {
        behave like dateForm(
          form = form,
          fieldName = "date",
          errorMessageKey = errorType => s"technicalReason.end.date.error.$errorType",
          errorMessageValue = (errorType, args) => messagesForLanguage.errorMessageConstructor(errorType, args:_*),
          messagesForLanguage = messagesForLanguage
        )

        "not bind when the date entered is earlier than the date provided previously" in {
          val result = form.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "12",
              "date.year" -> "2020"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe
            FormError(
              key = "date.day",
              message = messagesForLanguage.errorMessageConstructor(
                "endDateLessThanStartDate",
                s"1 ${monthMessages.january} 2021"
              ),
              args = Seq(monthMessages.day, monthMessages.month, monthMessages.year)
            )
        }
      }
    }
  }
}
