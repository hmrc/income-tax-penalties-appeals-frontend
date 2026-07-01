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

  def errorMessages(code: String, isSecondStageAppeal: Boolean)(implicit messages: Messages, appConfig: AppConfig): String = {
    val suffix = if (isSecondStageAppeal) ".review" else ""
    code match {
    // S3 (via errorRedirect in staging): file is 0 bytes because no file was selected, violating minimumFileSize=1 in the signed S3 policy
    case "EntityTooSmall"     => messages(s"uploadEvidence.error.noFileSpecified$suffix")
    // S3 (via errorRedirect in staging): file exceeds the maximumFileSize limit in the signed S3 policy
    case "EntityTooLarge"     => messages("uploadEvidence.error.fileTooLarge", appConfig.upscanMaxFileSizeMB)
    // upscan-stub (locally): returned when the 'file' field is not found in the multipart form submission
    case "InvalidArgument"    => messages(s"uploadEvidence.error.noFileSpecified$suffix")
    // upscan-verify (via upscan-notify callback): ClamAV virus scan detected a virus in the file
    case "QUARANTINE"         => messages(s"uploadEvidence.error.QUARANTINE")
    // upscan-verify (via upscan-notify callback): file MIME type or extension is not on the allowed list
    case "REJECTED"           => messages(s"uploadEvidence.error.REJECTED")
    // UpscanCallbackController: filename contains characters outside [a-zA-Z0-9-_.]
    case "INVALID_FILENAME"   => messages(s"uploadEvidence.error.INVALID_FILENAME")
    // UpscanCallbackController: upscan-verify returned QUARANTINE with an "Encrypted" message, indicating a password-protected file
    case "PASSWORD_PROTECTED" => messages(s"uploadEvidence.error.PASSWORD_PROTECTED")
    case _                    => messages("uploadEvidence.error.unableToUpload")
    }
  }
}
