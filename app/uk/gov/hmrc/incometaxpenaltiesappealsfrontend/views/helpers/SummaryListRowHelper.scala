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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

trait SummaryListRowHelper extends RichActionItemImplicits {

  def summaryListRow(label: String,
                     value: Html,
                     classes: String = "govuk-summary-list__row",
                     actions: Option[Actions] = None): SummaryListRow = SummaryListRow(
    key = Key(
      content = Text(label),
      classes = "govuk-summary-list__key"
    ),
    value = Value(
      content = HtmlContent(value),
      classes = "govuk-summary-list__value"
    ),
    classes = classes,
    actions = actions
  )
}
