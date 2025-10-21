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

import fixtures.messages.{Cy, En}

object NonJsRemoveFileMessages {

  sealed trait Messages {
    val headingAndTitle: Int => String = i => s"Are you sure you want to remove file $i?"
    val filenameHint: String => String = name => s"Filename: $name"
    val errorRequired = "Select yes if you want to remove this file"
    val errorInvalid = "Select yes if you want to remove this file"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle: Int => String = i => s"A ydych yn siŵr eich bod am dynnu ffeil $i?"
    override val filenameHint: String => String = name => s"Enw’r ffeil: $name"
    override val errorRequired = "Dewiswch ‘Iawn’ os ydych am dynnu’r ffeil hon"
    override val errorInvalid = "Dewiswch ‘Iawn’ os ydych am dynnu’r ffeil hon"
  }
}
