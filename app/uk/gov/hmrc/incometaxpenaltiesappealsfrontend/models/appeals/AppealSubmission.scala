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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

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
                             appealInformation: AppealInformation,
                             appealLevel: String
                           )

object AppealSubmission {
  implicit val agentDetailsFormatter: OFormat[AgentDetails] = AgentDetails.format


  private def parseAppealInformationToJson(payload: AppealInformation): JsValue =
    payload.reasonableExcuse match {
      case Bereavement =>
        Json.toJson(payload.asInstanceOf[BereavementAppealInformation])(BereavementAppealInformation.bereavementAppealWrites)
      case Crime =>
        Json.toJson(payload.asInstanceOf[CrimeAppealInformation])(CrimeAppealInformation.crimeAppealWrites)
      case FireOrFlood =>
        Json.toJson(payload.asInstanceOf[FireOrFloodAppealInformation])(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
      case LossOfStaff =>
        Json.toJson(payload.asInstanceOf[LossOfStaffAppealInformation])(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
      case TechnicalIssues =>
        Json.toJson(payload.asInstanceOf[TechnicalIssuesAppealInformation])(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
      case Health =>
        Json.toJson(payload.asInstanceOf[HealthAppealInformation])(HealthAppealInformation.healthAppealWrites)
      case UnexpectedHospital =>
        Json.toJson(payload.asInstanceOf[HealthAppealInformation])(HealthAppealInformation.healthAppealWrites)
      case Other =>
        Json.toJson(payload.asInstanceOf[OtherAppealInformation])(OtherAppealInformation.otherAppealInformationWrites)
      case reason =>
        throw new UnsupportedOperationException(s"$reason is not supported")
    }

  //scalastyle:off
  def constructModelBasedOnReasonableExcuse(reasonableExcuse: ReasonableExcuse,
                                            uploadedFiles: Option[Seq[UploadJourney]])
                                           (implicit request: CurrentUserRequestWithAnswers[_], timeMachine: TimeMachine, appConfig: AppConfig): AppealSubmission = {
    val isClientResponsibleForSubmission: Option[Boolean] = if (request.isLPP && request.isAgent) Some(true) else request.userAnswers.getAnswer(WhoPlannedToSubmitPage).map(_ == AgentClientEnum.client)
    val isClientResponsibleForLateSubmission: Option[Boolean] = if (request.isLPP && request.isAgent) Some(true)
    else if (request.userAnswers.getAnswer(WhoPlannedToSubmitPage).contains(AgentClientEnum.agent)) {
      request.userAnswers.getAnswer(WhatCausedYouToMissDeadlinePage).map(_ == AgentClientEnum.client)
    } else None

    def baseAppealSubmission(appealInfo: AppealInformation) = AppealSubmission(
      sourceSystem = "MDTP",
      taxRegime = "ITSA",
      customerReferenceNo = s"MTDITID${request.mtdItId}",
      dateOfAppeal = timeMachine.getCurrentDateTime.truncatedTo(ChronoUnit.SECONDS),
      isLPP = request.isLPP,
      appealSubmittedBy = if (request.isAgent) "agent" else "customer",
      agentDetails = request.arn.map(constructAgentDetails),
      appealInformation = appealInfo,
      appealLevel = if (request.is2ndStageAppeal) "02" else "01"
    )

    reasonableExcuse match {
      case Bereavement =>
        baseAppealSubmission(BereavementAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case Crime =>
        baseAppealSubmission(CrimeAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          reportedIssueToPolice = request.getMandatoryAnswer(CrimeReportedPage),
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case FireOrFlood =>
        baseAppealSubmission(FireOrFloodAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case LossOfStaff =>
        baseAppealSubmission(LossOfStaffAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case TechnicalIssues =>
        baseAppealSubmission(TechnicalIssuesAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent = request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay(),
          endDateOfEvent = request.getMandatoryAnswer(WhenDidEventEndPage).atStartOfDay().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS),
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case Health =>
        baseAppealSubmission(HealthAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          hospitalStayInvolved = false,
          startDateOfEvent = request.userAnswers.getAnswer(WhenDidEventHappenPage).map(_.atStartOfDay()),
          endDateOfEvent = None,
          eventOngoing = false,
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case UnexpectedHospital =>
        val isOngoingHospitalStay = !request.getMandatoryAnswer(HasHospitalStayEndedPage)
        baseAppealSubmission(HealthAppealInformation(
          reasonableExcuse = Health,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          hospitalStayInvolved = true,
          startDateOfEvent = request.userAnswers.getAnswer(WhenDidEventHappenPage).map(_.atStartOfDay()),
          endDateOfEvent = Option.when(!isOngoingHospitalStay)(request.userAnswers.getAnswer(WhenDidEventEndPage).map(_.atStartOfDay().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS))).flatten,
          eventOngoing = isOngoingHospitalStay,
          statement = None,
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
        ))

      case Other =>
        baseAppealSubmission(OtherAppealInformation(
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = request.getMandatoryAnswer(HonestyDeclarationPage),
          startDateOfEvent =
            //TODO: Date is not asked for in 2nd Stage Appeals flow, but is mandatory in Penalties BE.
            //      A change is needed to make it optional in the BE, at which point, this can be made optional and removed.
            if(request.is2ndStageAppeal) timeMachine.getCurrentDate.atStartOfDay()
            else request.getMandatoryAnswer(WhenDidEventHappenPage).atStartOfDay()
          ,
          statement = request.userAnswers.getAnswer(MissedDeadlineReasonPage),
          supportingEvidence = uploadedFiles.fold[Option[Evidence]](None)(files => if (files.isEmpty) None else Some(Evidence(files.size))),
          lateAppeal = request.isLateFirstStage(),
          lateAppealReason = if (request.isLateFirstStage()) request.userAnswers.getAnswer(LateAppealPage) else None,
          isClientResponsibleForSubmission = isClientResponsibleForSubmission,
          isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission,
          uploadedFiles = uploadedFiles
        ))
      case reason =>
        throw new UnsupportedOperationException(s"$reason is not supported and cannot be constructed to AppealInformation model")
    }
  }

  private def constructAgentDetails(arn: String)(implicit request: CurrentUserRequestWithAnswers[_]): AgentDetails =
    AgentDetails(
      agentReferenceNo = arn,
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
      "appealInformation" -> parseAppealInformationToJson(appealSubmission.appealInformation),
      "appealLevel" -> appealSubmission.appealLevel,
    ).deepMerge(
      appealSubmission.agentDetails.fold(
        Json.obj()
      )(
        agentDetails => Json.obj("agentDetails" -> agentDetails)
      )
    )
  }

  val auditWrites: Writes[AppealSubmission] = Writes { model =>
    model.appealInformation match {
      case info: BereavementAppealInformation =>
        Json.toJson(info)(BereavementAppealInformation.auditWrites)
      case info: CrimeAppealInformation =>
        Json.toJson(info)(CrimeAppealInformation.auditWrites)
      case info: FireOrFloodAppealInformation =>
        Json.toJson(info)(FireOrFloodAppealInformation.auditWrites)
      case info: LossOfStaffAppealInformation =>
        Json.toJson(info)(LossOfStaffAppealInformation.auditWrites)
      case info: TechnicalIssuesAppealInformation =>
        Json.toJson(info)(TechnicalIssuesAppealInformation.auditWrites)
      case info: HealthAppealInformation =>
        Json.toJson(info)(HealthAppealInformation.auditWrites)
      case info: OtherAppealInformation =>
        Json.toJson(info)(OtherAppealInformation.auditWrites)
      case info =>
        throw new UnsupportedOperationException(s"Audit writes for model of type '${info.getClass.getSimpleName}' are not supported")
    }
  }
}
