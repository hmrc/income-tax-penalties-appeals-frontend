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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms

import play.api.data.Form
import play.api.data.Forms.single
import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.mappings.Mappings
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ExtraEvidenceEnum

import scala.util.Try

object ExtraEvidenceForm extends Mappings {

  val key = "extraEvidence"

  def form()(implicit messages: Messages): Form[ExtraEvidenceEnum.Value] = Form[ExtraEvidenceEnum.Value](
    single(
      ExtraEvidenceForm.key -> text(messages("extraEvidence.error.required"))
        .verifying(messages("extraEvidence.error.invalid"), value => Try(ExtraEvidenceEnum.withName(value)).isSuccess)
        .transform[ExtraEvidenceEnum.Value](ExtraEvidenceEnum.withName, _.toString)
    )
  )
}
