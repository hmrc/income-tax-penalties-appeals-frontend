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

object ReasonableExcuseMessages {

  sealed trait Messages { _: i18n =>
    val titleAndHeading = "What was the reason for missing the submission deadline?"
    val bereavement: String = "Bereavement (someone died)"
    val cessation: String = "Cessation of income source"
    val crime: String = "Crime"
    val fireOrFlood: String = "Fire or flood"
    val health: String = "Serious or life-threatening ill health"
    val technical: String = "Software or technology issues"
    val unexpectedHospital: String = "Unexpected hospital stay"
    val other: String = "The reason does not fit into any of the other categories"
    val errorRequired: String = "You must select an option"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val titleAndHeading = "What was the reason for missing the submission deadline? (Welsh)"
    override val bereavement: String = "Bereavement (someone died) (Welsh)"
    override val cessation: String = "Cessation of income source (Welsh)"
    override val crime: String = "Crime (Welsh)"
    override val fireOrFlood: String = "Fire or flood (Welsh)"
    override val health: String = "Serious or life-threatening ill health (Welsh)"
    override val technical: String = "Software or technology issues (Welsh)"
    override val unexpectedHospital: String = "Unexpected hospital stay (Welsh)"
    override val other: String = "The reason does not fit into any of the other categories (Welsh)"
    override val errorRequired: String = "You must select an option (Welsh)"
  }
}
