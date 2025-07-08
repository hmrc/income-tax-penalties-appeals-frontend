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

object WhatCausedYouToMissDeadlineMessages {

  sealed trait Messages { _: i18n =>
    val titleAndHeading = "What caused you to miss the deadline?"
    val agent: String = "Something else happened to delay me"
    val client: String = "My client did not get information to me on time"
    val errorRequired: String = "Tell us the reason the return was submitted late"
    val errorInvalid: String = "Tell us the reason the return was submitted late"

    val cyaKey = "What caused you to miss the deadline?"
    val cyaHidden = "what caused you to miss the deadline"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val titleAndHeading = "Beth oedd wedi’ch achosi i fethu’r dyddiad cau?"
    override val agent: String = "Rheswm arall"
    override val client: String = "Nid oedd fy nghleient wedi rhoi’r wybodaeth i mi mewn pryd"
    override val errorRequired: String = "Rhowch y rheswm dros gyflwyno’r cyflwyniad yn hwyr"
    override val errorInvalid: String = "Rhowch y rheswm dros gyflwyno’r cyflwyniad yn hwyr"

    override val cyaKey = "Beth oedd wedi’ch achosi i fethu’r dyddiad cau?"
    override val cyaHidden = "Beth oedd wedi’ch achosi i fethu’r dyddiad cau"
  }
}
