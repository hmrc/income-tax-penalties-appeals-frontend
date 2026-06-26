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

object JointAppealMessages {

  sealed trait Messages { this: i18n =>
    val errorRequired = "Select yes if you want to appeal both penalties for the same reason"
    val errorInvalid = "Select yes if you want to appeal both penalties for the same reason"
    val errorRequiredReview = "Select yes if you want both appeal decisions to be reviewed at the same time"
    val errorInvalidReview = "Select yes if you want both appeal decisions to be reviewed at the same time"

    val cyaKey = "Appeal both penalties for the same reason"
    val cyaHidden = "do you want to appeal both penalties for the same reason"
    val cyaKeyReview = "Review both appeal decisions at the same time"
    val cyaHiddenReview = "do you want both appeal decisions to be reviewed at the same time"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val errorRequired = "Dewiswch ‘Iawn’ os ydych am apelio yn erbyn y ddwy gosb am yr un rheswm"
    override val errorInvalid = "Dewiswch ‘Iawn’ os ydych am apelio yn erbyn y ddwy gosb am yr un rheswm"
    override val errorRequiredReview = "Dewiswch ‘Iawn’ os ydych am i ddau benderfyniad yr apêl gael eu hadolygu ar yr un pryd"
    override val errorInvalidReview = "Dewiswch ‘Iawn’ os ydych am i ddau benderfyniad yr apêl gael eu hadolygu ar yr un pryd"

    override val cyaKey = "Adolygu penderfyniadau’r ddwy apêl ar yr un pryd"
    override val cyaHidden = "A ydych am apelio yn erbyn y ddwy gosb am yr un rheswm"
    override val cyaKeyReview = "Nodwch y rhif hwn os ydych yn cysylltu â CThEF ynghylch yr apêl hon"
    override val cyaHiddenReview = "A ydych am i’r ddau benderfyniad gael eu hadolygu ar yr un pryd?"
  }
}
