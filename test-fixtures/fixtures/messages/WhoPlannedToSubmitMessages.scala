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

object WhoPlannedToSubmitMessages {

  sealed trait Messages { _: i18n =>
    val titleAndHeading = "Before the deadline, who planned to send the submission?"
    val agent: String = "I did"
    val client: String = "My client did"
    val errorRequired: String = "Tell us who planned to send the submission"
    val errorInvalid: String = "Tell us who planned to send the submission"

    val cyaKey = "Before the deadline, who planned to send the submission?"
    val cyaHidden = "before the deadline, who planned to send the submission"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val titleAndHeading = "Cyn y dyddiad cau, pwy oedd yn bwriadu anfon y cyflwyniad?"
    override val agent: String = "Fi"
    override val client: String = 	"Fy nghleient"
    override val errorRequired: String = "Rhowch wybod i ni pwy oedd yn bwriadu cyflwyno’r cyflwyniad"
    override val errorInvalid: String = "Rhowch wybod i ni pwy oedd yn bwriadu cyflwyno’r cyflwyniad"

    override val cyaKey = "Cyn y dyddiad cau, pwy oedd yn bwriadu anfon y cyflwyniad?"
    override val cyaHidden = "Cyn y dyddiad cau, pwy oedd yn bwriadu anfon y cyflwyniad"
  }
}
