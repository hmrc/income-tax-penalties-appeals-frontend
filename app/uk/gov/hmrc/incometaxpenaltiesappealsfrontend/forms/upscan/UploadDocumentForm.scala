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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan

import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig

object UploadDocumentForm {

  val key = "file"
  val form: Form[String] = Form[String](key -> text)

  def errorMessages(code: String)(implicit messages: Messages, appConfig: AppConfig): String = code match {
    case "EntityTooSmall" => messages("uploadEvidence.error.fileTooSmall")
    case "EntityTooLarge" => messages("uploadEvidence.error.fileTooLarge", appConfig.upscanMaxFileSizeMB)
    case "InvalidArgument" => messages("uploadEvidence.error.noFileSpecified")
    case "QUARANTINE" => messages(s"uploadEvidence.error.QUARANTINE")
    case "REJECTED" => messages(s"uploadEvidence.error.REJECTED")
    case "INVALID_FILENAME" => messages(s"uploadEvidence.error.INVALID_FILENAME")
    case _ => messages("uploadEvidence.error.unableToUpload")
  }
}
