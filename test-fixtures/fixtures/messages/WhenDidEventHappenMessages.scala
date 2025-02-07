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
        case "bereavement" => suffix match {
          case "invalid" => bereavementInvalid
          case "required.all" => bereavementRequiredAll
          case "required.two" => bereavementRequiredTwo(args.head, args(1))
          case "required" => bereavementRequired(args.head)
          case "notInFuture" => bereavementNotInFuture
        }
        case "crime" => suffix match {
          case "invalid" => crimeInvalid
          case "required.all" => crimeRequiredAll
          case "required.two" => crimeRequiredTwo(args.head, args(1))
          case "required" => crimeRequired(args.head)
          case "notInFuture" => crimeNotInFuture
        }
        case "fireOrFlood" => suffix match {
          case "invalid" => fireOrFloodInvalid
          case "required.all" => fireOrFloodRequiredAll
          case "required.two" => fireOrFloodRequiredTwo(args.head, args(1))
          case "required" => fireOrFloodRequired(args.head)
          case "notInFuture" => fireOrFloodNotInFuture
        }
        case "technicalIssues" => suffix match {
          case "invalid" => technicalIssueInvalid
          case "required.all" => technicalIssueRequiredAll
          case "required.two" => technicalIssueRequiredTwo(args.head, args(1))
          case "required" => technicalIssueRequired(args.head)
          case "notInFuture" => technicalIssueNotInFuture
        }
        case "cessation" => suffix match {
          case "invalid" => cessationInvalid
          case "required.all" => cessationRequiredAll
          case "required.two" => cessationRequiredTwo(args.head, args(1))
          case "required" => cessationRequired(args.head)
          case "notInFuture" => cessationNotInFuture
        }
        case "health" => suffix match {
          case "invalid" => healthInvalid
          case "required.all" => healthRequiredAll
          case "required.two" => healthRequiredTwo(args.head, args(1))
          case "required" => healthRequired(args.head)
          case "notInFuture" => healthNotInFuture
        }
        case "unexpectedHospital" => suffix match {
          case "invalid" => unexpectedHospitalInvalid
          case "required.all" => unexpectedHospitalRequiredAll
          case "required.two" => unexpectedHospitalRequiredTwo(args.head, args(1))
          case "required" => unexpectedHospitalRequired(args.head)
          case "notInFuture" => unexpectedHospitalNotInFuture
        }
        case "other" => suffix match {
          case "invalid" => otherInvalid
          case "required.all" => otherRequiredAll
          case "required.two" => otherRequiredTwo(args.head, args(1))
          case "required" => otherRequired(args.head)
          case "notInFuture" => otherNotInFuture
        }
      }
    }

    val crimeInvalid = "The date of the crime must be a real date"
    val fireOrFloodInvalid = "The date of the fire or flood must be a real date"
    val technicalIssueInvalid = "The date the software or technology issues began must be a real date"
    val bereavementInvalid = "The date the person died must be a real date"
    val cessationInvalid = "TBC"
    val healthInvalid = "TBC"
    val unexpectedHospitalInvalid = "TBC"
    val otherInvalid = "TBC"

    val crimeRequiredAll = "Enter the date of the crime"
    val fireOrFloodRequiredAll = "Enter the date of the fire or flood"
    val technicalIssueRequiredAll = "Enter the date when the software or technology issues began"
    val bereavementRequiredAll = "Enter the date when the person died"
    val cessationRequiredAll = "TBC"
    val healthRequiredAll = "TBC"
    val unexpectedHospitalRequiredAll = "TBC"
    val otherRequiredAll = "TBC"

    def crimeRequiredTwo(missing: String, missingTwo: String) = s"The date of the crime must include a $missing and a $missingTwo"
    def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"The date of the fire or flood must include a $missing and a $missingTwo"
    def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo"
    def bereavementRequiredTwo(missing: String, missingTwo: String) = s"The date the person died must include a $missing and a $missingTwo"
    def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def healthRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def otherRequiredTwo(missing: String, missingTwo: String) = "TBC"


    def crimeRequired(missing: String) = s"The date of the crime must include a $missing"
    def fireOrFloodRequired(missing: String) = s"The date of the fire or flood must include a $missing"
    def technicalIssueRequired(missing: String) = s"The date the software or technology issues began must include a $missing"
    def bereavementRequired(missing: String) = s"The date the person died must include a $missing"
    def cessationRequired(missing: String) = "TBC"
    def healthRequired(missing: String) = "TBC"
    def unexpectedHospitalRequired(missing: String) = "TBC"
    def otherRequired(missing: String) = "TBC"

    val crimeNotInFuture = "The date of the crime must be today or in the past"
    val fireOrFloodNotInFuture = "The date of the fire or flood must be today or in the past"
    val technicalIssueNotInFuture = "The date the software or technology issues began must be today or in the past"
    val bereavementNotInFuture = "The date the person died must be today or in the past"
    val cessationNotInFuture = "TBC"
    val healthNotInFuture = "TBC"
    val unexpectedHospitalNotInFuture = "TBC"
    val otherNotInFuture = "TBC"

  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val crimeInvalid = "The date of the crime must be a real date (Welsh)"
    override val fireOrFloodInvalid = "The date of the fire or flood must be a real date (Welsh)"
    override val technicalIssueInvalid = "The date the software or technology issues began must be a real date (Welsh)"
    override val bereavementInvalid = "The date the person died must be a real date (Welsh)"
    override val cessationInvalid = "TBC (Welsh)"
    override val healthInvalid = "TBC (Welsh)"
    override val unexpectedHospitalInvalid = "TBC (Welsh)"
    override val otherInvalid = "TBC (Welsh)"

    override val crimeRequiredAll = "Enter the date of the crime (Welsh)"
    override val fireOrFloodRequiredAll = "Enter the date of the fire or flood (Welsh)"
    override val technicalIssueRequiredAll = "Enter the date when the software or technology issues began (Welsh)"
    override val bereavementRequiredAll = "Enter the date when the person died (Welsh)"
    override val cessationRequiredAll = "TBC (Welsh)"
    override val healthRequiredAll = "TBC (Welsh)"
    override val unexpectedHospitalRequiredAll = "TBC (Welsh)"
    override val otherRequiredAll = "TBC (Welsh)"

    override def crimeRequiredTwo(missing: String, missingTwo: String) = s"The date of the crime must include a $missing and a $missingTwo (Welsh)"
    override def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"The date of the fire or flood must include a $missing and a $missingTwo (Welsh)"
    override def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo (Welsh)"
    override def bereavementRequiredTwo(missing: String, missingTwo: String) = s"The date the person died must include a $missing and a $missingTwo (Welsh)"
    override def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def healthRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def otherRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"

    override def crimeRequired(missing: String) = s"The date of the crime must include a $missing (Welsh)"
    override def fireOrFloodRequired(missing: String) = s"The date of the fire or flood must include a $missing (Welsh)"
    override def technicalIssueRequired(missing: String) = s"The date the software or technology issues began must include a $missing (Welsh)"
    override def bereavementRequired(missing: String) = s"The date the person died must include a $missing (Welsh)"
    override def cessationRequired(missing: String) = "TBC (Welsh)"
    override def healthRequired(missing: String) = "TBC (Welsh)"
    override def unexpectedHospitalRequired(missing: String) = "TBC (Welsh)"
    override def otherRequired(missing: String) = "TBC (Welsh)"

    override val crimeNotInFuture = "The date of the crime must be today or in the past (Welsh)"
    override val fireOrFloodNotInFuture = "The date of the fire or flood must be today or in the past (Welsh)"
    override val technicalIssueNotInFuture = "The date the software or technology issues began must be today or in the past (Welsh)"
    override val bereavementNotInFuture = "The date the person died must be today or in the past (Welsh)"
    override val cessationNotInFuture = "TBC (Welsh)"
    override val healthNotInFuture = "TBC (Welsh)"
    override val unexpectedHospitalNotInFuture = "TBC (Welsh)"
    override val otherNotInFuture = "TBC (Welsh)"
  }
}
