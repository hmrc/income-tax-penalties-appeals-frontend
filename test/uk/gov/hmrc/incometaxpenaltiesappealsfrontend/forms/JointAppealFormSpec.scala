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

import fixtures.messages.JointAppealMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig

class JointAppealFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with FormBehaviours {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(JointAppealMessages.English, JointAppealMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      val form: Form[Boolean] = JointAppealForm.form()

      "bind" when {

        behave like mandatoryField(
          form = form,
          fieldName = JointAppealForm.key,
          requiredError = FormError(JointAppealForm.key, messagesForLanguage.errorRequired)
        )

        "bind valid values" in {
          Seq("true", "false").foreach  { value =>
            val result = form.bind(Map(JointAppealForm.key -> value))
            result.value shouldBe Some(value.toBoolean)
          }
        }

        "reject invalid values" in {
          val result = form.bind(Map(JointAppealForm.key -> "foo"))
          result.errors.headOption shouldBe Some(FormError(JointAppealForm.key, messagesForLanguage.errorInvalid))
        }
      }
    }
  }
}
