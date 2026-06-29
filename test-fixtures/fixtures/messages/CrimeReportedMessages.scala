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

object CrimeReportedMessages {

  sealed trait Messages { this: i18n =>
    val unkownOption = "I do not know"
    val errorRequired = "Select yes if the crime has been reported to the police"
    val errorInvalid = "Select yes if the crime has been reported to the police"

    val cyaKey = "Reported to the police"
    val cyaHidden = "has this crime been reported to the police"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val unkownOption = "Nid wyf yn gwybod"
    override val errorRequired = "Dewiswch ‘Iawn’ os yw’r heddlu wedi cael gwybod am y drosedd hon"
    override val errorInvalid = "Dewiswch ‘Iawn’ os yw’r heddlu wedi cael gwybod am y drosedd hon"

    override val cyaKey = "Wedi rhoi gwybod i’r heddlu"
    override val cyaHidden = "A roddwyd gwybod i’r heddlu am y drosedd hon"
  }
}
