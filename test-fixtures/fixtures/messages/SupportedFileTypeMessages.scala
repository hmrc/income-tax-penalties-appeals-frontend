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

  sealed trait Messages {
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
    override val summaryHeading = "Y mathau o ffeiliau y gallwch eu huwchlwytho"
    override val p1 = "Dyma’r ffeiliau y gallwch eu huwchlwytho:"
    override val bullet1 = "delwedd (.jpg, .jpeg, .png or .tiff)"
    override val bullet2 = "PDF (.pdf)"
    override val bullet3 = "e-bost (.txt neu .msg)"
    override val bullet4 = "Microsoft (Word, Excel neu PowerPoint)"
    override val bullet5 = "Open Document Format (ODF)"
  }
}
