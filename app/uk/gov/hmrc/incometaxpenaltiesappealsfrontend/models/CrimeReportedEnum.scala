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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import play.api.i18n.Messages
import play.api.libs.json.{Format, JsResult, JsString, JsValue}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object CrimeReportedEnum extends Enumeration {
  val yes: CrimeReportedEnum.Value = Value
  val no: CrimeReportedEnum.Value = Value
  val unknown: CrimeReportedEnum.Value = Value

  val apply: String => CrimeReportedEnum.Value = {
    case "yes" => yes
    case "no" => no
    case "unknown" => unknown
    case x => throw new IllegalArgumentException(s"Invalid crime reported value of '$x'")
  }

  implicit val format: Format[CrimeReportedEnum.Value] = new Format[CrimeReportedEnum.Value] {
    override def writes(o: CrimeReportedEnum.Value): JsValue = JsString(o.toString)
    override def reads(json: JsValue): JsResult[CrimeReportedEnum.Value] = json.validate[String].map(apply)
  }

  def radioOptions()(implicit messages: Messages): Seq[RadioItem] = Seq(yes, no, unknown).map { value =>
    RadioItem(
      content = Text(messages(s"crimeReason.$value")),
      value = Some(value.toString)
    )
  }
}
