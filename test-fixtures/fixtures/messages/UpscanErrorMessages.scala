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

object UpscanErrorMessages {

  sealed trait Messages { this: i18n =>
    val errorFileTooSmall: String = "The selected file is empty"
    val errorFileTooLarge: Int => String = max => s"The selected file must be smaller than ${max}MB"
    val errorNoFileSelected: String = "Select evidence to support this appeal"
    val errorNoFileSelectedReview: String = "Select evidence to support this review"
    val errorUploadFailed: String = "The selected file could not be uploaded – try again"
    val errorQuarantine: String = "The selected file contains a virus"
    val errorRejected: String = "The selected file must be a JPG, PNG, TIFF, PDF, TXT, Word, Excel, Powerpoint or Open Document Format (ODF). Choose another file"
    val errorFilename: String = "Filenames can only contain upper and lower case letters, digits from 0-9, hyphens, underscores and full stops"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val errorFileTooSmall: String = "Mae’r ffeil dan sylw yn wag"
    override val errorFileTooLarge: Int => String = max => s"Mae’n rhaid i’r ffeil dan sylw fod yn llai na ${max}MB"
    override val errorNoFileSelected: String = "Dewiswch dystiolaeth i gefnogi’r apêl hon"
    override val errorNoFileSelectedReview: String = "Dewiswch dystiolaeth i gefnogi’r adolygiad hwn"
    override val errorUploadFailed: String = "Nid oedd modd uwchlwytho’r ffeil dan sylw – rhowch gynnig arall arni"
    override val errorQuarantine: String = "Mae feirws yn y ffeil dan sylw"
    override val errorRejected: String = "Mae’n rhaid i’r ffeil dan sylw fod yn JPG, PNG, TIFF, PDF, TXT, Word, Excel, Powerpoint neu Fformat Dogfen Agored (ODF)"
    override val errorFilename: String = "Gall enw’r ffeil dim ond cynnwys llythrennau mawr a bach, rhifau 0-9, cysylltnodau, tanlinellau ac atalnodau llawn"
  }
}
