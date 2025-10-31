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
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import scala.util.{Failure, Success, Try}

object ReviewMoreThan30DaysEnum extends Enumeration {
  val yes: ReviewMoreThan30DaysEnum.Value = Value
  val no: ReviewMoreThan30DaysEnum.Value = Value
  val unsure: ReviewMoreThan30DaysEnum.Value = Value

  implicit val format: Format[ReviewMoreThan30DaysEnum.Value] = new Format[ReviewMoreThan30DaysEnum.Value] {
    override def writes(o: ReviewMoreThan30DaysEnum.Value): JsValue = JsString(o.toString)
    override def reads(json: JsValue): JsResult[ReviewMoreThan30DaysEnum.Value] =
      Try(ReviewMoreThan30DaysEnum.withName(json.as[String])) match {
        case Failure(_) => JsError("Invalid ReviewMoreThan30DaysEnum value")
        case Success(value) => JsSuccess(value)
      }
  }

  def radioOptions()(implicit messages: Messages): Seq[RadioItem] = ReviewMoreThan30DaysEnum.values.toSeq.map { value =>
    RadioItem(
      content = Text(messages(s"review.decision.30.days.$value")),
      value = Some(value.toString)
    )
  }
}