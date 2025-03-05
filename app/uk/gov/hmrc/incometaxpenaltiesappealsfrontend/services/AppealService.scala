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
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.ErrorResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.{AppealSubmission, AppealSubmissionResponseModel, MultiplePenaltiesData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AppealData, CurrentUserRequestWithAnswers, PenaltyTypeEnum, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.JointAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine, UUIDGenerator}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              upscanService: UpscanService,
                              idGenerator: UUIDGenerator
                             )(implicit timeMachine: TimeMachine, val appConfig: AppConfig) extends FeatureSwitching {

  def validatePenaltyIdForEnrolmentKey(penaltyId: String, isLPP: Boolean, isAdditional: Boolean, mtdItId: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealData]] = {
    penaltiesConnector.getAppealsDataForPenalty(penaltyId, mtdItId, isLPP, isAdditional).map {
      case None =>
        logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Found no appeal data for penalty ID: $penaltyId")
        None
      case Some(json) =>
        val parsedAppealDataModel = Json.fromJson(json)(AppealData.format)
        parsedAppealDataModel.fold(
          failure => {
            logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Failed to parse to model with error(s): $failure")
            None
          },
          parsedModel => Some(parsedModel)
        )
    }
  }

  def validateMultiplePenaltyDataForEnrolmentKey(penaltyId: String, mtdItId: String)
                                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[MultiplePenaltiesData]] = {
    penaltiesConnector.getMultiplePenaltiesForPrincipleCharge(penaltyId, mtdItId).map {
      case Right(model) =>
        logger.info(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - Received Right with parsed model")
        Some(model)
      case Left(e) =>
        logger.error(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - received Left with error $e")
        None
    }
  }


  def submitAppeal(reasonableExcuse: ReasonableExcuse)(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
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
                          (implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    val correlationId = idGenerator.generateUUID
    val modelFromRequest: AppealSubmission =
      AppealSubmission.constructModelBasedOnReasonableExcuse(
        reasonableExcuse = reasonableExcuse,
        isLateAppeal = request.isAppealLate(),
        agentReferenceNo = request.arn,
        uploadedFiles = Option.when(uploadedFiles.nonEmpty)(uploadedFiles),
        mtdItId = request.mtdItId
      )

    penaltiesConnector.submitAppeal(modelFromRequest, request.mtdItId, request.isLPP, request.penaltyNumber, correlationId, isMultiAppeal = false).map {
      case Left(error) =>
        logger.error(s"[AppealService][singleAppeal] - Received unknown status code from connector: ${error.status}")
        Left(error.status)
      case Right(_) =>
        logger.info("[AppealService][singleAppeal] - Received OK from the appeal submission call")
        Right((): Unit)
    }.recover {
      case e: UpstreamErrorResponse =>
        logger.error(s"[AppealService][singleAppeal] - Received 4xx/5xx response, error message: ${e.getMessage}")
        Left(e.statusCode)
      case e =>
        logger.error(s"[AppealService][singleAppeal] - An unknown error occurred, error message: ${e.getMessage}")
        Left(INTERNAL_SERVER_ERROR)
    }
  }

  private def multipleAppeal(reasonableExcuse: ReasonableExcuse,
                             uploadedFiles: Seq[UploadJourney])(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {

    val firstCorrelationId = idGenerator.generateUUID
    val secondCorrelationId = idGenerator.generateUUID
    val modelFromRequest: AppealSubmission = AppealSubmission.constructModelBasedOnReasonableExcuse(
      reasonableExcuse = reasonableExcuse,
      isLateAppeal = request.isAppealLate(),
      agentReferenceNo = request.arn,
      uploadedFiles = Option.when(uploadedFiles.nonEmpty)(uploadedFiles),
      mtdItId = request.mtdItId
    )

    val firstPenaltyNumber = request.firstPenaltyNumber.getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] First penalty number not found in session"))
    val secondPenaltyNumber = request.secondPenaltyNumber.getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] Second penalty number not found in session"))

    for {
      firstResponse <- penaltiesConnector.submitAppeal(modelFromRequest, request.mtdItId, request.isLPP, firstPenaltyNumber, firstCorrelationId, isMultiAppeal = true)
      secondResponse <- penaltiesConnector.submitAppeal(modelFromRequest, request.mtdItId, request.isLPP, secondPenaltyNumber, secondCorrelationId, isMultiAppeal = true)
    } yield {
      (firstResponse, secondResponse) match {
        case (Right(_), Right(_)) =>
          logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, firstCorrelationId, secondCorrelationId)
          // TODO implement auditing
          Right((): Unit)
        case (Right(firstResponseModel), Left(secondResponseModel)) =>
          logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, firstCorrelationId, secondCorrelationId)
          // TODO implement auditing
          logger.debug(s"[AppealService][multipleAppeal] - First penalty was $firstResponseModel, second penalty was $secondResponseModel")
          Right((): Unit)
        case (Left(firstResponseModel), Right(secondResponseModel)) =>
          logPartialFailureOfMultipleAppeal(Left(firstResponseModel), Right(secondResponseModel), firstCorrelationId, secondCorrelationId)
          // TODO implement auditing
          logger.debug(s"[AppealService][multipleAppeal] - Second penalty was $secondResponseModel, first penalty was $firstResponseModel")
          Right((): Unit)
        case _ =>
          logger.error(s"[AppealService][multipleAppeal] - Received unknown status code from connector:" +
            s" First response: $firstResponse, Second response: $secondResponse")
          Left(firstResponse.left.toOption.orElse(secondResponse.left.toOption).map(_.status).getOrElse(INTERNAL_SERVER_ERROR))
      }
    }
  }.recover {
    case e: UpstreamErrorResponse =>
      logger.error(s"[AppealService][multipleAppeal] - Received 4xx/5xx response, error message: ${e.getMessage}")
      Left(e.statusCode)
    case e =>
      logger.error(s"[AppealService][multipleAppeal] - An unknown error occurred, error message: ${e.getMessage}")
      Left(INTERNAL_SERVER_ERROR)
  }

  private def logPartialFailureOfMultipleAppeal(lpp1Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                                lpp2Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                                firstCorrelationId: String,
                                                secondCorrelationId: String)(implicit request: CurrentUserRequestWithAnswers[_]): Unit = {
    val isSuccess = lpp1Response.exists(_.status == OK) && lpp2Response.exists(_.status == OK)
    if (!isSuccess) {
      val lpp1Message = lpp1Response match {
        case Right(model) if model.status == OK => s"LPP1 appeal was submitted successfully, case ID is ${model.caseId}. Correlation ID for LPP1: $firstCorrelationId. "
        case Right(model) if model.status == MULTI_STATUS => s"LPP1 appeal was submitted successfully (case ID is ${model.caseId}) but there was an issue storing the notification for uploaded files, response body (${model.error}). Correlation ID for LPP1: $firstCorrelationId. "
        case Left(model) => s"LPP1 appeal was not submitted successfully, Reason given ${model.body}. Correlation ID for LPP1: $firstCorrelationId. "
        case _ => throw new MatchError(s"[AppealService][logPartialFailureOfMultipleAppeal] - unknown lpp1 response $lpp1Response")
      }
      val lpp2Message = lpp2Response match {
        case Right(model) if model.status == OK => s"LPP2 appeal was submitted successfully, case ID is ${model.caseId}. Correlation ID for LPP2: $secondCorrelationId. "
        case Right(model) if model.status == MULTI_STATUS => s"LPP2 appeal was submitted successfully (case ID is ${model.caseId}) but there was an issue storing the notification for uploaded files, response body (${model.error}). Correlation ID for LPP2: $secondCorrelationId. "
        case Left(model) => s"LPP2 appeal was not submitted successfully, Reason given ${model.body}. Correlation ID for LPP2: $secondCorrelationId. "
        case _ => throw new MatchError(s"[AppealService][logPartialFailureOfMultipleAppeal] - unknown lpp2 response $lpp2Response")
      }
      logger.error(s"${PagerDutyKeys.MULTI_APPEAL_FAILURE} Multiple appeal covering ${request.periodStartDate}-${request.periodEndDate} for user with MTDITID ${request.mtdItId} failed. " + lpp1Message + lpp2Message)
    }
  }

}
