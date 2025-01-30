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

package fixtures.views

trait BaseSelectors {
  val title: String = "title"
  val h1: String = "h1"
  val h2: Int => String = i => s"h2:nth-of-type($i)"
  val p: Int => String = i => s"p:nth-of-type($i)"
  val bullet: Int => String = i => s"ul li:nth-of-type($i)"
  val details: String = "details"
  val detailsSummary: String = s"$details summary"
  val label: String => String = input => s"label[for=$input]"
  val button: String = "button.govuk-button"
  val legend: String = "fieldset legend"
  val hint: String = "div.govuk-hint"
  val radio: Int => String = i => s"div.govuk-radios__item:nth-of-type($i) label"
  val summaryRowKey: Int => String = i => s"dl > div:nth-of-type($i) > dt"
  val summaryRowValue: Int => String = i => s"dl > div:nth-of-type($i) > dd:nth-of-type(1)"
  val summaryRowAction: (Int, Int) => String = (i, n) => s"dl > div:nth-of-type($i) > dd:nth-of-type(2) a:nth-of-type($n)"
}

object BaseSelectors extends BaseSelectors