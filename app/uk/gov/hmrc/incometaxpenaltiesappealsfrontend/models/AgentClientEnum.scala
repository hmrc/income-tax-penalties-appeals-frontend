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
import play.api.libs.json._
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import scala.util.{Failure, Success, Try}

object AgentClientEnum extends Enumeration {
  val agent: AgentClientEnum.Value = Value
  val client: AgentClientEnum.Value = Value

  implicit val format: Format[AgentClientEnum.Value] = new Format[AgentClientEnum.Value] {
    override def writes(o: AgentClientEnum.Value): JsValue = JsString(o.toString)
    override def reads(json: JsValue): JsResult[AgentClientEnum.Value] =
      Try(AgentClientEnum.withName(json.as[String])) match {
        case Failure(_) => JsError("Invalid WhoPlannedToSubmitEnum value")
        case Success(value) => JsSuccess(value)
      }
  }

  def radioOptions(page: String, reverse: Boolean = false)(implicit messages: Messages): Seq[RadioItem] = {
    val values = if (reverse) AgentClientEnum.values.toSeq.reverse else AgentClientEnum.values.toSeq
    values.map { value =>
      RadioItem(
        content = Text(messages(s"agents.$page.${value.toString}")),
        value = Some(value.toString)
      )
    }
  }
}
