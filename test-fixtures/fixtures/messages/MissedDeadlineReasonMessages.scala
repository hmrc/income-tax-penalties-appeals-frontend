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

object MissedDeadlineReasonMessages {

  sealed trait Messages { _: i18n =>
    def headingAndTitle(isLPP: Boolean): String = {
      if(isLPP) "Why was the payment late?" else "Why was the submission deadline missed?"
    }
    def headingAndTitleSecondStage(isLPP: Boolean): String = {
      if(isLPP) "Why are you asking us to review this decision?" else "Why are you asking us to review this decision?"
    }

    def hintText(isLPP: Boolean): String = {
      if(isLPP) "We only need to know about this penalty. Any other penalty related to this update period should be appealed separately."
      else "We only need to know about this penalty. Other penalties should be appealed separately."
    }
    def hintTextSecondStage(isLPP: Boolean): String = {
      if(isLPP) "Briefly explain why you feel that the original appeal decision was incorrect."
      else "Briefly explain why you feel that the original appeal decision was incorrect."
    }
    def errorRequired(isLPP: Boolean): String = {
      if(isLPP) "You must provide some information about why the payment was late"
      else "You must provide some information about why the deadline was missed"
    }
    def errorRequiredSecondStage: String = {
      "You must provide some information about why you are asking us to review this decision"
    }
    def errorRequiredMultiple: String = {
      "You must provide some information about why you are asking us to review these decisions"
    }
    val errorLength: Int => String = n => s"Explain the reason in ${"%,d".format(n)} characters or fewer"
    val errorRegex: String = "The text must contain only letters, numbers and standard special characters"

    def cyaKey(isLPP: Boolean, is2ndStageAppeal: Boolean, isJointAppeal: Boolean): String =
      if(is2ndStageAppeal && isJointAppeal) "Why are you asking us to review these appeal decisions?"
      else if(is2ndStageAppeal) "Why are you asking us to review this decision?"
      else if(isLPP) "Why was the payment late?"
      else "Why was the submission deadline missed?"

    def cyaHidden(isLPP: Boolean, is2ndStageAppeal: Boolean, isJointAppeal: Boolean): String =
      if(is2ndStageAppeal && isJointAppeal) "why are you asking us to review these appeal decisions"
      else if(is2ndStageAppeal) "why are you asking us to review this decision"
      else if(isLPP) "why was the payment late"
      else "why was the submission deadline missed"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override def headingAndTitle(isLPP: Boolean): String = {
      if(isLPP) "Why was the payment late? (Welsh)" else "Why was the submission deadline missed? (Welsh)"
    }

    override def headingAndTitleSecondStage(isLPP: Boolean): String = {
      if(isLPP) "Why are you asking us to review this decision? (Welsh)" else "Why are you asking us to review this decision? (Welsh)"
    }

    override def hintText(isLPP: Boolean): String = {
      if(isLPP) "We only need to know about this penalty. Any other penalty related to this update period should be appealed separately. (Welsh)"
      else "We only need to know about this penalty. Other penalties should be appealed separately. (Welsh)"
    }

    override def hintTextSecondStage(isLPP: Boolean): String = {
      if(isLPP) "We only need to know about this penalty. Any other penalty related to this update period should be appealed separately. (Welsh)"
      else "Briefly explain why you feel that the original appeal decision was incorrect. (Welsh)"
    }

    override def errorRequired(isLPP: Boolean): String = {
      if(isLPP) "You must provide some information about why the payment was late (Welsh)"
      else "You must provide some information about why the deadline was missed (Welsh)"
    }
    override def errorRequiredSecondStage: String = {
      "You must provide some information about why you are asking us to review this decision (Welsh)"
    }

    override def errorRequiredMultiple: String = {
      "You must provide some information about why you are asking us to review these decisions (Welsh)"
    }

    override val errorLength: Int => String = n => s"Explain the reason in ${"%,d".format(n)} characters or fewer (Welsh)"
    override val errorRegex: String = "The text must contain only letters, numbers and standard special characters (Welsh)"

    override def cyaKey(isLPP: Boolean, is2ndStageAppeal: Boolean, isJointAppeal: Boolean): String =
      if(is2ndStageAppeal && isJointAppeal) "Why are you asking us to review these appeal decisions? (Welsh)"
      else if(is2ndStageAppeal) "Why are you asking us to review this decision? (Welsh)"
      else if(isLPP) "Why was the payment late? (Welsh)"
      else "Why was the submission deadline missed? (Welsh)"

    override def cyaHidden(isLPP: Boolean, is2ndStageAppeal: Boolean, isJointAppeal: Boolean): String =
      if(is2ndStageAppeal && isJointAppeal) "why are you asking us to review these appeal decisions (Welsh)"
      else if(is2ndStageAppeal) "why are you asking us to review this decision (Welsh)"
      else if(isLPP) "why was the payment late (Welsh)"
      else "why was the submission deadline missed (Welsh)"
  }
}
