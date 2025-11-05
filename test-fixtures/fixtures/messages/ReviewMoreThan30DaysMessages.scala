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

object ReviewMoreThan30DaysMessages {

  sealed trait Messages { this: i18n =>

    val headingAndTitle = "Has it been more than 30 days since the appeal decision was issued?"
    val headingAndTitleMultiple = "Has it been more than 30 days since the appeal decisions were issued?"
    val hintText = "You usually need to ask for a review within 30 days of the date of the decision."
    val unkownOption = "I’m not sure"
    val errorRequired = "Tell us if it has been more than 30 days since the appeal decision was issued"
    val errorInvalid = "Tell us if it has been more than 30 days since the appeal decision was issued"
    val errorRequiredMultiple = "Tell us if it has been more than 30 days since the appeal decisions were issued"
    val errorInvalidMultiple = "Tell us if it has been more than 30 days since the appeal decisions were issued"

    val cyaKey = "Has it been more than 30 days since the appeal decision was issued?"
    val cyaHidden = "has it been more than 30 days since the appeal decision was issued"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Oes mwy na 30 diwrnod wedi mynd heibio ers cyhoeddi penderfyniad yr apêl?"
    override val headingAndTitleMultiple = "Oes mwy na 30 diwrnod wedi mynd heibio ers cyhoeddi penderfyniadau’r apêl?"
    override val hintText = "Fel arfer, mae’n rhaid i chi ofyn am adolygiad cyn pen 30 diwrnod o ddyddiad y penderfyniad.."
    override val unkownOption = "Dydw i ddim yn siŵr"
    override val errorRequired = "Rhowch wybod i ni os yw mwy na 30 diwrnod wedi mynd heibio ers cyhoeddi penderfyniad yr apêl"
    override val errorInvalid = "Rhowch wybod i ni os yw mwy na 30 diwrnod wedi mynd heibio ers cyhoeddi penderfyniad yr apêl"
    override val errorRequiredMultiple = "Rhowch wybod i ni os yw mwy na 30 diwrnod wedi mynd heibio ers i’r penderfyniadau apêl gael eu cyhoeddi"
    override val errorInvalidMultiple = "Rhowch wybod i ni os yw mwy na 30 diwrnod wedi mynd heibio ers i’r penderfyniadau apêl gael eu cyhoeddi"

    override val cyaKey = "Oes mwy na 30 diwrnod wedi mynd heibio ers cyhoeddi penderfyniad yr apêl?"
    override val cyaHidden = "oes mwy na 30 diwrnod wedi mynd heibio ers cyhoeddi penderfyniad yr apêl"
  }
}
