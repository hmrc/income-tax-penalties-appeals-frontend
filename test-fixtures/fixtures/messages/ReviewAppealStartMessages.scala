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
    val p1 = "If you disagree with the decision about the penalty appeal, you can ask for a review."
    val h2 = "Before you start"
    val p2 = "Collect any evidence that you believe shows why the appeal decision was incorrect."
    val p3 = "You can upload this evidence as part of this request to review."
    val p4 = "If it’s been more than 30 days since the review decision, you’ll need to explain why you did not ask for a review sooner."
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Adolygu penderfyniad ynghylch apêl"
    override val p1 = "Os ydych chi’n anghytuno â’r penderfyniad ynghylch yr apêl yn erbyn cosb, gallwch ofyn am adolygiad."
    override val h2 = "Cyn i chi ddechrau"
    override val p2 = "Casglwch unrhyw dystiolaeth sy’n dangos bod y penderfyniad ynghylch eich apêl yn anghywir, yn eich barn chi."
    override val p3 = "Gallwch uwchlwythor dystiolaeth hon fel rhan or cais hwn i gael adolygiad."
    override val p4 = "Os oes mwy na 30 diwrnod wedi mynd heibio ers penderfyniad yr adolygiad, bydd angen i chi roi manylion dros beidio â gofyn am adolygiad yn gynt."
  }
}
