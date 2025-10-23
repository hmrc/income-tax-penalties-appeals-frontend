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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._

object WhenDidEventHappenMessages {

  sealed trait Messages { this: i18n =>

    def headingAndTitle(reasonableExcuse: ReasonableExcuse, isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String = reasonableExcuse match {
      case Bereavement => "When did the person die?"
      case Crime => "When did the crime happen?"
      case FireOrFlood => "When did the fire or flood happen?"
      case TechnicalIssues => "When did the software or technology issues begin?"
      case Cessation => "TBC cessation"
      case Health => if(isLPP) "When did the health issue first stop you making the payment?" else "When did the health issue first stop you from meeting the submission deadline?"
      case UnexpectedHospital => "When did the hospital stay begin?"
      case LossOfStaff => "TBC lossOfStaff"
      case Other => otherHeadingAndTitle(isLPP, isAgent, wasClientInformationIssue)
    }

    def otherHeadingAndTitle(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "When did the issue first stop your client getting information to you?"
        case (true, _, true)  => "When did the issue first stop your client making the payment?"
        case (true, _, false) => "When did the issue first stop your client from meeting the submission deadline?"
        case (false, _, true) => "When did the issue first stop you paying the tax bill by the due date?"
        case (_, _, _)        => "When did the issue first stop you from meeting the submission deadline?"
      }

    def errorMessageConstructor(reasonableExcuse: ReasonableExcuse,
                                suffix: String,
                                isLPP: Boolean = false,
                                isAgent: Boolean = false,
                                wasClientInformationIssue: Boolean = false,
                                args: Seq[String] = Seq()): String = {
      reasonableExcuse match {
        case Bereavement => suffix match {
          case "invalid" => bereavementInvalid
          case "required.all" => bereavementRequiredAll
          case "required.two" => bereavementRequiredTwo(args.head, args(1))
          case "required" => bereavementRequired(args.head)
          case "notInFuture" => bereavementNotInFuture
        }
        case Crime => suffix match {
          case "invalid" => crimeInvalid
          case "required.all" => crimeRequiredAll
          case "required.two" => crimeRequiredTwo(args.head, args(1))
          case "required" => crimeRequired(args.head)
          case "notInFuture" => crimeNotInFuture
        }
        case FireOrFlood => suffix match {
          case "invalid" => fireOrFloodInvalid
          case "required.all" => fireOrFloodRequiredAll
          case "required.two" => fireOrFloodRequiredTwo(args.head, args(1))
          case "required" => fireOrFloodRequired(args.head)
          case "notInFuture" => fireOrFloodNotInFuture
        }
        case TechnicalIssues => suffix match {
          case "invalid" => technicalIssueInvalid
          case "required.all" => technicalIssueRequiredAll
          case "required.two" => technicalIssueRequiredTwo(args.head, args(1))
          case "required" => technicalIssueRequired(args.head)
          case "notInFuture" => technicalIssueNotInFuture
        }
        case Cessation => suffix match {
          case "invalid" => cessationInvalid
          case "required.all" => cessationRequiredAll
          case "required.two" => cessationRequiredTwo(args.head, args(1))
          case "required" => cessationRequired(args.head)
          case "notInFuture" => cessationNotInFuture
        }
        case Health => suffix match {
          case "invalid" => healthInvalid
          case "required.all" => healthRequiredAll
          case "required.two" => healthRequiredTwo(args.head, args(1))
          case "required" => healthRequired(args.head)
          case "notInFuture" => healthNotInFuture
        }
        case UnexpectedHospital => suffix match {
          case "invalid" => unexpectedHospitalInvalid
          case "required.all" => unexpectedHospitalRequiredAll
          case "required.two" => unexpectedHospitalRequiredTwo(args.head, args(1))
          case "required" => unexpectedHospitalRequired(args.head)
          case "notInFuture" => unexpectedHospitalNotInFuture
        }
        case LossOfStaff => suffix match {
          case "invalid" => lossOfStaffInvalid
          case "required.all" => lossOfStaffRequiredAll
          case "required.two" => lossOfStaffRequiredTwo(args.head, args(1))
          case "required" => lossOfStaffRequired(args.head)
          case "notInFuture" => lossOfStaffNotInFuture
        }
        case Other => suffix match {
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
    val cyaKeyHealth: String = "When did the health issue first stop you making the payment?"
    val cyaKeyLossOfStaff: String = "TBC lossOfStaff"
    val cyaKeyOther: String = "TBC other"
    val cyaKeyUnexpectedHospital: String = "When did the hospital stay begin?"

    def cyaKey(reasonableExcuse: ReasonableExcuse, isLPP: Boolean = false, isAgent: Boolean = false, wasClientInformationIssue: Boolean = false): String = reasonableExcuse match {
      case Bereavement => cyaKeyBereavement
      case Cessation => cyaKeyCessation
      case Crime => cyaKeyCrime
      case FireOrFlood => cyaKeyFireOrFlood
      case Health => headingAndTitle(Health, isLPP, isAgent, wasClientInformationIssue)
      case TechnicalIssues => cyaKeyTechnical
      case UnexpectedHospital => cyaKeyUnexpectedHospital
      case LossOfStaff => cyaKeyLossOfStaff
      case Other => otherHeadingAndTitle(isLPP, isAgent, wasClientInformationIssue)
    }

    val cyaHiddenBereavement: String = "when did the person die"
    val cyaHiddenCrime: String = "when did the crime happen"
    val cyaHiddenFireOrFlood: String = "when did the fire or flood happen"
    val cyaHiddenTechnical: String = "when did the software or technology issues begin"
    val cyaHiddenCessation: String = "TBC cessation"
    val cyaHiddenHealth: String = "when did the health issue first stop you making the payment"
    val cyaHiddenUnexpectedHospital: String = "when did the hospital stay begin"
    val cyaHiddenLossOfStaff: String = "TBC lossOfStaff"
    val cyaHiddenOther: String = "TBC other"

    def cyaHidden(reasonableExcuse: ReasonableExcuse): String = reasonableExcuse match {
      case Bereavement => cyaHiddenBereavement
      case Cessation => cyaHiddenCessation
      case Crime => cyaHiddenCrime
      case FireOrFlood => cyaHiddenFireOrFlood
      case Health => cyaHiddenHealth
      case TechnicalIssues => cyaHiddenTechnical
      case UnexpectedHospital => cyaHiddenUnexpectedHospital
      case LossOfStaff => cyaHiddenLossOfStaff
      case Other => cyaHiddenOther
    }

    val crimeInvalid = "The date of the crime must be a real date"
    val fireOrFloodInvalid = "The date of the fire or flood must be a real date"
    val technicalIssueInvalid = "The date the software or technology issues began must be a real date"
    val bereavementInvalid = "The date must be a real date"
    val cessationInvalid = "TBC"
    val healthInvalid = "The date of the health issue must be a real date"
    val unexpectedHospitalInvalid = "The date that the hospital stay began must be a real date"
    val lossOfStaffInvalid = "TBC"
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
    val healthRequiredAll = "Enter the date that the health issue began"
    val unexpectedHospitalRequiredAll = "Enter the date that the hospital stay began"
    val lossOfStaffRequiredAll = "TBC"
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
    def healthRequiredTwo(missing: String, missingTwo: String) = s"The date of the health issue must include a $missing and a $missingTwo"
    def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = s"The date that the hospital stay began must include a $missing and a $missingTwo"
    def lossOfStaffRequiredTwo(missing: String, missingTwo: String) = "TBC"
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
    def healthRequired(missing: String) = s"The date of the health issue must include a $missing"
    def unexpectedHospitalRequired(missing: String) = s"The date that the hospital stay began must include a $missing"
    def lossOfStaffRequired(missing: String) = "TBC"
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
    val healthNotInFuture = "The date of the health issue must be today or in the past"
    val unexpectedHospitalNotInFuture = "The date that the hospital stay began must be today or in the past"
    val lossOfStaffNotInFuture = "TBC"
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

    override def headingAndTitle(reasonableExcuse: ReasonableExcuse, isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String = reasonableExcuse match {
      case Bereavement => "Pryd y bu farw’r person?"
      case Crime => "Pryd ddigwyddodd y drosedd?"
      case FireOrFlood => "Pryd ddigwyddodd y tân neu lifogydd?"
      case TechnicalIssues => "Pryd y gwnaeth y problemau technegol neu’r problemau meddalwedd ddechrau?"
      case Cessation => "TBC cessation (Welsh)"
      case Health => if(isLPP) "Pryd gwnaeth y broblem iechyd eich rhwystro am y tro cyntaf rhag gwneud y taliad?" else "Pryd y gwnaeth y broblem iechyd eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno?"
      case UnexpectedHospital => "Pryd y gwnaeth yr arhosiad yn yr ysbyty ddechrau?"
      case LossOfStaff => "TBC lossOfStaff (Welsh)"
      case Other => otherHeadingAndTitle(isLPP, isAgent, wasClientInformationIssue)
    }

    override def otherHeadingAndTitle(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "Pryd y gwnaeth y broblem atal eich cleient am y tro cyntaf rhag cael gwybodaeth atoch chi?"
        case (true, _, true)  => "Pryd y gwnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag gwneud y taliad?"
        case (true, _, false) => "Pryd y gwnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno?"
        case (false, _, true) => "Pryd y gwnaeth y broblem eich rhwystro am y tro cyntaf rhag gwneud y taliad?"
        case (_, _, _)        => "Pryd y gwnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno?"
      }

    override val crimeInvalid = "Mae’n rhaid i ddyddiad y drosedd fod yn ddyddiad go iawn"
    override val fireOrFloodInvalid = "Mae’n rhaid i ddyddiad y tân neu lifogydd fod yn ddyddiad go iawn"
    override val technicalIssueInvalid = "Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd fod yn ddyddiad go iawn"
    override val bereavementInvalid = "Mae’n rhaid i’r dyddiad fod yn ddyddiad go iawn"
    override val cessationInvalid = "TBC (Welsh)"
    override val healthInvalid = "Mae’n rhaid i’r dyddiad pan ddechreuodd y broblem iechyd fod yn ddyddiad go iawn"
    override val unexpectedHospitalInvalid = "Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty fod yn ddyddiad go iawn"
    override val lossOfStaffInvalid = "TBC (Welsh)"
    override def otherInvalid(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String = {
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "Mae’n rhaid i’r dyddiad pan wnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag anfon gwybodaeth atoch fod yn ddyddiad go iawn"
        case (true, _, true)  => "The date when the issue first stopped your client paying the tax bill by the due date must be a real date (Welsh)"
        case (true, _, false) => "The date when the issue first stopped your client from meeting the submission deadline must be a real date (Welsh)"
        case (false, _, true) => "Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag talu’r bil treth erbyn y dyddiad cau ar gyfer talu fod yn ddyddiad go iawn"
        case (_, _, _)        => "Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno fod yn ddyddiad go iawn"
      }
    }

    override val crimeRequiredAll = "Nodwch ddyddiad y drosedd"
    override val fireOrFloodRequiredAll = "Nodwch ddyddiad y tân neu lifogydd"
    override val technicalIssueRequiredAll = "Rhowch wybod i ni pryd y dechreuodd y problemau technoleg neu’r problemau meddalwedd"
    override val bereavementRequiredAll = "Nodwch y dyddiad y bu farw’r person"
    override val cessationRequiredAll = "TBC (Welsh)"
    override val healthRequiredAll = "Nodwch y dyddiad pan ddechreuodd y broblem iechyd"
    override val unexpectedHospitalRequiredAll = "Nodwch y dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty"
    override val lossOfStaffRequiredAll = "TBC (Welsh)"
    override def otherRequiredAll(isLPP: Boolean = false, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => "Rhowch wybod i ni pryd y gwnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag anfon gwybodaeth atoch"
        case (true, _, true)  => "Tell us when the issue first stopped your client paying the tax bill by the due date (Welsh)"
        case (true, _, false) => "Tell us when the issue first stopped your client from meeting the submission deadline? (Welsh)"
        case (false, _, true) => "Rhowch wybod i ni pryd y gwnaeth y broblem eich rhwystro am y tro cyntaf rhag talu’r bil treth erbyn y dyddiad cau ar gyfer talu"
        case (_, _, _)        => "Rhowch wybod i ni pryd y gwnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno"
      }

    override def crimeRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i ddyddiad y drosedd gynnwys $missing a $missingTwo"
    override def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i ddyddiad y tân neu lifogydd gynnwys $missing a $missingTwo"
    override def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd gynnwys $missing and a $missingTwo"
    override def bereavementRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad gynnwys $missing a $missingTwo"
    override def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def healthRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd y broblem iechyd gynnwys $missing a $missingTwo"
    override def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty gynnwys $missing a $missingTwo"
    override def lossOfStaffRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def otherRequiredTwo(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean, missing: String, missingTwo: String): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag anfon gwybodaeth atoch gynnwys $missing a $missingTwo"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must include a $missing and a $missingTwo (Welsh)"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must include a $missing and a $missingTwo (Welsh)"
        case (false, _, true) => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag talu’r bil treth erbyn y dyddiad cau ar gyfer talu gynnwys $missing a $missingTwo"
        case (_, _, _)        => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno gynnwys $missing a $missingTwo"
      }

    override def crimeRequired(missing: String) = s"Mae’n rhaid i ddyddiad y drosedd gynnwys $missing"
    override def fireOrFloodRequired(missing: String) = s"Mae’n rhaid i ddyddiad y tân neu lifogydd gynnwys $missing"
    override def technicalIssueRequired(missing: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd gynnwys $missing"
    override def bereavementRequired(missing: String) = s"Mae’n rhaid i’r dyddiad gynnwys $missing"
    override def cessationRequired(missing: String) = "TBC (Welsh)"
    override def healthRequired(missing: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd y broblem iechyd gynnwys $missing"
    override def unexpectedHospitalRequired(missing: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty gynnwys $missing"
    override def lossOfStaffRequired(missing: String) = "TBC (Welsh)"
    override def otherRequired(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean, missing: String): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag anfon gwybodaeth atoch gynnwys"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must include a $missing (Welsh)"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must include a $missing (Welsh)"
        case (false, _, true) => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag talu’r bil treth erbyn y dyddiad cau ar gyfer talu gynnwys $missing"
        case (_, _, _)        => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno gynnwys $missing"
      }

    override val crimeNotInFuture = "Mae’n rhaid i ddyddiad y drosedd fod heddiw neu yn y gorffennol"
    override val fireOrFloodNotInFuture = "Mae’n rhaid i ddyddiad y tân neu lifogydd fod heddiw neu yn y gorffennol"
    override val technicalIssueNotInFuture = "Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd fod heddiw neu yn y gorffennol"
    override val bereavementNotInFuture = "Mae’n rhaid i’r dyddiad fod heddiw neu yn y gorffennol"
    override val cessationNotInFuture = "TBC (Welsh)"
    override val healthNotInFuture = "Mae’n rhaid i’r dyddiad pan ddechreuodd y broblem iechyd fod heddiw neu yn y gorffennol"
    override val unexpectedHospitalNotInFuture = "Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty fod heddiw neu yn y gorffennol"
    override val lossOfStaffNotInFuture = "TBC (Welsh)"
    override def otherNotInFuture(isLPP: Boolean, isAgent: Boolean, wasClientInformationIssue: Boolean): String =
      (isAgent, wasClientInformationIssue, isLPP) match {
        case (true, true, _)  => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem rwystro’ch cleient am y tro cyntaf rhag anfon gwybodaeth atoch fod heddiw neu yn y gorffennol"
        case (true, _, true)  => s"The date when the issue first stopped your client paying the tax bill by the due date must be today or in the past (Welsh)"
        case (true, _, false) => s"The date when the issue first stopped your client from meeting the submission deadline must be today or in the past (Welsh)"
        case (false, _, true) => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag talu’r bil treth erbyn y dyddiad cau ar gyfer talu fod heddiw neu yn y gorffennol"
        case (_, _, _)        => s"Mae’n rhaid i’r dyddiad pan wnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno fod heddiw neu yn y gorffennol"
      }

    override val cyaKeyBereavement: String = "Pryd y bu farw’r person?"
    override val cyaKeyCrime: String = "Pryd ddigwyddodd y drosedd?"
    override val cyaKeyFireOrFlood: String = "Pryd ddigwyddodd y tân neu lifogydd?"
    override val cyaKeyTechnical: String = "Pryd y gwnaeth y problemau technegol neu’r problemau meddalwedd ddechrau?"
    override val cyaKeyCessation: String = "TBC cessation (Welsh)"
    override val cyaKeyHealth: String = "Pryd gwnaeth y broblem iechyd eich rhwystro am y tro cyntaf rhag gwneud y taliad?"
    override val cyaKeyUnexpectedHospital: String = "Pryd y gwnaeth yr arhosiad yn yr ysbyty ddechrau?"
    override val cyaKeyLossOfStaff: String = "TBC lossOfStaff (Welsh)"
    override val cyaKeyOther: String = "TBC other (Welsh)"

    override val cyaHiddenBereavement: String = "Pryd y bu farw’r person"
    override val cyaHiddenCrime: String = "Pryd ddigwyddodd y drosedd"
    override val cyaHiddenFireOrFlood: String = "Pryd ddigwyddodd y tân neu lifogydd"
    override val cyaHiddenTechnical: String = "Pryd y gwnaeth y problemau technegol neu’r problemau meddalwedd ddechrau"
    override val cyaHiddenCessation: String = "TBC cessation (Welsh)"
    override val cyaHiddenHealth: String = "Pryd gwnaeth y broblem iechyd eich rhwystro am y tro cyntaf rhag gwneud y taliad"
    override val cyaHiddenUnexpectedHospital: String = "Pryd y dechreuodd yr arhosiad yn yr ysbyty?"
    override val cyaHiddenLossOfStaff: String = "TBC lossOfStaff (Welsh)"
    override val cyaHiddenOther: String = "TBC other (Welsh)"
  }
}
