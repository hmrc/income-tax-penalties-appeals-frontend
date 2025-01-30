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

object MonthMessages {

  sealed trait Messages { _: i18n =>
    val january   = "January"
    val february  = "February"
    val march     = "March"
    val april     = "April"
    val may       = "May"
    val june      = "June"
    val july      = "July"
    val august    = "August"
    val september = "September"
    val october   = "October"
    val november  = "November"
    val december  = "December"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val january    = "Ionawr"
    override val february   = "Chwefror"
    override val march      = "Mawrth"
    override val april      = "Ebrill"
    override val may        = "Mai"
    override val june       = "Mehefin"
    override val july       = "Gorffennaf"
    override val august     = "Awst"
    override val september  = "Medi"
    override val october    = "Hydref"
    override val november   = "Tachwedd"
    override val december   = "Rhagfyr"
  }
}
