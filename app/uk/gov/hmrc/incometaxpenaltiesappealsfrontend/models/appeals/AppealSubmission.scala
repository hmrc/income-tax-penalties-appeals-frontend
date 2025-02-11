/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals

import play.api.libs.json._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

case class AppealSubmission(
                             sourceSystem: String,
                             taxRegime: String,
                             customerReferenceNo: String,
                             dateOfAppeal: LocalDateTime,
                             isLPP: Boolean,
                             appealSubmittedBy: String,
                             agentDetails: Option[AgentDetails],
                             appealInformation: AppealInformation
                           )

object AppealSubmission {
  implicit val agentDetailsFormatter: OFormat[AgentDetails] = AgentDetails.format


  def parseAppealInformationToJson(payload: AppealInformation): JsValue = {
    payload.reasonableExcuse match {
      case "bereavement" => Json.toJson(payload.asInstanceOf[BereavementAppealInformation])(BereavementAppealInformation.bereavementAppealWrites)
      case "crime" => Json.toJson(payload.asInstanceOf[CrimeAppealInformation])(CrimeAppealInformation.crimeAppealWrites)
      case "fireandflood" => Json.toJson(payload.asInstanceOf[FireOrFloodAppealInformation])(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
      case "lossOfEssentialStaff" => Json.toJson(payload.asInstanceOf[LossOfStaffAppealInformation])(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
      case "technicalIssue" => Json.toJson(
        payload.asInstanceOf[TechnicalIssuesAppealInformation])(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
      case "health" => Json.toJson(payload.asInstanceOf[HealthAppealInformation])(HealthAppealInformation.healthAppealWrites)
      case "other" => Json.toJson(payload.asInstanceOf[OtherAppealInformation])(OtherAppealInformation.otherAppealInformationWrites)
    }
  }

  //scalastyle:off
  def constructModelBasedOnReasonableExcuse(reasonableExcuse: String,
                                            isLateAppeal: Boolean,
                                            agentReferenceNo: Option[String],
                                            uploadedFiles: Option[Seq[UploadJourney]],
                                            mtdItId: String)
                                           (implicit request: CurrentUserRequestWithAnswers[_]): AppealSubmission = {
    val isLPP: Boolean = !request.session.get(IncomeTaxSessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString)
    val isClientResponsibleForSubmission: Option[Boolean] = if (isLPP && agentReferenceNo.isDefined) Some(true) else request.userAnswers.getAnswer(WhoPlannedToSubmitPage).map(_ == AgentClientEnum.client)
    val isClientResponsibleForLateSubmission: Option[Boolean] = if (isLPP && agentReferenceNo.isDefined) Some(true)
    else if (request.userAnswers.getAnswer(WhoPlannedToSubmitPage).contains(AgentClientEnum.agent)) {
      request.userAnswers.getAnswer(WhatCausedYouToMissDeadlinePage).map(_ == AgentClientEnum.client)
    } else None

    def baseAppealSubmission(appealInfo: AppealInformation) = AppealSubmission(
      sourceSystem = "MDTP",
      taxRegime = "ITSA",
      customerReferenceNo = s"MTDITID$mtdItId",
      dateOfAppeal = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
      isLPP = isLPP,
      appealSubmittedBy = if (agentReferenceNo.isDefined) "agent" else "customer",
      agentDetails = if (agentReferenceNo.isDefined) Some(constructAgentDetails(agentReferenceNo)) else None,
      appealInformation = appealInfo
    )

    reasonableExcuse match {
      case "bereavement" =>
        baseAppealSubmission(BereavementAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = None,
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case "crime" =>
        baseAppealSubmission(CrimeAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          reportedIssueToPolice = request.getMandatoryAnswer(CrimeReportedPage),
          statement = None,
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case "fireOrFlood" =>
        baseAppealSubmission(FireOrFloodAppealInformation(
          reasonableExcuse = "fireandflood", //API spec outlines this - the frontend says 'fire or flood' (11/10/23)
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = None,
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case "lossOfStaff" =>
        baseAppealSubmission(LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfEssentialStaff",
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = None,
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case "technicalIssues" =>
        baseAppealSubmission(TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssue",
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          endDateOfEvent = request.getMandatoryAnswer(WhenDidEventEndPage).atStartOfDay().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS),
          statement = None,
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case "health" =>
        //TODO: These will need updating when we built the health flow to retrieve from User Answers
        val isHospitalStay = request.session.get(IncomeTaxSessionKeys.wasHospitalStayRequired).get == "yes"
        val isOngoingHospitalStay = request.session.get(IncomeTaxSessionKeys.hasHealthEventEnded).contains("no")
        baseAppealSubmission(HealthAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          hospitalStayInvolved = isHospitalStay,
          startDateOfEvent = request.userAnswers.getAnswer(WhenDidEventHappenPage).map(_.atStartOfDay()),
          endDateOfEvent = Option.when(!isOngoingHospitalStay)(request.userAnswers.getAnswer(WhenDidEventEndPage).map(_.atStartOfDay().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS))).flatten,
          eventOngoing = isOngoingHospitalStay,
          statement = None,
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case "other" =>
        baseAppealSubmission(OtherAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = request.session.get(IncomeTaxSessionKeys.whyReturnSubmittedLate), //TODO: Update this when we build the page
          supportingEvidence = uploadedFiles.fold[Option[Evidence]](None)(files => if (files.isEmpty) None else Some(Evidence(files.size))),
          lateAppeal = isLateAppeal,
          lateAppealReason = if (isLateAppeal) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission,
          uploadedFiles = uploadedFiles
        ))
    }
  }

  private def constructAgentDetails(agentReferenceNo: Option[String])(implicit request: CurrentUserRequestWithAnswers[_]): AgentDetails =
    AgentDetails(
      agentReferenceNo = agentReferenceNo.get,
      isExcuseRelatedToAgent = request.userAnswers.getAnswer(WhoPlannedToSubmitPage).contains(AgentClientEnum.agent) &&
        request.userAnswers.getAnswer(WhatCausedYouToMissDeadlinePage).contains(AgentClientEnum.agent))


  implicit val writes: Writes[AppealSubmission] = (appealSubmission: AppealSubmission) => {
    Json.obj(
      "sourceSystem" -> appealSubmission.sourceSystem,
      "taxRegime" -> appealSubmission.taxRegime,
      "customerReferenceNo" -> appealSubmission.customerReferenceNo,
      "dateOfAppeal" -> appealSubmission.dateOfAppeal,
      "isLPP" -> appealSubmission.isLPP,
      "appealSubmittedBy" -> appealSubmission.appealSubmittedBy,
      "appealInformation" -> parseAppealInformationToJson(appealSubmission.appealInformation)
    ).deepMerge(
      appealSubmission.agentDetails.fold(
        Json.obj()
      )(
        agentDetails => Json.obj("agentDetails" -> agentDetails)
      )
    )
  }
}
