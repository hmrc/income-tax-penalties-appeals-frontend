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

  sealed trait Messages { this: i18n =>

    def headingAndTitle(is2ndStage: Boolean): String = {
      if (is2ndStage) {
        "Do you want to upload evidence to support this review?"
      } else {
        "Do you want to upload evidence to support your appeal?"
      }
    }
    
    def hintText(is2ndStage: Boolean, isJointAppeal: Boolean = false): String = {
      if (is2ndStage) {
        if (isJointAppeal) {
          "Uploading evidence is optional. We will still review the original appeal decisions if you do not upload evidence."
        } else {
          "Uploading evidence is optional. We will still review the original appeal decision if you do not upload evidence."
        }
      } else {
        "We will still review your appeal if you do not upload evidence."
      }
    }

    val errorRequired = "Select yes if you want to upload evidence to support your appeal"
    val errorInvalid = "Select yes if you want to upload evidence to support your appeal"
    val errorRequiredReview = "Select yes if you want to upload evidence to support this review"
    val errorInvalidReview = "Select yes if you want to upload evidence to support this review"

    def errorMessage(is2ndStage: Boolean): String = {
      if (is2ndStage) {
        "Select yes if you want to upload evidence to support this review"
      } else {
        "Select yes if you want to upload evidence to support your appeal"
      }
    }

    val cyaKey = "Evidence uploaded"
    val cyaHidden = "do you want to upload evidence to support your appeal"
    val cyaKeyReview = "Evidence uploaded"
    val cyaHiddenReview = "do you want to upload evidence to support this review?"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    override def headingAndTitle(is2ndStage: Boolean): String = {
      if (is2ndStage) {
        "A ydych am uwchlwytho tystiolaeth i ategu’r adolygiad hwn?"
      } else {
        "A ydych am uwchlwytho tystiolaeth i ategu’ch apêl?"
      }
    }

    override def hintText(is2ndStage: Boolean, isJointAppeal: Boolean = false): String = {
      if (is2ndStage) {
        "Mae uwchlwytho tystiolaeth yn ddewisol. Byddwn yn dal i adolygu’r penderfyniad ynghylch yr apêl wreiddiol os nad ydych yn uwchlwytho tystiolaeth."
      } else {
        "Byddwn yn dal i adolygu’ch apêl os nad ydych yn uwchlwytho tystiolaeth."
      }
    }

    override val errorRequired = "Dewiswch ‘Iawn’ os ydych am uwchlwytho tystiolaeth i gefnogi’ch apêl"
    override val errorInvalid = "Dewiswch ‘Iawn’ os ydych am uwchlwytho tystiolaeth i gefnogi’ch apêl"
    override val errorRequiredReview = "Dewiswch ‘Iawn’ os ydych am uwchlwytho tystiolaeth i gefnogi’r adolygiad hwn"
    override val errorInvalidReview = "Dewiswch ‘Iawn’ os ydych am uwchlwytho tystiolaeth i gefnogi’r adolygiad hwn"

    override val cyaKey = "Tystiolaeth wedi’i uwchlwytho"
    override val cyaHidden = "a ydych am uwchlwytho tystiolaeth i ategu’ch apêl"
    override val cyaKeyReview = "Tystiolaeth wedi’i uwchlwytho"
    override val cyaHiddenReview = "a ydych am uwchlwytho tystiolaeth i ategu’r adolygiad hwn"
  }
}
