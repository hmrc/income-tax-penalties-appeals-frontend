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

    def headingAndTitle(reasonableExcuse: String, isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String = reasonableExcuse match {
      case "bereavement" => "When did the person die?"
      case "crime" => "When did the crime happen?"
      case "fireOrFlood" => "When did the fire or flood happen?"
      case "technicalIssues" => "When did the software or technology issues begin?"
      case "cessation" => "TBC cessation"
      case "health" => "TBC health"
      case "unexpectedHospital" => "TBC unexpectedHospital"
      case "other" => otherHeadingAndTitle(isLPP, isAgent, wasClientInformationIssue)
    }

    def otherHeadingAndTitle(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "When did the issue first stop your client getting information to you?"
        case (true, _, true)  => "When did the issue first stop your client paying the tax bill by the due date?"
        case (true, _, false) => "When did the issue first stop your client from meeting the submission deadline?"
        case (false, _, true) => "When did the issue first stop you paying the tax bill by the due date?"
        case (_, _, _)        => "When did the issue first stop you meeting the submission deadline?"
      }

    def errorMessageConstructor(reasonableExcuse: String,
                                suffix: String,
                                isLPP: Boolean = false,
                                isAgent: Boolean = false,
                                wasClientInformationIssue: Boolean = false,
                                args: Seq[String] = Seq()): String = {
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
          case "invalid" => otherInvalid(isLPP, isAgent, wasClientInformationIssue)
          case "required.all" => otherRequiredAll(isLPP, isAgent, wasClientInformationIssue)
          case "required.two" => otherRequiredTwo(isLPP, isAgent, wasClientInformationIssue, args.head, args(1))
          case "required" => otherRequired(isLPP, isAgent, wasClientInformationIssue, args.head)
          case "notInFuture" => otherNotInFuture(isLPP, isAgent, wasClientInformationIssue)
        }
      }
    }

    val cyaKeyBereavement: String = "When did the person die?"
    val cyaKeyCrime: String = "When did the crime happen?"
    val cyaKeyFireOrFlood: String = "When did the fire or flood happen?"
    val cyaKeyTechnical: String = "When did the software or technology issues begin?"
    val cyaKeyCessation: String = "TBC cessation"
    val cyaKeyHealth: String = "TBC health"
    val cyaKeyUnexpectedHospital: String = "TBC unexpectedHospital"

    def cyaKey(reasonableExcuse: String): String = reasonableExcuse match {
      case "bereavement" => cyaKeyBereavement
      case "crime" => cyaKeyCrime
      case "fireOrFlood" => cyaKeyFireOrFlood
      case "technicalIssues" => cyaKeyTechnical
      case "cessation" => cyaKeyCessation
      case "health" => cyaKeyHealth
      case "unexpectedHospital" => cyaKeyUnexpectedHospital
    }

    val cyaHiddenBereavement: String = "when did the person die"
    val cyaHiddenCrime: String = "when did the crime happen"
    val cyaHiddenFireOrFlood: String = "when did the fire or flood happen"
    val cyaHiddenTechnical: String = "when did the software or technology issues begin"
    val cyaHiddenCessation: String = "TBC cessation"
    val cyaHiddenHealth: String = "TBC health"
    val cyaHiddenUnexpectedHospital: String = "TBC unexpectedHospital"

    def cyaHidden(reasonableExcuse: String): String = reasonableExcuse match {
      case "bereavement" => cyaHiddenBereavement
      case "crime" => cyaHiddenCrime
      case "fireOrFlood" => cyaHiddenFireOrFlood
      case "technicalIssues" => cyaHiddenTechnical
      case "cessation" => cyaHiddenCessation
      case "health" => cyaHiddenHealth
      case "unexpectedHospital" => cyaHiddenUnexpectedHospital
    }

    val crimeInvalid = "The date of the crime must be a real date"
    val fireOrFloodInvalid = "The date of the fire or flood must be a real date"
    val technicalIssueInvalid = "The date the software or technology issues began must be a real date"
    val bereavementInvalid = "The date must be a real date"
    val cessationInvalid = "TBC"
    val healthInvalid = "TBC"
    val unexpectedHospitalInvalid = "TBC"
    def otherInvalid(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String = {
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "The date when the issue first stopped your client getting information to you must be a real date"
        case (true, _, true)  => "The date when the issue first stopped your client paying the tax bill by the due date must be a real date"
        case (true, _, false) => "The date when the issue first stopped your client from meeting the submission deadline must be a real date"
        case (false, _, true) => "The date the issue first stopped you paying the tax bill by the due date must be a real date"
        case (_, _, _)        => "The date the issue first stopped you meeting the submission deadline must be a real date"
      }
    }

    val crimeRequiredAll = "Enter the date of the crime"
    val fireOrFloodRequiredAll = "Enter the date of the fire or flood"
    val technicalIssueRequiredAll = "Tell us when the software or technology issues began"
    val bereavementRequiredAll = "Enter the date when the person died"
    val cessationRequiredAll = "TBC"
    val healthRequiredAll = "TBC"
    val unexpectedHospitalRequiredAll = "TBC"
    def otherRequiredAll(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "Tell us when the issue first stopped your client getting information to you"
        case (true, _, true)  => "Tell us when the issue first stopped your client paying the tax bill by the due date"
        case (true, _, false) => "Tell us when the issue first stopped your client from meeting the submission deadline?"
        case (false, _, true) => "Tell us when the issue first stopped you paying the tax bill by the due date"
        case (_, _, _)        => "Tell us when the issue first stopped you meeting the submission deadline"
      }

    def crimeRequiredTwo(missing: String, missingTwo: String) = s"The date of the crime must include a $missing and a $missingTwo"
    def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"The date of the fire or flood must include a $missing and a $missingTwo"
    def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo"
    def bereavementRequiredTwo(missing: String, missingTwo: String) = s"The date must include a $missing and a $missingTwo"
    def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def healthRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def otherRequiredTwo(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean, missing: String, missingTwo: String): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"The date when the issue first stopped your client getting information to you must include a $missing and a $missingTwo"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must include a $missing and a $missingTwo"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must include a $missing and a $missingTwo"
        case (false, _, true) => s"The date the issue first stopped you paying the tax bill by the due date must include a $missing and a $missingTwo"
        case (_, _, _)        => s"The date the issue first stopped you meeting the submission deadline must include a $missing and a $missingTwo"
      }

    def crimeRequired(missing: String) = s"The date of the crime must include a $missing"
    def fireOrFloodRequired(missing: String) = s"The date of the fire or flood must include a $missing"
    def technicalIssueRequired(missing: String) = s"The date the software or technology issues began must include a $missing"
    def bereavementRequired(missing: String) = s"The date must include a $missing"
    def cessationRequired(missing: String) = "TBC"
    def healthRequired(missing: String) = "TBC"
    def unexpectedHospitalRequired(missing: String) = "TBC"
    def otherRequired(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean, missing: String): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"The date when the issue first stopped your client getting information to you must include a $missing"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must include a $missing"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must include a $missing"
        case (false, _, true) => s"The date the issue first stopped you paying the tax bill by the due date must include a $missing"
        case (_, _, _)        => s"The date the issue first stopped you meeting the submission deadline must include a $missing"
      }

    val crimeNotInFuture = "The date of the crime must be today or in the past"
    val fireOrFloodNotInFuture = "The date of the fire or flood must be today or in the past"
    val technicalIssueNotInFuture = "The date the software or technology issues began must be today or in the past"
    val bereavementNotInFuture = "The date must be today or in the past"
    val cessationNotInFuture = "TBC"
    val healthNotInFuture = "TBC"
    val unexpectedHospitalNotInFuture = "TBC"
    def otherNotInFuture(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"The date when the issue first stopped your client getting information to you must be today or in the past"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must be today or in the past"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must be today or in the past"
        case (false, _, true) => s"The date the issue first stopped you paying the tax bill by the due date must be today or in the past"
        case (_, _, _)        => s"The date the issue first stopped you meeting the submission deadline must be today or in the past"
      }
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    override def headingAndTitle(reasonableExcuse: String, isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String = reasonableExcuse match {
      case "bereavement" => "When did the person die? (Welsh)"
      case "crime" => "When did the crime happen? (Welsh)"
      case "fireOrFlood" => "When did the fire or flood happen? (Welsh)"
      case "technicalIssues" => "When did the software or technology issues begin? (Welsh)"
      case "cessation" => "TBC cessation (Welsh)"
      case "health" => "TBC health (Welsh)"
      case "unexpectedHospital" => "TBC unexpectedHospital (Welsh)"
      case "other" => otherHeadingAndTitle(isLPP, isAgent, wasClientInformationIssue)
    }

    override def otherHeadingAndTitle(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "When did the issue first stop your client getting information to you? (Welsh)"
        case (true, _, true)  => "When did the issue first stop your client paying the tax bill by the due date? (Welsh)"
        case (true, _, false) => "When did the issue first stop your client from meeting the submission deadline? (Welsh)"
        case (false, _, true) => "When did the issue first stop you paying the tax bill by the due date? (Welsh)"
        case (_, _, _)        => "When did the issue first stop you meeting the submission deadline? (Welsh)"
      }

    override val crimeInvalid = "Mae’n rhaid i ddyddiad y drosedd fod yn ddyddiad go iawn"
    override val fireOrFloodInvalid = "Mae’n rhaid i ddyddiad y tân neu lifogydd fod yn ddyddiad go iawn"
    override val technicalIssueInvalid = "The date the software or technology issues began must be a real date (Welsh)"
    override val bereavementInvalid = "Mae’n rhaid i’r dyddiad fod yn ddyddiad go iawn"
    override val cessationInvalid = "TBC (Welsh)"
    override val healthInvalid = "TBC (Welsh)"
    override val unexpectedHospitalInvalid = "TBC (Welsh)"
    override def otherInvalid(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String = {
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "The date when the issue first stopped your client getting information to you must be a real date (Welsh)"
        case (true, _, true)  => "The date when the issue first stopped your client paying the tax bill by the due date must be a real date (Welsh)"
        case (true, _, false) => "The date when the issue first stopped your client from meeting the submission deadline must be a real date (Welsh)"
        case (false, _, true) => "The date the issue first stopped you paying the tax bill by the due date must be a real date (Welsh)"
        case (_, _, _)        => "The date the issue first stopped you meeting the submission deadline must be a real date (Welsh)"
      }
    }

    override val crimeRequiredAll = "Nodwch ddyddiad y drosedd"
    override val fireOrFloodRequiredAll = "Nodwch ddyddiad y tân neu lifogydd"
    override val technicalIssueRequiredAll = "Tell us when the software or technology issues began (Welsh)"
    override val bereavementRequiredAll = "Nodwch y dyddiad y bu farw’r person"
    override val cessationRequiredAll = "TBC (Welsh)"
    override val healthRequiredAll = "TBC (Welsh)"
    override val unexpectedHospitalRequiredAll = "TBC (Welsh)"
    override def otherRequiredAll(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "Tell us when the issue first stopped your client getting information to you (Welsh)"
        case (true, _, true)  => "Tell us when the issue first stopped your client paying the tax bill by the due date (Welsh)"
        case (true, _, false) => "Tell us when the issue first stopped your client from meeting the submission deadline? (Welsh)"
        case (false, _, true) => "Tell us when the issue first stopped you paying the tax bill by the due date (Welsh)"
        case (_, _, _)        => "Tell us when the issue first stopped you meeting the submission deadline (Welsh)"
      }

    override def crimeRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i ddyddiad y drosedd gynnwys $missing a $missingTwo"
    override def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i ddyddiad y tân neu lifogydd gynnwys $missing a $missingTwo"
    override def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo (Welsh)"
    override def bereavementRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad gynnwys $missing a $missingTwo"
    override def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def healthRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def otherRequiredTwo(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean, missing: String, missingTwo: String): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"The date when the issue first stopped your client getting information to you must include a $missing and a $missingTwo (Welsh)"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must include a $missing and a $missingTwo (Welsh)"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must include a $missing and a $missingTwo (Welsh)"
        case (false, _, true) => s"The date the issue first stopped you paying the tax bill by the due date must include a $missing and a $missingTwo (Welsh)"
        case (_, _, _)        => s"The date the issue first stopped you meeting the submission deadline must include a $missing and a $missingTwo (Welsh)"
      }

    override def crimeRequired(missing: String) = s"Mae’n rhaid i ddyddiad y drosedd gynnwys $missing"
    override def fireOrFloodRequired(missing: String) = s"Mae’n rhaid i ddyddiad y tân neu lifogydd gynnwys $missing"
    override def technicalIssueRequired(missing: String) = s"The date the software or technology issues began must include a $missing (Welsh)"
    override def bereavementRequired(missing: String) = s"Mae’n rhaid i’r dyddiad gynnwys $missing"
    override def cessationRequired(missing: String) = "TBC (Welsh)"
    override def healthRequired(missing: String) = "TBC (Welsh)"
    override def unexpectedHospitalRequired(missing: String) = "TBC (Welsh)"
    override def otherRequired(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean, missing: String): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"The date when the issue first stopped your client getting information to you must include a $missing (Welsh)"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must include a $missing (Welsh)"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must include a $missing (Welsh)"
        case (false, _, true) => s"The date the issue first stopped you paying the tax bill by the due date must include a $missing (Welsh)"
        case (_, _, _)        => s"The date the issue first stopped you meeting the submission deadline must include a $missing (Welsh)"
      }

    override val crimeNotInFuture = "Mae’n rhaid i ddyddiad y drosedd fod heddiw neu yn y gorffennol"
    override val fireOrFloodNotInFuture = "Mae’n rhaid i ddyddiad y tân neu lifogydd fod heddiw neu yn y gorffennol"
    override val technicalIssueNotInFuture = "The date the software or technology issues began must be today or in the past (Welsh)"
    override val bereavementNotInFuture = "Mae’n rhaid i’r dyddiad fod heddiw neu yn y gorffennol"
    override val cessationNotInFuture = "TBC (Welsh)"
    override val healthNotInFuture = "TBC (Welsh)"
    override val unexpectedHospitalNotInFuture = "TBC (Welsh)"
    override def otherNotInFuture(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"The date when the issue first stopped your client getting information to you must be today or in the past (Welsh)"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must be today or in the past (Welsh)"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must be today or in the past (Welsh)"
        case (false, _, true) => s"The date the issue first stopped you paying the tax bill by the due date must be today or in the past (Welsh)"
        case (_, _, _)        => s"The date the issue first stopped you meeting the submission deadline must be today or in the past (Welsh)"
      }

    override val cyaKeyBereavement: String = "When did the person die? (Welsh)"
    override val cyaKeyCrime: String = "When did the crime happen? (Welsh)"
    override val cyaKeyFireOrFlood: String = "When did the fire or flood happen? (Welsh)"
    override val cyaKeyTechnical: String = "When did the software or technology issues begin? (Welsh)"
    override val cyaKeyCessation: String = "TBC cessation (Welsh)"
    override val cyaKeyHealth: String = "TBC health (Welsh)"
    override val cyaKeyUnexpectedHospital: String = "TBC unexpectedHospital (Welsh)"

    override val cyaHiddenBereavement: String = "when did the person die (Welsh)"
    override val cyaHiddenCrime: String = "when did the crime happen (Welsh)"
    override val cyaHiddenFireOrFlood: String = "when did the fire or flood happen (Welsh)"
    override val cyaHiddenTechnical: String = "when did the software or technology issues begin (Welsh)"
    override val cyaHiddenCessation: String = "TBC cessation (Welsh)"
    override val cyaHiddenHealth: String = "TBC health (Welsh)"
    override val cyaHiddenUnexpectedHospital: String = "TBC unexpectedHospital (Welsh)"
  }
}
