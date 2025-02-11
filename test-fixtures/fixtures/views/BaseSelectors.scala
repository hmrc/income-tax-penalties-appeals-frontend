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
  def prefix = ""
  def title: String = "title"
  def h1: String = "h1"
  def h2: Int => String = i => s"$prefix h2:nth-of-type($i)"
  def p: Int => String = i => s"$prefix p:nth-of-type($i)"
  def bullet: Int => String = i => s"$prefix ul li:nth-of-type($i)"
  def details: String = s"$prefix details"
  def detailsSummary: String = s"$prefix $details summary"
  def label: String => String = input => s"$prefix label[for=$input]"
  def button: String = s"$prefix button.govuk-button"
  def legend: String = s"$prefix fieldset legend"
  def hint: String = s"$prefix div.govuk-hint"
  def warning: String = s"$prefix div.govuk-warning-text strong"
  def radio: Int => String = i => s"$prefix div.govuk-radios__item:nth-of-type($i) label"
  def summaryRowKey: Int => String = i => s"$prefix dl > div:nth-of-type($i) > dt"
  def summaryRowValue: Int => String = i => s"$prefix dl > div:nth-of-type($i) > dd:nth-of-type(1)"
  def summaryRowAction: (Int, Int) => String = (i, n) => s"$prefix dl > div:nth-of-type($i) > dd:nth-of-type(2) a:nth-of-type($n)"
}

object BaseSelectors extends BaseSelectors