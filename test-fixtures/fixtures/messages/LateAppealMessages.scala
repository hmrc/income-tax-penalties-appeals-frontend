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

  sealed trait Messages {
    val errorRequired: String = "You must provide some information about why you did not appeal sooner"
    val errorRequiredReview: String = "You must provide some information about why you did not ask for a review sooner"
    val errorLength: Int => String = n => s"Explain the reason in ${"%,d".format(n)} characters or fewer"
    val errorRegex: String = "The text must contain only letters, numbers and standard special characters"

    val cyaKey: Int => String = i => s"Reason for appealing after $i days"
    val cyaHidden: Int => String = i => s"reason for appealing after $i days"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val errorRequired: String = "Mae’n rhaid i chi roi ychydig o wybodaeth i ni ynglŷn â pham nad oeddech wedi apelio’n gynt"
    override val errorRequiredReview: String = "Mae’n rhaid i chi roi gwybodaeth i ni ynghylch pam nad oeddech wedi gofyn am adolygiad yn gynt"

    override val errorLength: Int => String = n => s"Esboniwch y rheswm gan ddefnyddio ${"%,d".format(n)} o gymeriadau neu lai"
    override val errorRegex: String = "Mae’n rhaid i’r testun gynnwys llythrennau, rhifau a chymeriadau arbennig safonol yn unig"

    override val cyaKey: Int => String = i => s"Rheswm dros apelio ar ôl $i o ddiwrnodau"
    override val cyaHidden: Int => String = i => s"rheswm dros apelio ar ôl $i o ddiwrnodau"
  }
}
