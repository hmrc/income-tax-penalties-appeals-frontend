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

  sealed trait Messages { this: i18n =>
    val summaryHeading = "Types of file you can upload"
    val p1 = "These file types are allowed:"
    val p1New = "Your files can be:"
    val bullet1 = "image (.jpg, .jpeg, .png or .tiff)"
    val bullet2 = "PDF (.pdf)"
    val bullet3 = "email (.txt or .msg)"
    val bullet4 = "Microsoft Word, Excel, or PowerPoint (.doc, .docx, .xls, .xlsx, .ppt, or .pptx)"
    val bullet5 = "Open Document Format files (.odt, .ods or .odp)"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val summaryHeading = "Y mathau o ffeiliau y gallwch eu huwchlwytho"
    override val p1 = "Dyma’r ffeiliau y gallwch eu huwchlwytho:"
    override val p1New = "Gall eich ffeiliau fod yn un o’r canlynol:"
    override val bullet1 = "delweddau (.jpg, .jpeg, .png neu .tiff)"
    override val bullet2 = "PDF (.pdf)"
    override val bullet3 = "e-byst (.txt or .msg)"
    override val bullet4 = "Microsoft Word, Excel, neu PowerPoint (.doc, .docx, .xls, .xlsx, .ppt, neu .pptx)"
    override val bullet5 = "Ffeiliau Fformat Dogfen Agored (.odt, .ods neu .odp)"
  }
}
