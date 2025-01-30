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
    val p1 = "Use this page to upload any evidence to help us review the penalty."
    val p2 = "Evidence might include any documents or letters that show why the submission deadline was missed."
    val p3: Int => String = n => s"You can upload up to $n files."
    val p4 = "Each file must be smaller than 6MB."
    val label = "Select a file"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Evidence to support this appeal (Welsh)"
    override val p1 = "Use this page to upload any evidence to help us review the penalty. (Welsh)"
    override val p2 = "Evidence might include any documents or letters that show why the submission deadline was missed. (Welsh)"
    override val p3: Int => String = n => s"You can upload up to $n files. (Welsh)"
    override val p4 = "Each file must be smaller than 6MB. (Welsh)"
    override val label = "Select a file (Welsh)"
  }
}
