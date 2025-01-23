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

    val reasonMessage: String => String = {
      case "bereavement" => "bereavement message"
      case "crime" => "crime message"
    }

    def errorMessageConstructor(reasonableExcuse: String, suffix: String, missing: Option[String] = None, missingTwo: Option[String] = None ): String = {
      reasonableExcuse match {
//        case "bereavementReason" => (bereavementReason + suffix)
//        case "bereavementReason" => if (suffix == "Invalid") bereavementReasonInvalid  else if (suffix == "RequiredAll") bereavementReasonRequiredAll

        case "bereavementReason" => suffix match {
          case "Invalid" => bereavementReasonInvalid
          case "RequiredAll" => bereavementReasonRequiredAll
          case "RequiredTwo" => bereavementReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => bereavementReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => bereavementReasonNotInFuture
        }
        case "crimeReason" => suffix match {
          case "Invalid" => crimeReasonInvalid
          case "RequiredAll" => crimeReasonRequiredAll
          case "RequiredTwo" => crimeReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => crimeReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => crimeReasonNotInFuture
        }
        case "fireOrFloodReason" => suffix match {
          case "Invalid" => fireOrFloodReasonInvalid
          case "RequiredAll" => fireOrFloodReasonRequiredAll
          case "RequiredTwo" => fireOrFloodReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => fireOrFloodReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => fireOrFloodReasonNotInFuture
        }
        case "technicalReason" => suffix match {
          case "Invalid" => technicalReasonInvalid
          case "RequiredAll" => technicalReasonRequiredAll
          case "RequiredTwo" => technicalReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => technicalReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => technicalReasonNotInFuture
        }
        case "cessationReason" => suffix match {
          case "Invalid" => cessationReasonInvalid
          case "RequiredAll" => cessationReasonRequiredAll
          case "RequiredTwo" => cessationReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => cessationReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => cessationReasonNotInFuture
        }
        case "healthReason" => suffix match {
          case "Invalid" => healthReasonInvalid
          case "RequiredAll" => healthReasonRequiredAll
          case "RequiredTwo" => healthReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => healthReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => healthReasonNotInFuture
        }
        case "unexpectedHospitalReason" => suffix match {
          case "Invalid" => unexpectedHospitalReasonInvalid
          case "RequiredAll" => unexpectedHospitalReasonRequiredAll
          case "RequiredTwo" => unexpectedHospitalReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => unexpectedHospitalReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => unexpectedHospitalReasonNotInFuture
        }
        case "otherReason" => suffix match {
          case "Invalid" => otherReasonInvalid
          case "RequiredAll" => otherReasonRequiredAll
          case "RequiredTwo" => otherReasonRequiredTwo(missing.getOrElse(""), missingTwo.getOrElse(""))
          case "Required" => otherReasonRequired(missing.getOrElse(""))
          case "NotInFuture" => otherReasonNotInFuture
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

  }
}
