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

object HasHospitalStayEndedMessages {

  sealed trait Messages { _: i18n =>
    
    val headingAndTitle = "Has the hospital stay ended?"
    val errorRequired = "Tell us if the hospital stay has ended"
    val errorInvalid = "Tell us if the hospital stay has ended"

    val cyaKey = "Has the hospital stay ended?"
    val cyaHidden = "has the hospital stay ended"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    
    override val headingAndTitle = "Has the hospital stay ended? (Welsh)"
    override val errorRequired = "Tell us if the hospital stay has ended (Welsh)"
    override val errorInvalid = "Tell us if the hospital stay has ended (Welsh)"

    override val cyaKey = "Has the hospital stay ended? (Welsh)"
    override val cyaHidden = "has the hospital stay ended (Welsh)"
  }
}
