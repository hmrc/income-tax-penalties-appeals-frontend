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

object WhenDidEventHappenMessages {

  sealed trait Messages { _: i18n =>

    def errorMessageConstructor(reasonableExcuse: String, suffix: String, args: String*): String = {
      reasonableExcuse match {
        case "bereavementReason" => suffix match {
          case "invalid" => bereavementReasonInvalid
          case "required.all" => bereavementReasonRequiredAll
          case "required.two" => bereavementReasonRequiredTwo(args.head, args(1))
          case "required" => bereavementReasonRequired(args.head)
          case "notInFuture" => bereavementReasonNotInFuture
        }
        case "crimeReason" => suffix match {
          case "invalid" => crimeReasonInvalid
          case "required.all" => crimeReasonRequiredAll
          case "required.two" => crimeReasonRequiredTwo(args.head, args(1))
          case "required" => crimeReasonRequired(args.head)
          case "notInFuture" => crimeReasonNotInFuture
        }
        case "fireOrFloodReason" => suffix match {
          case "invalid" => fireOrFloodReasonInvalid
          case "required.all" => fireOrFloodReasonRequiredAll
          case "required.two" => fireOrFloodReasonRequiredTwo(args.head, args(1))
          case "required" => fireOrFloodReasonRequired(args.head)
          case "notInFuture" => fireOrFloodReasonNotInFuture
        }
        case "technicalReason" => suffix match {
          case "invalid" => technicalReasonInvalid
          case "required.all" => technicalReasonRequiredAll
          case "required.two" => technicalReasonRequiredTwo(args.head, args(1))
          case "required" => technicalReasonRequired(args.head)
          case "notInFuture" => technicalReasonNotInFuture
        }
        case "cessationReason" => suffix match {
          case "invalid" => cessationReasonInvalid
          case "required.all" => cessationReasonRequiredAll
          case "required.two" => cessationReasonRequiredTwo(args.head, args(1))
          case "required" => cessationReasonRequired(args.head)
          case "notInFuture" => cessationReasonNotInFuture
        }
        case "healthReason" => suffix match {
          case "invalid" => healthReasonInvalid
          case "required.all" => healthReasonRequiredAll
          case "required.two" => healthReasonRequiredTwo(args.head, args(1))
          case "required" => healthReasonRequired(args.head)
          case "notInFuture" => healthReasonNotInFuture
        }
        case "unexpectedHospitalReason" => suffix match {
          case "invalid" => unexpectedHospitalReasonInvalid
          case "required.all" => unexpectedHospitalReasonRequiredAll
          case "required.two" => unexpectedHospitalReasonRequiredTwo(args.head, args(1))
          case "required" => unexpectedHospitalReasonRequired(args.head)
          case "notInFuture" => unexpectedHospitalReasonNotInFuture
        }
        case "otherReason" => suffix match {
          case "invalid" => otherReasonInvalid
          case "required.all" => otherReasonRequiredAll
          case "required.two" => otherReasonRequiredTwo(args.head, args(1))
          case "required" => otherReasonRequired(args.head)
          case "notInFuture" => otherReasonNotInFuture
        }
      }
    }

    val crimeReasonInvalid = "The date of the crime must be a real date"
    val fireOrFloodReasonInvalid = "The date of the fire or flood must be a real date"
    val technicalReasonInvalid = "The date the software or technology issues began must be a real date"
    val bereavementReasonInvalid = "The date the person died must be a real date"
    val cessationReasonInvalid = "TBC"
    val healthReasonInvalid = "TBC"
    val unexpectedHospitalReasonInvalid = "TBC"
    val otherReasonInvalid = "TBC"

    val crimeReasonRequiredAll = "Enter the date of the crime"
    val fireOrFloodReasonRequiredAll = "Enter the date of the fire or flood"
    val technicalReasonRequiredAll = "Enter the date when the software or technology issues began"
    val bereavementReasonRequiredAll = "Enter the date when the person died"
    val cessationReasonRequiredAll = "TBC"
    val healthReasonRequiredAll = "TBC"
    val unexpectedHospitalReasonRequiredAll = "TBC"
    val otherReasonRequiredAll = "TBC"

    def crimeReasonRequiredTwo(missing: String, missingTwo: String) = s"The date of the crime must include a $missing and a $missingTwo"
    def fireOrFloodReasonRequiredTwo(missing: String, missingTwo: String) = s"The date of the fire or flood must include a $missing and a $missingTwo"
    def technicalReasonRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo"
    def bereavementReasonRequiredTwo(missing: String, missingTwo: String) = s"The date the person died must include a $missing and a $missingTwo"
    def cessationReasonRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def healthReasonRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def unexpectedHospitalReasonRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def otherReasonRequiredTwo(missing: String, missingTwo: String) = "TBC"


    def crimeReasonRequired(missing: String) = s"The date of the crime must include a $missing"
    def fireOrFloodReasonRequired(missing: String) = s"The date of the fire or flood must include a $missing"
    def technicalReasonRequired(missing: String) = s"The date the software or technology issues began must include a $missing"
    def bereavementReasonRequired(missing: String) = s"The date the person died must include a $missing"
    def cessationReasonRequired(missing: String) = "TBC"
    def healthReasonRequired(missing: String) = "TBC"
    def unexpectedHospitalReasonRequired(missing: String) = "TBC"
    def otherReasonRequired(missing: String) = "TBC"

    val crimeReasonNotInFuture = "The date of the crime must be today or in the past"
    val fireOrFloodReasonNotInFuture = "The date of the fire or flood must be today or in the past"
    val technicalReasonNotInFuture = "The date the software or technology issues began must be today or in the past"
    val bereavementReasonNotInFuture = "The date the person died must be today or in the past"
    val cessationReasonNotInFuture = "TBC"
    val healthReasonNotInFuture = "TBC"
    val unexpectedHospitalReasonNotInFuture = "TBC"
    val otherReasonNotInFuture = "TBC"

  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val crimeReasonInvalid = "The date of the crime must be a real date (Welsh)"
    override val fireOrFloodReasonInvalid = "The date of the fire or flood must be a real date (Welsh)"
    override val technicalReasonInvalid = "The date the software or technology issues began must be a real date (Welsh)"
    override val bereavementReasonInvalid = "The date the person died must be a real date (Welsh)"
    override val cessationReasonInvalid = "TBC (Welsh)"
    override val healthReasonInvalid = "TBC (Welsh)"
    override val unexpectedHospitalReasonInvalid = "TBC (Welsh)"
    override val otherReasonInvalid = "TBC (Welsh)"

    override val crimeReasonRequiredAll = "Enter the date of the crime (Welsh)"
    override val fireOrFloodReasonRequiredAll = "Enter the date of the fire or flood (Welsh)"
    override val technicalReasonRequiredAll = "Enter the date when the software or technology issues began (Welsh)"
    override val bereavementReasonRequiredAll = "Enter the date when the person died (Welsh)"
    override val cessationReasonRequiredAll = "TBC (Welsh)"
    override val healthReasonRequiredAll = "TBC (Welsh)"
    override val unexpectedHospitalReasonRequiredAll = "TBC (Welsh)"
    override val otherReasonRequiredAll = "TBC (Welsh)"

    override def crimeReasonRequiredTwo(missing: String, missingTwo: String) = s"The date of the crime must include a $missing and a $missingTwo (Welsh)"
    override def fireOrFloodReasonRequiredTwo(missing: String, missingTwo: String) = s"The date of the fire or flood must include a $missing and a $missingTwo (Welsh)"
    override def technicalReasonRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo (Welsh)"
    override def bereavementReasonRequiredTwo(missing: String, missingTwo: String) = s"The date the person died must include a $missing and a $missingTwo (Welsh)"
    override def cessationReasonRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def healthReasonRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def unexpectedHospitalReasonRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def otherReasonRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"

    override def crimeReasonRequired(missing: String) = s"The date of the crime must include a $missing (Welsh)"
    override def fireOrFloodReasonRequired(missing: String) = s"The date of the fire or flood must include a $missing (Welsh)"
    override def technicalReasonRequired(missing: String) = s"The date the software or technology issues began must include a $missing (Welsh)"
    override def bereavementReasonRequired(missing: String) = s"The date the person died must include a $missing (Welsh)"
    override def cessationReasonRequired(missing: String) = "TBC (Welsh)"
    override def healthReasonRequired(missing: String) = "TBC (Welsh)"
    override def unexpectedHospitalReasonRequired(missing: String) = "TBC (Welsh)"
    override def otherReasonRequired(missing: String) = "TBC (Welsh)"

    override val crimeReasonNotInFuture = "The date of the crime must be today or in the past (Welsh)"
    override val fireOrFloodReasonNotInFuture = "The date of the fire or flood must be today or in the past (Welsh)"
    override val technicalReasonNotInFuture = "The date the software or technology issues began must be today or in the past (Welsh)"
    override val bereavementReasonNotInFuture = "The date the person died must be today or in the past (Welsh)"
    override val cessationReasonNotInFuture = "TBC (Welsh)"
    override val healthReasonNotInFuture = "TBC (Welsh)"
    override val unexpectedHospitalReasonNotInFuture = "TBC (Welsh)"
    override val otherReasonNotInFuture = "TBC (Welsh)"
  }
}
