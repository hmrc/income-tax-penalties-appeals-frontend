/*
 * Copyright 2024 HM Revenue & Customs
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

/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.mappings.Mappings
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{DateFormatter, TimeMachine}

import java.time.LocalDate

object WhenDidEventEndForm extends Mappings {

  val key = "date"

  def form(reason: ReasonableExcuse, startDate: LocalDate, isLPP: Boolean = false)(implicit messages: Messages, appConfig: AppConfig, timeMachine: TimeMachine): Form[LocalDate] = {

    def lspLppInfix: String = (reason, isLPP) match {
      case (Other, lpp) => if (lpp) ".lpp" else ".lsp"
      case _ => ""
    }

    Form(
      key -> localDate(
        invalidKey = s"whenDidEventEnd.$reason$lspLppInfix.end.date.error.invalid",
        allRequiredKey = s"whenDidEventEnd.$reason$lspLppInfix.end.date.error.required.all",
        twoRequiredKey = s"whenDidEventEnd.$reason$lspLppInfix.end.date.error.required.two",
        requiredKey = s"whenDidEventEnd.$reason$lspLppInfix.end.date.error.required",
        futureKey = Some(s"whenDidEventEnd.$reason$lspLppInfix.end.date.error.notInFuture"),
        //Using the messages API as it's easier to pass in the startDate message param
        dateNotEqualOrAfterKeyAndCompareDate = Some((messages(s"whenDidEventEnd.$reason$lspLppInfix.end.date.error.endDateLessThanStartDate", DateFormatter.dateToString(startDate)), startDate))
      )
    )
  }
}
