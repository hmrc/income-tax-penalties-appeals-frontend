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

import play.api.data.Form
import play.api.data.Forms.single
import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.mappings.Mappings
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Regexes
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.MissedDeadlineReasonHelper.messageKeyPrefix


object MissedDeadlineReasonForm extends Mappings {

  val key = "missedDeadlineReason"

  def form(isLPP: Boolean, isSecondStageAppeal: Boolean, isMultipleAppeal: Boolean)(implicit appConfig: AppConfig, messages: Messages): Form[String] = {
    val prefix = if (isSecondStageAppeal) ".review" else ""
    val multipleAppeal = if (isMultipleAppeal) ".multiple" else ""
    Form[String](
      single(
        key -> text(messages(s"${messageKeyPrefix(isLPP)}.error$prefix.required$multipleAppeal"))
          .verifying(
            error = messages(s"${messageKeyPrefix(isLPP)}.error.length", appConfig.numberOfCharsInTextArea),
            constraint = _.length <= appConfig.numberOfCharsInTextArea
          )
          .verifying(
            error = messages(s"${messageKeyPrefix(isLPP)}.error.regex"),
            constraint = _.matches(Regexes.textArea)
          )
      )
    )
  }
}
