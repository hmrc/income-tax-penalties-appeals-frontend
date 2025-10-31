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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReviewMoreThan30DaysEnum

import scala.util.Try

object ReviewMoreThan30DaysForm extends Mappings {

  val key = "decisionReview"


  def form(isMultipleAppeal: Boolean)(implicit messages: Messages): Form[ReviewMoreThan30DaysEnum.Value] = {

    val multipleAppeal = if(isMultipleAppeal){".multiple"}else{""}

    Form[ReviewMoreThan30DaysEnum.Value](
      single(
        ReviewMoreThan30DaysForm.key -> text(messages(s"review.decision.30.days.error.required$multipleAppeal"))
          .verifying(messages(s"review.decision.30.days.error.invalid$multipleAppeal"), value => Try(ReviewMoreThan30DaysEnum.withName(value)).isSuccess)
          .transform[ReviewMoreThan30DaysEnum.Value](ReviewMoreThan30DaysEnum.withName, _.toString)
      )
    )
  }
}