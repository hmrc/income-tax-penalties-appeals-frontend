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

object SupportedFileTypeMessages {

  sealed trait Messages { _: i18n =>
    val summaryHeading = "Types of file you can upload"
    val p1 = "These file types are allowed:"
    val bullet1 = "image (.jpg, .jpeg, .png or .tiff)"
    val bullet2 = "PDF (.pdf)"
    val bullet3 = "email (.txt or .msg)"
    val bullet4 = "Microsoft (Word, Excel or PowerPoint)"
    val bullet5 = "Open Document Format (ODF)"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val summaryHeading = "Types of file you can upload (Welsh)"
    override val p1 = "These file types are allowed: (Welsh)"
    override val bullet1 = "image (.jpg, .jpeg, .png or .tiff) (Welsh)"
    override val bullet2 = "PDF (.pdf) (Welsh)"
    override val bullet3 = "email (.txt or .msg) (Welsh)"
    override val bullet4 = "Microsoft (Word, Excel or PowerPoint) (Welsh)"
    override val bullet5 = "Open Document Format (ODF) (Welsh)"
  }
}
