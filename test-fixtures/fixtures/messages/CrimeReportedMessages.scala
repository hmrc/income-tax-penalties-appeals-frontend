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

  sealed trait Messages { _: i18n =>
    val unkownOption = "I do not know"
    val errorRequired = "Tell us if the police have been told about the crime"
    val errorInvalid = "Tell us if the police have been told about the crime"

    val cyaKey = "Has this crime been reported to the police?"
    val cyaHidden = "has this crime been reported to the police"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val unkownOption = "Nid wyf yn gwybod"
    override val errorRequired = "Tell us if the police have been told about the crime (Welsh)"
    override val errorInvalid = "Tell us if the police have been told about the crime (Welsh)"

    override val cyaKey = "Has this crime been reported to the police? (Welsh)"
    override val cyaHidden = "has this crime been reported to the police (Welsh)"
  }
}
