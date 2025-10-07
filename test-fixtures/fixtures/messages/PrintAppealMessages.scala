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

package fixtures.messages

object PrintAppealMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Appeal details"
    val headingAndTitleReview = "Review details"
    val nino = "National Insurance number"
    val penaltyAppealed = "Penalty appealed"
    val appealDate = "Appeal date"
    val appealDateReview = "Review request sent"
    val warn1 = "Print or download this page if you want to keep it."
    val warn2 = "You will not be able to return to these appeal details later."
    val warn2Review = "You will not be able to return to these review details later."
    val printThisPage = "Print this page"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Manylion yr apêl"
    override val headingAndTitleReview = "Gwirio’r manylion"
    override val nino = "Rhif Yswiriant Gwladol"
    override val penaltyAppealed = "Y gosb sydd wedi’i hapelio"
    override val appealDate = "Dyddiad yr apêl"
    override val appealDateReview = "Cais am adolygiad wedi’i anfon"
    override val warn1 = "Argraffwch neu lawrlwythwch y dudalen hon os hoffech ei chadw."
    override val warn2 = "Ni fyddwch yn gallu dychwelyd i’r manylion hyn o ran yr apêl yn nes ymlaen."
    override val warn2Review = "Ni fyddwch yn gallu dychwelyd i’r dudalen ar gyfer y manylion hyn yn nes ymlaen."
    override val printThisPage = "Argraffu’r dudalen hon"
  }
}
