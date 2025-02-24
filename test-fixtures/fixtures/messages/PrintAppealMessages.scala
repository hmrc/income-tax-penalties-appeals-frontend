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

object PrintAppealMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Appeal details"
    val nino = "National Insurance number"
    val penaltyAppealed = "Penalty appealed"
    val appealDate = "Appeal date"
    val warn1 = "Print or download this page if you want to keep it."
    val warn2 = "You will not be able to return to these appeal details later."
    val printThisPage = "Print this page"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Appeal details (Welsh)"
    override val nino = "National Insurance number (Welsh)"
    override val penaltyAppealed = "Penalty appealed (Welsh)"
    override val appealDate = "Appeal date (Welsh)"
    override val warn1 = "Print or download this page if you want to keep it. (Welsh)"
    override val warn2 = "You will not be able to return to these appeal details later. (Welsh)"
    override val printThisPage = "Print this page (Welsh)"
  }
}
