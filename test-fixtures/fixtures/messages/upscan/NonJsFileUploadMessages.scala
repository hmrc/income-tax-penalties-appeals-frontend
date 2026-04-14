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
    val headingAndTitle = "Upload evidence to support this appeal"
    val headingAndTitleReview = "Upload evidence to support this review"
    val p1LSP = "Upload evidence that explains why the submission deadline was missed. Evidence could be a letter or email."
    val heading3 = "Files you can upload"
    val p1LPP = "Upload evidence that explains why the payment deadline was missed. Evidence could be a letter or email."
    val p3: (Int, Int) => String = (n, m) => s"You can upload up to $n files. Each file must be smaller than ${m}MB."
    val label = "Upload a file"

  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Uwchlwythwch dystiolaeth i gefnogi’r apêl hon"
    override val headingAndTitleReview = "Uwchlwythwch dystiolaeth i gefnogi’r adolygiad hwn"
    override val p1LSP = "Upload evidence that explains why the submission deadline was missed. Evidence could be a letter or email. (Welsh)"
    override val heading3 = "Ffeiliau y mae modd i chi eu huwchlwytho"
    override val p1LPP = "Upload evidence that explains why the payment deadline was missed. Evidence could be a letter or email. (Welsh)"
    override val p3: (Int, Int) => String = (n, m) => s"Gallwch uwchlwytho hyd at $n ffeil. Mae’n rhaid i bob ffeil fod yn llai na ${m}MB."
    override val label: String = "Uwchlwytho ffeil"
  }
}
