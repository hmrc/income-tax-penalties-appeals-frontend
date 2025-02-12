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

object ExtraEvidenceMessages {

  sealed trait Messages { _: i18n =>
    val errorRequired = "Tell us if you want to upload evidence"
    val errorInvalid = "Tell us if you want to upload evidence"

    val cyaKey = "Do you want to upload evidence to support your appeal?"
    val cyaHidden = "do you want to upload evidence to support your appeal?"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val errorRequired = "Tell us if you want to upload evidence (Welsh)"
    override val errorInvalid = "Tell us if you want to upload evidence (Welsh)"

    override val cyaKey = "Do you want to upload evidence to support your appeal? (Welsh)"
    override val cyaHidden = "do you want to upload evidence to support your appeal? (Welsh)"
  }
}
