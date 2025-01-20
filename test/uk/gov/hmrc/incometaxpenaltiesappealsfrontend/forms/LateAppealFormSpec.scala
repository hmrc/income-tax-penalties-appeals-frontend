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

import fixtures.messages.LateAppealMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig

class LateAppealFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with FormBehaviours {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(LateAppealMessages.English, LateAppealMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      val form: Form[String] = LateAppealForm.form()

      "bind" when {

        behave like mandatoryField(
          form = form,
          fieldName = LateAppealForm.key,
          requiredError = FormError(LateAppealForm.key, messagesForLanguage.errorRequired)
        )

        s"allow a text value with length <= ${appConfig.numberOfCharsInTextArea}" in {

          val value = "A" * appConfig.numberOfCharsInTextArea
          val result = form.bind(Map(LateAppealForm.key -> value))

          result.hasErrors shouldBe false
          result.value shouldBe Some(value)
        }

        s"reject more than ${appConfig.numberOfCharsInTextArea} characters with correct error message" in {

          val value = "A" * (appConfig.numberOfCharsInTextArea + 1)
          val result = form.bind(Map(LateAppealForm.key -> value))

          result.errors.headOption shouldBe Some(FormError(
            key = LateAppealForm.key,
            message = messagesForLanguage.errorLength(appConfig.numberOfCharsInTextArea)
          ))
        }

        "reject non0standard character give regex error and not bind in" in {

          val result = form.bind(Map(LateAppealForm.key -> invalidChars))

          result.errors.headOption shouldBe Some(FormError(LateAppealForm.key, messagesForLanguage.errorRegex))
        }
      }
    }
  }
}
