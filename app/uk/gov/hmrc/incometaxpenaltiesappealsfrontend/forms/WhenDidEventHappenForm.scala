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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.mappings.Mappings
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.WhenDidEventHappenHelper

import java.time.LocalDate

object WhenDidEventHappenForm extends Mappings with WhenDidEventHappenHelper {

  val key = "date"

  def form(reason: ReasonableExcuse, isLPP: Boolean = false)(implicit user: CurrentUserRequestWithAnswers[_], messages: Messages, appConfig: AppConfig, timeMachine: TimeMachine): Form[LocalDate] =
    Form(
      key -> localDate(
        invalidKey = s"${messageKeyPrefix(reason, isLPP)}.date.error.invalid",
        allRequiredKey = s"${messageKeyPrefix(reason, isLPP)}.date.error.required.all",
        twoRequiredKey = s"${messageKeyPrefix(reason, isLPP)}.date.error.required.two",
        requiredKey = s"${messageKeyPrefix(reason, isLPP)}.date.error.required",
        futureKey = Some(s"${messageKeyPrefix(reason, isLPP)}.date.error.notInFuture")
      )
    )
}
