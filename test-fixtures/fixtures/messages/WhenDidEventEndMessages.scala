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

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{TechnicalIssues, UnexpectedHospital}

object WhenDidEventEndMessages {

  sealed trait Messages { _: i18n =>

    def errorMessageConstructor(suffix: String, reason: ReasonableExcuse, args: String*): String = {
      reason match {
        case UnexpectedHospital => suffix match {
          case "invalid" => unexpectedHospitalInvalid
          case "required.all" => unexpectedHospitalRequiredAll
          case "required.two" => unexpectedHospitalRequiredTwo(args.head, args(1))
          case "required" => unexpectedHospitalRequired(args.head)
          case "notInFuture" => unexpectedHospitalNotInFuture
          case "endDateLessThanStartDate" => unexpectedHospitalEndDateLessThanStartDate(args.head)
        }
        case TechnicalIssues => suffix match {
          case "invalid" => technicalIssueInvalid
          case "required.all" => technicalIssueRequiredAll
          case "required.two" => technicalIssueRequiredTwo(args.head, args(1))
          case "required" => technicalIssueRequired(args.head)
          case "notInFuture" => technicalIssueNotInFuture
          case "endDateLessThanStartDate" => technicalIssueEndDateLessThanStartDate(args.head)
        }
        case reason => throw new UnsupportedOperationException("No message content exists for this ReasonableExcuse: " + reason)
      }
    }
    
    def headingAndTitle(reasonableExcuse: ReasonableExcuse): String = reasonableExcuse match {
      case TechnicalIssues => "When did the software or technology issues end?"
      case UnexpectedHospital => "When did the hospital stay end?"
      case reason => throw new UnsupportedOperationException("No message content exists for this ReasonableExcuse: " + reason)
    }
    
    val cyaKeyTechnical = "When did the software or technology issues end?"
    val cyaKeyHospital = "TBC unexpectedHospital"

    def cyaKey(reasonableExcuse: ReasonableExcuse): String = reasonableExcuse match {
      case TechnicalIssues => cyaKeyTechnical
      case UnexpectedHospital => cyaKeyHospital
      case reason => throw new UnsupportedOperationException("No message content exists for this ReasonableExcuse: " + reason)
    }

    val cyaHiddenTechnical = "when did the software or technology issues end"
    val cyaHiddenHospital = "TBC unexpectedHospital"

    def cyaHidden(reasonableExcuse: ReasonableExcuse): String = reasonableExcuse match {
      case TechnicalIssues => cyaHiddenTechnical
      case UnexpectedHospital => cyaHiddenHospital
      case reason => throw new UnsupportedOperationException("No message content exists for this ReasonableExcuse: " + reason)
    }

    val technicalIssueInvalid = "The date the software or technology issues ended must be a real date"
    val technicalIssueRequiredAll = "Tell us when the software or technology issues ended"
    def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues ended must include a $missing and a $missingTwo"
    def technicalIssueRequired(missing: String) = s"The date the software or technology issues ended must include a $missing"
    val technicalIssueNotInFuture = "The date the software or technology issues ended must be today or in the past"
    def technicalIssueEndDateLessThanStartDate(startDate:String) = s"The date the software or technology issues ended must be $startDate or later"

    val unexpectedHospitalInvalid = "The date that the hospital stay ended must be a real date"
    val unexpectedHospitalRequiredAll = "Tell us when the hospital stay ended"
    def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = s"The date that the hospital stay ended must include a $missing and a $missingTwo"
    def unexpectedHospitalRequired(missing: String) = s"The date that the hospital stay ended must include a $missing"
    val unexpectedHospitalNotInFuture = "The date that the hospital stay ended must be today or in the past"
    def unexpectedHospitalEndDateLessThanStartDate(startDate:String) = s"The date that the hospital stay ended must be $startDate or later"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    override val technicalIssueInvalid = "The date the software or technology issues ended must be a real date (Welsh)"
    override val technicalIssueRequiredAll = "Tell us when the software or technology issues ended (Welsh)"
    override def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues ended must include a $missing and a $missingTwo (Welsh)"
    override def technicalIssueRequired(missing: String) = s"The date the software or technology issues ended must include a $missing (Welsh)"
    override val technicalIssueNotInFuture = "The date the software or technology issues ended must be today or in the past (Welsh)"
    override def technicalIssueEndDateLessThanStartDate(startDate:String) = s"The date the software or technology issues ended must be $startDate or later (Welsh)"

    override val unexpectedHospitalInvalid = "The date that the hospital stay ended must be a real date (Welsh)"
    override val unexpectedHospitalRequiredAll = "Tell us when the hospital stay ended (Welsh)"
    override def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = s"The date that the hospital stay ended must include a $missing and a $missingTwo (Welsh)"
    override def unexpectedHospitalRequired(missing: String) = s"The date that the hospital stay ended must include a $missing (Welsh)"
    override val unexpectedHospitalNotInFuture = "The date that the hospital stay ended must be today or in the past (Welsh)"
    override def unexpectedHospitalEndDateLessThanStartDate(startDate:String) = s"The date that the hospital stay ended must be $startDate or later (Welsh)"

    override val cyaKeyTechnical = "When did the software or technology issues end? (Welsh)"
    override val cyaKeyHospital = "TBC unexpectedHospital (Welsh)"

    override val cyaHiddenTechnical = "when did the software or technology issues end (Welsh)"
    override val cyaHiddenHospital = "TBC unexpectedHospital (Welsh)"

    override def headingAndTitle(reasonableExcuse: ReasonableExcuse): String = reasonableExcuse match {
      case TechnicalIssues => "When did the software or technology issues end?"
      case UnexpectedHospital => "When did the hospital stay end?"
      case reason => throw new UnsupportedOperationException("No message content exists for this ReasonableExcuse: " + reason)
    }
  }
}
