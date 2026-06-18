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

object LateAppealMessages {

  sealed trait Messages { this: i18n =>
    val errorRequired: Int => String = days => s"Enter why you could not appeal within $days days"
    val errorRequiredReview: Int => String = days => s"Enter why you have not asked for a review within $days days"
    val errorLength: (Int, Int) => String = (days, chars) => s"Why you could not appeal within $days days must be ${"%,d".format(chars)} characters or less"
    val errorLengthReview: (Int, Int) => String = (days, chars) => s"Why you have not ask for a review within $days days must be ${"%,d".format(chars)} characters or less"
    val errorRegex: Int => String = days => s"Why you could not appeal within $days days must only include letters a to z, numbers 0 to 9 and standard special characters"
    val errorRegexReview: Int => String = days => s"Why you have not ask for a review within $days days must only include letters a to z, numbers 0 to 9, and standard special characters"

    val cyaKey: Int => String = i => s"Reason for appealing after $i days"
    val cyaHidden: Int => String = i => s"reason for appealing after $i days"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val errorRequired: Int => String = days => s"Nodwch y rheswm pam nad oeddech yn gallu apelio cyn pen $days o ddiwrnodau"
    override val errorRequiredReview: Int => String = days => s"Nodwch y rheswm pam nad ydych wedi gofyn am adolygiad cyn pen $days o ddiwrnodau"

    override val errorLength: (Int, Int) => String = (days, chars) => s"Mae’n rhaid i’r rheswm pam nad oeddech yn gallu apelio cyn pen $days o ddiwrnodau fod yn ${"%,d".format(chars)} o gymeriadau neu’n llai"
    override val errorLengthReview: (Int, Int) => String = (days, chars) => s"Mae’n rhaid i’r rheswm pam nad ydych wedi gofyn am adolygiad cyn pen $days o ddiwrnodau fod yn ${"%,d".format(chars)} o gymeriadau neu’n llai"
    override val errorRegex: Int => String = days => s"Mae’n rhaid i’r rheswm pam nad oeddech yn gallu apelio cyn pen $days o ddiwrnodau gynnwys y llythrennau a i z, y rhifau 0 i 9 a chymeriadau arbennig safonol yn unig"
    override val errorRegexReview: Int => String = days => s"Mae’n rhaid i’r rheswm pam nad ydych wedi gofyn am adolygiad cyn pen $days o ddiwrnodau gynnwys y llythrennau a i z, y rhifau 0 i 9 a chymeriadau arbennig safonol yn unig"

    override val cyaKey: Int => String = i => s"Rheswm dros apelio ar ôl $i o ddiwrnodau"
    override val cyaHidden: Int => String = i => s"rheswm dros apelio ar ôl $i o ddiwrnodau"
  }
}
