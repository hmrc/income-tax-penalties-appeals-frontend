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

object WhenDidEventEndMessages {

  sealed trait Messages { _: i18n =>

    def errorMessageConstructor(suffix: String, missing: Option[String] = None, missingTwo: Option[String] = None, startDate:Option[String] = None ): String = {
      suffix match {
        case "Invalid" => technicalReasonInvalid
        case "RequiredAll" => technicalReasonRequiredAll
        case "RequiredTwo" => technicalReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
        case "Required" => technicalReasonRequired(missing.getOrElse(""))
        case "NotInFuture" => technicalReasonNotInFuture
        case "EndDateLessThanStartDate" => technicalReasonEndDateLessThanStartDate(startDate.getOrElse(""))
      }
    }

    val technicalReasonInvalid = "The date the software or technology issues ended must be a real date"
    val technicalReasonRequiredAll = "Tell us when the software or technology issues ended"
    def technicalReasonRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues ended must include a $missing and a $missingTwo"
    def technicalReasonRequired(missing: String) = s"The date the software or technology issues ended must include a $missing"
    val technicalReasonNotInFuture = "The date the software or technology issues ended must be today or in the past"
    def technicalReasonEndDateLessThanStartDate(startDate:String) = s"The date the software or technology issues ended must be $startDate or later"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    override val technicalReasonInvalid = "The date the software or technology issues ended must be a real date (Welsh)"
    override val technicalReasonRequiredAll = "Tell us when the software or technology issues ended (Welsh)"
    override def technicalReasonRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues ended must include a $missing and a $missingTwo (Welsh)"
    override def technicalReasonRequired(missing: String) = s"The date the software or technology issues ended must include a $missing (Welsh)"
    override val technicalReasonNotInFuture = "The date the software or technology issues ended must be today or in the past (Welsh)"
    override def technicalReasonEndDateLessThanStartDate(startDate:String) = s"The date the software or technology issues ended must be $startDate or later (Welsh)"
  }
}
