/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Reads}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

case class ReasonableExcuse(
                             `type`: String,
                             descriptionKey: String,
                             isOtherOption: Boolean
                           )

object ReasonableExcuse {
  val singularReads: Reads[ReasonableExcuse] = (json: JsValue) => {
    for {
      excuseType <- (json \ "type").validate[String]
      descriptionKey <- (json \ "descriptionKey").validate[String]
      isOtherOption = excuseType == "other"
    } yield {
      ReasonableExcuse(
        excuseType,
        descriptionKey,
        isOtherOption
      )
    }
  }

  val seqReads: Reads[Seq[ReasonableExcuse]] = (json: JsValue) => {
    (json \ "excuses").validate[Seq[ReasonableExcuse]](Reads.seq[ReasonableExcuse](singularReads))
  }

  def options(form: Form[_], reasonableExcuses: Seq[ReasonableExcuse], hintTextMessageKey: String,
              showHintText: Boolean)(implicit messages: Messages): Seq[RadioItem] =
    reasonableExcuses.map {
      value =>
        RadioItem(
          value = Some(value.`type`),
          content = Text(messages(value.descriptionKey)),
          checked = form("value").value.contains(value.`type`),
          hint = if(!showHintText) None else if(value.isOtherOption) Some(Hint(content = Text(messages(hintTextMessageKey)))) else None
        )
    }

  def optionsWithDivider(form: Form[_], messageKeyForDivider: String, reasonableExcuses: Seq[ReasonableExcuse], showAgentHintText: Boolean,
                         showHintText: Boolean)
                        (implicit messages: Messages): Seq[RadioItem] = {
    val otherOptionInSeq: ReasonableExcuse = reasonableExcuses.filter(_.isOtherOption).head
    val dividerPosition = reasonableExcuses.indexOf(otherOptionInSeq)
    val hintTextMessageKey = if(showAgentHintText) "agent.reasonableExcuses.otherReason.hintText" else "reasonableExcuses.otherReason.hintText"
    val optionsList = options(form, reasonableExcuses, hintTextMessageKey, showHintText)
    val divider = RadioItem(
      divider = Some(messages(messageKeyForDivider))
    )
    optionsList.take(dividerPosition) ++ Seq(divider) ++ optionsList.drop(dividerPosition)
  }
}
