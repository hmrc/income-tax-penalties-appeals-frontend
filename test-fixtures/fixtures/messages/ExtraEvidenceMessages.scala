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

  sealed trait Messages {

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

    val errorRequired = "Tell us if you want to upload evidence to support your appeal"
    val errorInvalid = "Tell us if you want to upload evidence to support your appeal"
    val errorRequiredReview = "Tell us if you want to upload evidence to support this review"
    val errorInvalidReview = "Tell us if you want to upload evidence to support this review"

    def errorMessage(is2ndStage: Boolean): String = {
      if (is2ndStage) {
        "Tell us if you want to upload evidence to support this review"
      } else {
        "Tell us if you want to upload evidence to support your appeal"
      }
    }

    val cyaKey = "Do you want to upload evidence to support your appeal?"
    val cyaHidden = "do you want to upload evidence to support your appeal"
    val cyaKeyReview = "Do you want to upload evidence to support this review?"
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
        "Mae uwchlwytho tystiolaeth yn ddewisol. Byddwn yn dal i adolygu’r apêl wreiddiol os nad ydych yn uwchlwytho tystiolaeth."
      } else {
        "Byddwn yn dal i adolygu’ch apêl os nad ydych yn uwchlwytho tystiolaeth."
      }
    }

    override val errorRequired = "Rhowch wybod i ni os ydych am uwchlwytho tystiolaeth i ategu’r apêl"
    override val errorInvalid = "Rhowch wybod i ni os ydych am uwchlwytho tystiolaeth i ategu’r apêl"
    override val errorRequiredReview = "Rhowch wybod i ni os ydych am uwchlwytho tystiolaeth i ategu’r adolygiad"
    override val errorInvalidReview = "Rhowch wybod i ni os ydych am uwchlwytho tystiolaeth i ategu’r adolygiad"

    override val cyaKey = "A ydych am uwchlwytho tystiolaeth i ategu’ch apêl?"
    override val cyaHidden = "a ydych am uwchlwytho tystiolaeth i ategu’ch apêl"
    override val cyaKeyReview = "A ydych am uwchlwytho tystiolaeth i ategu’r adolygiad hwn?"
    override val cyaHiddenReview = "a ydych am uwchlwytho tystiolaeth i ategu’r adolygiad hwn"
  }
}
