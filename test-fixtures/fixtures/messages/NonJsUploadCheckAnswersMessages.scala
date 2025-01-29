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

object NonJsUploadCheckAnswersMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitlePlural: Int => String = i => s"You have uploaded $i files"
    val headingAndTitleSingular = "You have uploaded 1 file"
    val summaryRowKey: Int => String = i => s"File $i"
    val uploadAnotherFileLegend = "Do you want to upload another file?"
    val errorRequired = "Tell us if you want to upload another file"
    val errorInvalid = "Tell us if you want to upload another file"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitlePlural: Int => String = i => s"Rydych wedi uwchlwytho $i o ffeiliau"
    override val headingAndTitleSingular = "Rydych wedi uwchlwytho 1 ffeil"
    override val summaryRowKey: Int => String = i => s"Ffeil $i"
    override val uploadAnotherFileLegend = "A ydych eisiau uwchlwytho ffeil arall?"
    override val errorRequired = "Rhowch wybod i ni a ydych eisiau uwchlwytho ffeil arall"
    override val errorInvalid = "Rhowch wybod i ni a ydych eisiau uwchlwytho ffeil arall"
  }
}
