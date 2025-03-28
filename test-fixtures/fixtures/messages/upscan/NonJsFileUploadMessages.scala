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

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Evidence to support this appeal"
    val headingAndTitleReview = "Upload evidence"
    val p1 = "Use this page to upload any evidence to help us review this penalty."
    val p1Joint = "Use this page to upload any evidence to help us review these penalties."
    val p1Review = "Use this page to upload any evidence to help us review the appeal decision."
    val p1JointReview = "Use this page to upload any evidence to help us review the appeal decisions."
    val p2LSP = "Evidence might include any documents or letters that show why the submission deadline was missed."
    val p2LSPReview = "Evidence might include any documents or letters that show why the submission was sent late."
    val p2LPP = "Evidence might include any documents or letters that show why the payment deadline was missed."
    val p2LPPReview = "Evidence might include any documents or letters that show why the tax bill was paid late."
    val p3: Int => String = n => s"You can upload up to $n files."
    val p4: Int => String = n => s"Each file must be smaller than ${n}MB."
    val label = "Select a file"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Evidence to support this appeal (Welsh)"
    override val headingAndTitleReview = "Upload evidence (Welsh)"
    override val p1 = "Use this page to upload any evidence to help us review this penalty. (Welsh)"
    override val p1Joint = "Use this page to upload any evidence to help us review these penalties. (Welsh)"
    override val p1Review = "Use this page to upload any evidence to help us review the appeal decision. (Welsh)"
    override val p1JointReview = "Use this page to upload any evidence to help us review the appeal decisions. (Welsh)"
    override val p2LSP = "Evidence might include any documents or letters that show why the submission deadline was missed. (Welsh)"
    override val p2LSPReview = "Evidence might include any documents or letters that show why the submission was sent late. (Welsh)"
    override val p2LPP = "Evidence might include any documents or letters that show why the payment deadline was missed. (Welsh)"
    override val p2LPPReview = "Evidence might include any documents or letters that show why the tax bill was paid late. (Welsh)"
    override val p3: Int => String = n => s"You can upload up to $n files. (Welsh)"
    override val p4: Int => String = n => s"Each file must be smaller than ${n}MB. (Welsh)"
    override val label = "Select a file (Welsh)"
  }
}
