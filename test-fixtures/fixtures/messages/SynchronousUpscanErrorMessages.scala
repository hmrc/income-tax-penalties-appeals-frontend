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

object SynchronousUpscanErrorMessages {

  sealed trait Messages { _: i18n =>
    val errorFileTooSmall: String = "The selected file is empty. Choose another file."
    val errorFileTooLarge: String = "The selected file must be smaller than 6MB. Choose another file."
    val errorNoFileSelected: String = "Select a file."
    val errorUploadFailed: String = "The selected file could not be uploaded. Choose another file."
    val errorQuarantine: String = "The selected file contains a virus. Choose another file."
    val errorRejected: String = "The selected file must be a JPG, PNG, TIFF, PDF, TXT, MSG, Word, Excel, Powerpoint or Open Document Format (ODF). Choose another file."
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val errorFileTooSmall: String = "Mae’r ffeil dan sylw yn wag. Dewiswch ffeil arall."
    override val errorFileTooLarge: String = "Mae’n rhaid i’r ffeil dan sylw fod yn llai na 6 MB. Dewiswch ffeil arall."
    override val errorNoFileSelected: String = "Dewiswch ffeil."
    override val errorUploadFailed: String = "Nid oedd modd uwchlwytho’r ffeil dan sylw. Dewiswch ffeil arall."
    override val errorQuarantine: String = "Mae feirws yn y ffeil dan sylw. Dewiswch ffeil arall."
    override val errorRejected: String = "Mae’n rhaid i’r ffeil dan sylw fod yn JPG, PNG, TIFF, PDF, TXT, MSG, Word, Excel, Powerpoint neu Fformat Dogfen Agored (ODF)"
  }
}
