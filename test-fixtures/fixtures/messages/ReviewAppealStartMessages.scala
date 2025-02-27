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

  sealed trait Messages { _: i18n =>
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
    override val headingAndTitle = "Review an appeal decision (Welsh)"
    override val p1 = "If you disagree with the decision of your penalty appeal, you can ask for your case to be reviewed again. (Welsh)"
    override val p2 = "This service is for requesting a review for appeal decisions given for individual submissions. (Welsh)"
    override val h2 = "Before you start (Welsh)"
    override val p3 = "You’ll need to collect any evidence that you believe shows why the appeal decision was incorrect. (Welsh)"
    override val p4 = "You will be asked to upload this evidence as part of this request to review. (Welsh)"
    override val p3List = "You’ll need: (Welsh)"
    override val bullet1 = "any evidence that you believe shows why the appeal decision was incorrect (Welsh)"
    override val bullet2 = "details of why you did not ask for a review sooner (Welsh)"
  }
}
