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

    def headingAndTitle(reasonableExcuse: ReasonableExcuse, isLPP: Boolean, wasClientInformationIssue: Boolean): String = reasonableExcuse match {
      case Bereavement => "When did the person die?"
      case Crime => "When did the crime happen?"
      case FireOrFlood => "When did the fire or flood happen?"
      case TechnicalIssues => "When did the software or technology issues begin?"
      case Cessation => "TBC cessation"
      case Health => if(isLPP) "When did the health issue first stop the payment being made?" else "When did the health issue first stop the submission deadline being met?"
      case UnexpectedHospital => "When did the hospital stay begin?"
      case LossOfStaff => "TBC lossOfStaff"
      case Other => otherHeadingAndTitle(isLPP)
    }

    def otherHeadingAndTitle(isLPP: Boolean = false): String = {
      if (isLPP) "When did the issue first stop the payment being made?" else "When did the issue first stop the submission deadline being met?"
    }

    def errorMessageConstructor(reasonableExcuse: ReasonableExcuse,
                                suffix: String,
                                isLPP: Boolean,
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
          case "invalid" => healthInvalid(isLPP)
          case "required.all" => healthRequiredAll(isLPP)
          case "required.two" => healthRequiredTwo(isLPP, args.head, args(1))
          case "required" => healthRequired(isLPP, args.head)
          case "notInFuture" => healthNotInFuture(isLPP)
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
          case "invalid" => otherInvalid(isLPP)
          case "required.all" => otherRequiredAll(isLPP)
          case "required.two" => otherRequiredTwo(isLPP, args.head, args(1))
          case "required" => otherRequired(isLPP, args.head)
          case "notInFuture" => otherNotInFuture(isLPP)
        }
      }
    }

    val cyaKeyBereavement: String = "Date person died"
    val cyaKeyCrime: String = "Date of crime"
    val cyaKeyFireOrFlood: String = "Date of fire or flood"
    val cyaKeyTechnical: String = "Date software or technology issue started"
    val cyaKeyCessation: String = "TBC cessation"
    val cyaKeyHealth: String = "Date health issue stopped you from meeting the deadline"
    val cyaKeyLossOfStaff: String = "TBC lossOfStaff"
    val cyaKeyOther: String = "Date issue stopped you from meeting the deadline"
    val cyaKeyUnexpectedHospital: String = "Date hospital stay started"

    def cyaKey(reasonableExcuse: ReasonableExcuse, isLPP: Boolean = false, isAgent: Boolean = false, wasClientInformationIssue: Boolean = false): String = reasonableExcuse match {
      case Bereavement => cyaKeyBereavement
      case Cessation => cyaKeyCessation
      case Crime => cyaKeyCrime
      case FireOrFlood => cyaKeyFireOrFlood
      case Health => cyaKeyHealth
      case TechnicalIssues => cyaKeyTechnical
      case UnexpectedHospital => cyaKeyUnexpectedHospital
      case LossOfStaff => cyaKeyLossOfStaff
      case Other => cyaKeyOther
    }

    val cyaHiddenBereavement: String = "when did the person die"
    val cyaHiddenCrime: String = "when did the crime happen"
    val cyaHiddenFireOrFlood: String = "when did the fire or flood happen"
    val cyaHiddenTechnical: String = "when did the software or technology issues begin"
    val cyaHiddenCessation: String = "TBC cessation"
    val cyaHiddenHealth: String = "when did the health issue first stop you from meeting the submission deadline"
    val cyaHiddenUnexpectedHospital: String = "when did the hospital stay begin"
    val cyaHiddenLossOfStaff: String = "TBC lossOfStaff"
    val cyaHiddenOther: String = "when did the issue first stop you from meeting the submission deadline"

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
    val bereavementInvalid = "The date the person died must be a real date"
    val cessationInvalid = "TBC"
    def healthInvalid(isLPP: Boolean = false): String = {
      if (isLPP) "The date the health issue first stopped payment being made must be a real date" else "The date the health issue first stopped the submission deadline being met must be a real date"
    }
    val unexpectedHospitalInvalid = "The date that the hospital stay began must be a real date"
    val lossOfStaffInvalid = "TBC"
    def otherInvalid(isLPP: Boolean = false): String = {
      if (isLPP) "The date the issue first stopped payment being made must be a real date" else "The date the issue first stopped the submission deadline being met must be a real date"
    }

    val crimeRequiredAll = "Enter the date of the crime"
    val fireOrFloodRequiredAll = "Enter the date of the fire or flood"
    val technicalIssueRequiredAll = "Enter the date the software or technology issues began"
    val bereavementRequiredAll = "Enter the date when the person died"
    val cessationRequiredAll = "TBC"
    def healthRequiredAll(isLPP: Boolean = false): String = {
      if (isLPP) "Enter the date the health issue first stopped payment being made" else "Enter the date the health issue first stopped the submission deadline being met"
    }
    val unexpectedHospitalRequiredAll = "Enter the date that the hospital stay began"
    val lossOfStaffRequiredAll = "TBC"
    def otherRequiredAll(isLPP: Boolean = false): String = {
      if (isLPP) "Enter the date the issue first stopped payment being made" else "Enter the date the issue first stopped the submission deadline being met"
    }

    def crimeRequiredTwo(missing: String, missingTwo: String) = s"The date of the crime must include a $missing and a $missingTwo"
    def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"The date of the fire or flood must include a $missing and a $missingTwo"
    def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"The date the software or technology issues began must include a $missing and a $missingTwo"
    def bereavementRequiredTwo(missing: String, missingTwo: String): String = s"The date the person died must include a $missing and a $missingTwo"
    def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def healthRequiredTwo(isLPP: Boolean, missing: String, missingTwo: String): String = {
      if (isLPP) s"The date the health issue first stopped payment being made must include a $missing and a $missingTwo" else s"The date the health issue first stopped the submission deadline being met must include a $missing and a $missingTwo"
    }
    def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = s"The date that the hospital stay began must include a $missing and a $missingTwo"
    def lossOfStaffRequiredTwo(missing: String, missingTwo: String) = "TBC"
    def otherRequiredTwo(isLPP: Boolean, missing: String, missingTwo: String): String = {
      if (isLPP) s"The date the issue first stopped payment being made must include a $missing and a $missingTwo" else s"The date the issue first stopped the submission deadline being met must include a $missing and a $missingTwo"
    }

    def crimeRequired(missing: String) = s"The date of the crime must include a $missing"
    def fireOrFloodRequired(missing: String) = s"The date of the fire or flood must include a $missing"
    def technicalIssueRequired(missing: String) = s"The date the software or technology issues began must include a $missing"
    def bereavementRequired(missing: String) = s"The date the person died must include a $missing"
    def cessationRequired(missing: String) = "TBC"
    def healthRequired(isLPP: Boolean, missing: String): String = {
      if (isLPP) s"The date the health issue first stopped payment being made must include a $missing" else s"The date the health issue first stopped the submission deadline being met must include a $missing"
    }
    def unexpectedHospitalRequired(missing: String) = s"The date that the hospital stay began must include a $missing"
    def lossOfStaffRequired(missing: String) = "TBC"
    def otherRequired(isLPP: Boolean, missing: String): String = {
      if (isLPP) s"The date the issue first stopped payment being made must include a $missing" else s"The date the issue first stopped the submission deadline being met must include a $missing"
    }

    val crimeNotInFuture = "The date of the crime must be today or in the past"
    val fireOrFloodNotInFuture = "The date of the fire or flood must be today or in the past"
    val technicalIssueNotInFuture = "The date the software or technology issues began must be today or in the past"
    val bereavementNotInFuture = "The date the person died must be today or in the past"
    val cessationNotInFuture = "TBC"
    def healthNotInFuture(isLPP: Boolean = false): String = {
      if (isLPP) "The date the issue first stopped payment being made must be today or in the past" else "The date the issue first stopped the submission deadline being met must be today or in the past"
    }
    val unexpectedHospitalNotInFuture = "The date that the hospital stay began must be today or in the past"
    val lossOfStaffNotInFuture = "TBC"
    def otherNotInFuture(isLPP: Boolean): String = {
      if (isLPP) "The date the issue first stopped payment being made must be today or in the past" else "The date the issue first stopped the submission deadline being met must be today or in the past"
    }
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    override def headingAndTitle(reasonableExcuse: ReasonableExcuse, isLPP: Boolean, wasClientInformationIssue: Boolean): String = reasonableExcuse match {
      case Bereavement => "Pryd y bu farw’r person?"
      case Crime => "Pryd ddigwyddodd y drosedd?"
      case FireOrFlood => "Pryd ddigwyddodd y tân neu lifogydd?"
      case TechnicalIssues => "Pryd y gwnaeth y problemau technegol neu’r problemau meddalwedd ddechrau?"
      case Cessation => "TBC cessation (Welsh)"
      case Health => if(isLPP) "Pryd gwnaeth y broblem iechyd eich rhwystro am y tro cyntaf rhag gwneud y taliad?" else "Pryd gwnaeth y broblem iechyd rhwystro’r cyflwyniad rhag bodloni’r dyddiad cau am y tro cyntaf?"
      case UnexpectedHospital => "Pryd y dechreuodd yr arhosiad yn yr ysbyty?"
      case LossOfStaff => "TBC lossOfStaff (Welsh)"
      case Other => otherHeadingAndTitle(isLPP)
    }

    override def otherHeadingAndTitle(isLPP: Boolean = false): String = {
      if (isLPP) "Pryd gwnaeth y broblem rhwystro’r taliad rhag cael ei wneud am y tro cyntaf?" else "Pryd gwnaeth y broblem rhwystro’r cyflwyniad rhag bodloni’r dyddiad cau am y tro cyntaf?"
    }

    override val crimeInvalid = "Mae’n rhaid i ddyddiad y drosedd fod yn ddyddiad go iawn"
    override val fireOrFloodInvalid = "Mae’n rhaid i ddyddiad y tân neu lifogydd fod yn ddyddiad go iawn"
    override val technicalIssueInvalid = "Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd fod yn ddyddiad go iawn"
    override val bereavementInvalid = "Mae’n rhaid i’r dyddiad y bu farw’r person fod yn ddyddiad go iawn"
    override val cessationInvalid = "TBC (Welsh)"
    override def healthInvalid(isLPP: Boolean = false): String = {
      if (isLPP) "Y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn fod yn ddyddiad go iawn" else "Y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn fod yn ddyddiad go iawn"
    }
    override val unexpectedHospitalInvalid = "Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty fod yn ddyddiad go iawn"
    override val lossOfStaffInvalid = "TBC (Welsh)"
    override def otherInvalid(isLPP: Boolean = false): String = {
      if (isLPP) "Y dyddiad y gwnaeth y broblem eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn fod yn ddyddiad go iawn" else "Y dyddiad y gwnaeth y broblem eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn fod yn ddyddiad go iawn"
    }

    override val crimeRequiredAll = "Nodwch ddyddiad y drosedd"
    override val fireOrFloodRequiredAll = "Nodwch ddyddiad y tân neu lifogydd"
    override val technicalIssueRequiredAll = "Enter the date the software or technology issues began (Welsh)"
    override val bereavementRequiredAll = "Nodwch y dyddiad y bu farw’r person"
    override val cessationRequiredAll = "TBC (Welsh)"
    override def healthRequiredAll(isLPP: Boolean = false): String = {
      if (isLPP) "Nodwch y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag gwneud taliad am y tro cyntaf" else "Nodwch y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf"
    }
    override val unexpectedHospitalRequiredAll = "Nodwch y dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty"
    override val lossOfStaffRequiredAll = "TBC (Welsh)"
    override def otherRequiredAll(isLPP: Boolean = false): String = {
      if (isLPP) "Nodwch y dyddiad y gwnaeth y broblem eich rhwystro rhag gwneud taliad am y tro cyntaf" else "Nodwch y dyddiad y gwnaeth y broblem eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf"
    }

    override def crimeRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i ddyddiad y drosedd gynnwys $missing a $missingTwo"
    override def fireOrFloodRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i ddyddiad y tân neu lifogydd gynnwys $missing a $missingTwo"
    override def technicalIssueRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd gynnwys $missing a $missingTwo"
    override def bereavementRequiredTwo(missing: String, missingTwo: String): String = s"Mae’n rhaid i’r dyddiad y bu farw’r person gynnwys $missing a $missingTwo"
    override def cessationRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def healthRequiredTwo(isLPP: Boolean, missing: String, missingTwo: String): String = {
      if (isLPP) s"Y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing a $missingTwo" else s"Y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing a $missingTwo"
    }
    override def unexpectedHospitalRequiredTwo(missing: String, missingTwo: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty gynnwys $missing a $missingTwo"
    override def lossOfStaffRequiredTwo(missing: String, missingTwo: String) = "TBC (Welsh)"
    override def otherRequiredTwo(isLPP: Boolean, missing: String, missingTwo: String): String = {
      if (isLPP) s"Y dyddiad y gwnaeth y broblem eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing a $missingTwo" else s"Y dyddiad y gwnaeth y broblem eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing a $missingTwo"
    }

    override def crimeRequired(missing: String) = s"Mae’n rhaid i ddyddiad y drosedd gynnwys $missing"
    override def fireOrFloodRequired(missing: String) = s"Mae’n rhaid i ddyddiad y tân neu lifogydd gynnwys $missing"
    override def technicalIssueRequired(missing: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd gynnwys $missing"
    override def bereavementRequired(missing: String) = s"Mae’n rhaid i’r dyddiad y bu farw’r person gynnwys $missing"
    override def cessationRequired(missing: String) = "TBC (Welsh)"
    override def healthRequired(isLPP: Boolean, missing: String): String = {
      if (isLPP) s"Y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing" else s"Y dyddiad y gwnaeth y broblem iechyd eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing"
    }
    override def unexpectedHospitalRequired(missing: String) = s"Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty gynnwys $missing"
    override def lossOfStaffRequired(missing: String) = "TBC (Welsh)"
    override def otherRequired(isLPP: Boolean, missing: String): String = {
      if (isLPP) s"Y dyddiad y gwnaeth y broblem eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing" else s"Y dyddiad y gwnaeth y broblem eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn gynnwys $missing"
    }

    override val crimeNotInFuture = "Mae’n rhaid i ddyddiad y drosedd fod heddiw neu yn y gorffennol"
    override val fireOrFloodNotInFuture = "Mae’n rhaid i ddyddiad y tân neu lifogydd fod heddiw neu yn y gorffennol"
    override val technicalIssueNotInFuture = "Mae’n rhaid i’r dyddiad pan ddechreuodd y problemau technoleg neu’r problemau meddalwedd fod heddiw neu yn y gorffennol"
    override val bereavementNotInFuture = "Mae’n rhaid i’r dyddiad y bu farw’r person fod heddiw neu yn y gorffennol"
    override val cessationNotInFuture = "TBC (Welsh)"
    override def healthNotInFuture(isLPP: Boolean = false): String = {
      if (isLPP) "Y dyddiad y gwnaeth y broblem eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn fod heddiw neu yn y gorffennol" else "Y dyddiad y gwnaeth y broblem eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn fod heddiw neu yn y gorffennol"
    }
    override val unexpectedHospitalNotInFuture = "Mae’n rhaid i’r dyddiad pan ddechreuodd yr arhosiad yn yr ysbyty fod heddiw neu yn y gorffennol"
    override val lossOfStaffNotInFuture = "TBC (Welsh)"
    override def otherNotInFuture(isLPP: Boolean): String = {
      if (isLPP) "Y dyddiad y gwnaeth y broblem eich rhwystro rhag gwneud taliad am y tro cyntaf: mae’n rhaid i hyn fod heddiw neu yn y gorffennol" else "Y dyddiad y gwnaeth y broblem eich rhwystro rhag bodloni’r dyddiad cyflwyno am y tro cyntaf: mae’n rhaid i hyn fod heddiw neu yn y gorffennol"
    }

    override val cyaKeyBereavement: String = "Dyddiad y bu farw’r person"
    override val cyaKeyCrime: String = "Dyddiad y drosedd"
    override val cyaKeyFireOrFlood: String = "Dyddiad y tân neu’r llifogydd"
    override val cyaKeyTechnical: String = "Dyddiad y dechreuodd y broblem meddalwedd neu dechnoleg"
    override val cyaKeyCessation: String = "TBC cessation (Welsh)"
    override val cyaKeyHealth: String = "Dyddiad y gwnaeth problem iechyd eich atal rhag cyrraedd y dyddiad cau"
    override val cyaKeyUnexpectedHospital: String = "Dyddiad dechrau arhosiad yn yr ysbyty"
    override val cyaKeyLossOfStaff: String = "TBC lossOfStaff (Welsh)"
    override val cyaKeyOther: String = "Fe wnaeth problem dyddiad eich atal rhag cyrraedd y dyddiad cau"

    override val cyaHiddenBereavement: String = "Pryd y bu farw’r person"
    override val cyaHiddenCrime: String = "Pryd ddigwyddodd y drosedd"
    override val cyaHiddenFireOrFlood: String = "Pryd ddigwyddodd y tân neu lifogydd"
    override val cyaHiddenTechnical: String = "Pryd y gwnaeth y problemau technegol neu’r problemau meddalwedd ddechrau"
    override val cyaHiddenCessation: String = "TBC cessation (Welsh)"
    override val cyaHiddenHealth: String = "pryd y gwnaeth y broblem iechyd eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno"
    override val cyaHiddenUnexpectedHospital: String = "Pryd y dechreuodd yr arhosiad yn yr ysbyty?"
    override val cyaHiddenLossOfStaff: String = "TBC lossOfStaff (Welsh)"
    override val cyaHiddenOther: String = "Pryd y gwnaeth y broblem eich rhwystro am y tro cyntaf rhag bodloni’r dyddiad cau ar gyfer cyflwyno"
  }
}
