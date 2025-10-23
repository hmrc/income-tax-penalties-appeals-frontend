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

package fixtures.messages.upscan

import fixtures.messages.{Cy, En, i18n}

object NonJsFileUploadMessages {

  sealed trait Messages { this: i18n =>
    val headingAndTitle = "Evidence to support this appeal"
    val headingAndTitleReview = "Upload evidence"
    val p1 = "Use this page to upload any evidence to help us review this penalty."
    val p1Joint = "Use this page to upload any evidence to help us review these penalties."
    val p1Review = "Use this page to upload any evidence to help us review the appeal decision."
    val p1JointReview = "Use this page to upload any evidence to help us review the appeal decisions."
    val p2LSP = "Evidence might include any documents or letters that show why the submission deadline was missed."
    val p2LSPReview = "Evidence might include any documents or letters that show why the submission was sent late."
    val p2LPP = "Evidence might include any documents or letters that show why the payment deadline was missed."
    val p2LPPReview = "Evidence might include any documents or letters that show why the payment deadline was missed."
    val p3: Int => String = n => s"You can upload up to $n files."
    val p4: Int => String = n => s"Each file must be smaller than ${n}MB."
    val label = "Select a file"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Tystiolaeth i ategu’r apêl hon"
    override val headingAndTitleReview = "Uwchlwytho tystiolaeth"
    override val p1 = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu ni i adolygu’r gosb."
    override val p1Joint = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu ni i adolygu’r cosbau hyn."
    override val p1Review = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu i adolygu’r gosb."
    override val p1JointReview = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu i adolygu’r penderfyniad ynghylch apêl."
    override val p2LSP = "Gall tystiolaeth gynnwys dogfennau neu lythyrau sy’n dangos pam y cafodd y dyddiad cau ar gyfer cyflwyno ei fethu."
    override val p2LSPReview = "Gall tystiolaeth gynnwys dogfennau neu lythyrau sy’n dangos pam y cafodd y cyflwyniad ei anfon yn hwyr."
    override val p2LPP = "Gall tystiolaeth gynnwys dogfennau neu lythyrau sy’n dangos pam y cafodd y dyddiad cau ar gyfer talu ei fethu."
    override val p2LPPReview = "Gall tystiolaeth gynnwys dogfennau neu lythyrau sy’n dangos pam y cafodd y dyddiad cau ar gyfer talu ei fethu."
    override val p3: Int => String = n => s"Gallwch uwchlwytho hyd at $n ffeil."
    override val p4: Int => String = n => s"Mae’n rhaid i bob ffeil fod yn llai na ${n}MB."
    override val label = "Dewiswch ffeil"
  }
}
