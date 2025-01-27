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

import fixtures.messages.SynchronousUpscanErrorMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadDocumentForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.FailureReasonEnum.{QUARANTINE, REJECTED, UNKNOWN}

class UploadDocumentFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(SynchronousUpscanErrorMessages.English, SynchronousUpscanErrorMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      "have the correct error message for the EntityTooSmall code" in {
        UploadDocumentForm.errorMessages("EntityTooSmall") shouldBe messagesForLanguage.errorFileTooSmall
      }

      "have the correct error message for the EntityTooLarge code" in {
        UploadDocumentForm.errorMessages("EntityTooLarge") shouldBe messagesForLanguage.errorFileTooLarge
      }

      "have the correct error message for the InvalidArgument code" in {
        UploadDocumentForm.errorMessages("InvalidArgument") shouldBe messagesForLanguage.errorNoFileSelected
      }

      s"have the correct error message for the $QUARANTINE code" in {
        UploadDocumentForm.errorMessages(QUARANTINE.toString) shouldBe messagesForLanguage.errorQuarantine
      }

      s"have the correct error message for the $REJECTED code" in {
        UploadDocumentForm.errorMessages(REJECTED.toString) shouldBe messagesForLanguage.errorRejected
      }

      s"have the correct error message for the $UNKNOWN code" in {
        UploadDocumentForm.errorMessages(UNKNOWN.toString) shouldBe messagesForLanguage.errorUploadFailed
      }

      "have the correct error message for any other code" in {
        UploadDocumentForm.errorMessages("UnableToUpload") shouldBe messagesForLanguage.errorUploadFailed
      }
    }
  }
}
