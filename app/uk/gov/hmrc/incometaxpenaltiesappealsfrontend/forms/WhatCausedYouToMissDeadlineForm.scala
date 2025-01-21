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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms

import play.api.data.Form
import play.api.data.Forms.single
import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.mappings.Mappings
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.AgentClientEnum

import scala.util.Try

object WhatCausedYouToMissDeadlineForm extends Mappings {

  val key = "whatCausedYouToMissTheDeadline"

  def form()(implicit messages: Messages): Form[AgentClientEnum.Value] = Form[AgentClientEnum.Value](
    single(
      key -> text(messages("agents.whatCausedYouToMissTheDeadline.error.required"))
        .verifying(messages("agents.whatCausedYouToMissTheDeadline.error.invalid"), x => Try(AgentClientEnum.withName(x)).isSuccess)
        .transform[AgentClientEnum.Value](AgentClientEnum.withName, _.toString)
    )
  )
}
