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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.ErrorResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.audit.AppealSubmissionAuditModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.JointAppealPage
import play.api.Logging
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{TimeMachine, UUIDGenerator}
import play.api.http.Status

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              upscanService: UpscanService,
                              idGenerator: UUIDGenerator,
                              auditService: AuditService
                             )(implicit timeMachine: TimeMachine, val appConfig: AppConfig) extends FeatureSwitching with Logging {

  def validatePenaltyIdForEnrolmentKey(penaltyId: String, isLPP: Boolean, isAdditional: Boolean, nino: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealData]] = {
    penaltiesConnector.getAppealsDataForPenalty(penaltyId, nino, isLPP, isAdditional).map {
      case None =>
        logger.warn("[AppealService][validatePenaltyIdForEnrolmentKey] - Found no appeal data for penalty")
        None
      case Some(json) =>
        val parsedAppealDataModel = Json.fromJson(json)(AppealData.format)
        parsedAppealDataModel.fold(
          _ => {
            logger.warn("[AppealService][validatePenaltyIdForEnrolmentKey] - Failed to parse appeal data")
            None
          },
          parsedModel => Some(parsedModel)
        )
    }
  }

  def validateMultiplePenaltyDataForEnrolmentKey(penaltyId: String, nino: String)
                                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[MultiplePenaltiesData]] = {
    penaltiesConnector.getMultiplePenaltiesForPrincipleCharge(penaltyId, nino).map {
      case Right(model) =>
        logger.info(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - Received Right with parsed model")
        Some(model)
      case Left(e) =>
        logger.error(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - received an error response with status ${e.status}")
        None
    }
  }


  def submitAppeal(reasonableExcuse: ReasonableExcuse)(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[SubmissionErrorResponse, SubmissionSuccessResponse]] = {
    val files = if(reasonableExcuse == Other) upscanService.getAllReadyFiles(request.journeyId) else Future.successful(Seq.empty)
    files.flatMap { uploadedFiles =>
      if (request.penaltyData.appealData.`type` != PenaltyTypeEnum.Late_Submission && request.userAnswers.getAnswer(JointAppealPage).contains(true)) {
        multipleAppeal(reasonableExcuse, uploadedFiles)
      } else {
        singleAppeal(reasonableExcuse, uploadedFiles)
      }
    }
  }


  private def singleAppeal(reasonableExcuse: ReasonableExcuse,
                           uploadedFiles: Seq[UploadJourney])
                          (implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[SubmissionErrorResponse, SubmissionSuccessResponse]] = {
    val correlationId = idGenerator.generateUUID
    implicit val modelFromRequest: AppealSubmission =
      AppealSubmission.constructModelBasedOnReasonableExcuse(
        reasonableExcuse = reasonableExcuse,
        uploadedFiles = Option.when(uploadedFiles.nonEmpty)(uploadedFiles)
      )

    for {
      response <- penaltiesConnector.submitAppeal(modelFromRequest, request.nino, request.isLPP, request.penaltyNumber, correlationId, isMultiAppeal = false)
      _ = auditSubmission(response, request.penaltyData.appealData.`type`, correlationId)
    } yield response match {
      case Right(response) =>
        logger.info("[AppealService][singleAppeal] - Received OK from the appeal submission call")
        Right(SuccessfulAppeal(response))

      case Left(error) if error.status == Status.CONFLICT =>
        logger.error(s"[AppealService][singleAppeal] - Appeal already in progress: ${error.status}")
          Left(DuplicateAppealInProgress)

      case Left(error) =>
        logger.error(s"[AppealService][singleAppeal] - Received unknown status code from connector: ${error.status}")
        Left(AppealFailed)
    }
  }.recover {
    case e =>
      logger.error(s"[AppealService][singleAppeal] - An unknown error occurred, exception type: ${e.getClass.getSimpleName}")
      Left(UnexpectedFailedFuture(e))
  }

  private def multipleAppeal(reasonableExcuse: ReasonableExcuse,
                             uploadedFiles: Seq[UploadJourney])(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[SubmissionErrorResponse, SubmissionSuccessResponse]] = {

    val firstCorrelationId = idGenerator.generateUUID
    val secondCorrelationId = idGenerator.generateUUID
    val firstPenaltyNumber = request.firstPenaltyNumber.getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] First penalty number not found in session"))
    val secondPenaltyNumber = request.secondPenaltyNumber.getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] Second penalty number not found in session"))

    implicit val modelFromRequest: AppealSubmission = AppealSubmission.constructModelBasedOnReasonableExcuse(
      reasonableExcuse = reasonableExcuse,
      uploadedFiles = Option.when(uploadedFiles.nonEmpty)(uploadedFiles)
    )

    //vals intentionally outside `for comprehension` so that they run async in parallel
    val submitLPP1 = penaltiesConnector.submitAppeal(modelFromRequest, request.nino, request.isLPP, firstPenaltyNumber, firstCorrelationId, isMultiAppeal = true)
    val submitLPP2 = penaltiesConnector.submitAppeal(modelFromRequest, request.nino, request.isLPP, secondPenaltyNumber, secondCorrelationId, isMultiAppeal = true)

    for {
      lpp1Response <- submitLPP1
      lpp2Response <- submitLPP2
      _ = logMultipleAppeal(lpp1Response, lpp2Response)
      _ = auditSubmission(lpp1Response, PenaltyTypeEnum.Late_Payment, firstCorrelationId)
      _ = auditSubmission(lpp2Response, PenaltyTypeEnum.Additional, secondCorrelationId)
    } yield (lpp1Response, lpp2Response) match {
      case (Right(lpp1Success), Right(lpp2Success)) =>
        Right(SuccessfulMultiAppeal(lpp1Success, lpp2Success))
      case (Right(lpp1Success), Left(e)) if e.status == CONFLICT =>
        Left(MultiAppealDuplicateLPP2(lpp1Success))
      case (Right(lpp1Success), Left(_)) =>
        Left(MultiAppealFailedLPP2(lpp1Success))
      case (Left(e), Right(lpp2Success)) if e.status == CONFLICT  =>
        Left(MultiAppealDuplicateLPP1(lpp2Success))
      case (Left(_), Right(lpp2Success)) =>
        Left(MultiAppealFailedLPP1(lpp2Success))
      case (Left(e1), Left(e2)) if e1.status == CONFLICT || e2.status == CONFLICT =>
        Left(MultiAppealDuplicateBoth)
      case _ =>
        Left(MultiAppealFailedBoth)
    }
  }.recover {
    case e =>
      logger.error(s"[AppealService][multipleAppeal] - An unknown error occurred, exception type: ${e.getClass.getSimpleName}")
      Left(UnexpectedFailedFuture(e))
  }

  private def auditSubmission(response: Either[ErrorResponse, AppealSubmissionResponseModel],
                              penaltyType: PenaltyTypeEnum.Value,
                              correlationId: String)(implicit modelFromRequest: AppealSubmission, user: CurrentUserRequestWithAnswers[_], hc: HeaderCarrier): Unit =
    response match {
      case Left(error) =>
        auditService.audit(AppealSubmissionAuditModel(
          penaltyNumber = user.penaltyNumber,
          penaltyType = penaltyType,
          caseId = None,
          error = Some(error.body),
          correlationId = correlationId,
          appealSubmission = modelFromRequest
        ))
      case Right(success) =>
        auditService.audit(AppealSubmissionAuditModel(
          penaltyNumber = user.penaltyNumber,
          penaltyType = penaltyType,
          caseId = success.caseId,
          error = success.error,
          correlationId = correlationId,
          appealSubmission = modelFromRequest
        ))
    }

  private def logMultipleAppeal(lpp1Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                lpp2Response: Either[ErrorResponse, AppealSubmissionResponseModel]): Unit = {
    lpp1Response match {
      case Right(model) if model.status != OK && model.status != MULTI_STATUS => throw new MatchError("unknown lpp1 response")
      case _ =>
    }
    lpp2Response match {
      case Right(model) if model.status != OK && model.status != MULTI_STATUS => throw new MatchError("unknown lpp2 response")
      case _ =>
    }

    val isSuccess = lpp1Response.exists(_.status == OK) && lpp2Response.exists(_.status == OK)
    if (!isSuccess) {
      logger.error(s"${PagerDutyKeys.MULTI_APPEAL_FAILURE} Multiple appeal submission failed")
    }
  }

}
