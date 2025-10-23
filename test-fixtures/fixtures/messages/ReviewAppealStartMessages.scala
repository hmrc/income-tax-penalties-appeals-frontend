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

object ReviewAppealStartMessages {

  sealed trait Messages { this: i18n =>
    val headingAndTitle = "Review an appeal decision"
    val p1 = "If you disagree with the decision of your penalty appeal, you can ask for your case to be reviewed again."
    val p2 = "This service is for requesting a review for appeal decisions given for individual submissions."
    val h2 = "Before you start"
    val p3 = "You’ll need to collect any evidence that you believe shows why the appeal decision was incorrect."
    val p4 = "You will be asked to upload this evidence as part of this request to review."
    val p3List = "You’ll need:"
    val bullet1 = "any evidence that you believe shows why the appeal decision was incorrect"
    val bullet2 = "details of why you did not ask for a review sooner"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Adolygu penderfyniad ynghylch apêl"
    override val p1 = "Os ydych yn anghytuno â’r penderfyniad ynghylch eich apêl, gallwch ofyn am i’ch achos gael ei adolygu eto."
    override val p2 = "Mae’r gwasanaeth hwn ar gyfer gofyn am adolygiad o benderfyniadau ynghylch apeliadau sy’n gysylltiedig â chyflwyniadau a wnaed gan unigolyn."
    override val h2 = "Cyn i chi ddechrau"
    override val p3 = "Bydd angen i chi gasglu unrhyw dystiolaeth sy’n dangos bod y penderfyniad ynghylch eich apêl yn anghywir, yn eich barn chi."
    override val p4 = "Bydd gofyn i chi uwchlwytho’r dystiolaeth hon fel rhan o’ch cais i gael adolygiad."
    override val p3List = "Bydd angen y canlynol arnoch:"
    override val bullet1 = "unrhyw dystiolaeth sy’n dangos bod y penderfyniad ynghylch eich apêl yn anghywir, yn eich barn chi"
    override val bullet2 = "rheswm dros beidio â gofyn am adolygiad yn gynt"
  }
}
