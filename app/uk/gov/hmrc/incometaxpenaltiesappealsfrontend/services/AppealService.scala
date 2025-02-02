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
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.ErrorResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.{AppealSubmission, AppealSubmissionResponseModel, MultiplePenaltiesData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AppealData, CurrentUserRequestWithAnswers, PenaltyTypeEnum, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{EnrolmentUtil, IncomeTaxSessionKeys, TimeMachine, UUIDGenerator}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              timeMachine: TimeMachine,
                              idGenerator: UUIDGenerator,
                              val appConfig: AppConfig) extends FeatureSwitching {

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
    val enrolmentKey = EnrolmentUtil.buildItsaEnrolment(mtdItId)
    penaltiesConnector.getMultiplePenaltiesForPrincipleCharge(penaltyId, enrolmentKey).map {
      case Right(model) =>
        logger.info(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - Received Right with parsed model")
        Some(model)
      case Left(e) =>
        logger.error(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - received Left with error $e")
        None
    }
  }

  def getReasonableExcuses()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[ReasonableExcuse]]] = {
    penaltiesConnector.getListOfReasonableExcuses().map {
      case None =>
        logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Found no reasonable excuses")
        None
      case Some(json) =>
        val resultOfParsing: JsResult[Seq[ReasonableExcuse]] = Json.fromJson[Seq[ReasonableExcuse]](json)(ReasonableExcuse.seqReads)
        resultOfParsing.fold(
          failure => {
            logger.error(s"[AppealService][getReasonableExcuseListAndParse] - Failed to parse to model with error(s): $failure")
            None
          },
          seqOfReasonableExcuses => Some(seqOfReasonableExcuses)
        )
    }
  }


  def submitAppeal(reasonableExcuse: String,
                   mtdItId: String,
                   optArn: Option[String]
                  )(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {

    val appealType = request.session.get(IncomeTaxSessionKeys.appealType).map(PenaltyTypeEnum.withName)
    val isLPP = appealType.contains(PenaltyTypeEnum.Late_Payment) || appealType.contains(PenaltyTypeEnum.Additional)
    if (!request.session.get(IncomeTaxSessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString) && request.session.get(IncomeTaxSessionKeys.doYouWantToAppealBothPenalties).contains("yes")) {
      multipleAppeal(mtdItId, isLPP, optArn, reasonableExcuse)
    } else {
      singleAppeal(mtdItId, isLPP, optArn, reasonableExcuse)
    }

  }

  private def singleAppeal(mtdItId: String, isLPP: Boolean,
                           agentReferenceNo: Option[String],
                           reasonableExcuse: String
                          )(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    val correlationId = idGenerator.generateUUID
    val modelFromRequest: AppealSubmission =
      AppealSubmission.constructModelBasedOnReasonableExcuse(
        reasonableExcuse = reasonableExcuse,
        isLateAppeal = isAppealLate,
        agentReferenceNo = agentReferenceNo,
        uploadedFiles = None, //TODO: Retrieve the file uploads as part of MIPR-1406
        mtdItId = mtdItId
      )
    val penaltyNumber = request.session.get(IncomeTaxSessionKeys.penaltyNumber).getOrElse(throw new RuntimeException("[AppealService][singleAppeal] Penalty number not found in session"))

    penaltiesConnector.submitAppeal(modelFromRequest, mtdItId, isLPP, penaltyNumber, correlationId, isMultiAppeal = false).map {
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

  private def multipleAppeal(mtdItId: String, isLPP: Boolean,
                             agentReferenceNo: Option[String],
                             reasonableExcuse: String)(implicit request: CurrentUserRequestWithAnswers[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {

    val firstCorrelationId = idGenerator.generateUUID
    val secondCorrelationId = idGenerator.generateUUID
    val modelFromRequest: AppealSubmission = AppealSubmission.constructModelBasedOnReasonableExcuse(
      reasonableExcuse = reasonableExcuse,
      isLateAppeal = isAppealLate,
      agentReferenceNo = agentReferenceNo,
      uploadedFiles = None, //TODO: Retrieve the file uploads as part of
      mtdItId = mtdItId
    )
    val firstPenaltyNumber = request.session.get(IncomeTaxSessionKeys.firstPenaltyChargeReference).getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] First penalty number not found in session"))
    val secondPenaltyNumber = request.session.get(IncomeTaxSessionKeys.secondPenaltyChargeReference).getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] Second penalty number not found in session"))
    val dateFrom = request.session.get(IncomeTaxSessionKeys.startDateOfPeriod).getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] Start date of period not found in session"))
    val dateTo = request.session.get(IncomeTaxSessionKeys.endDateOfPeriod).getOrElse(throw new RuntimeException("[AppealService][multipleAppeal] End date of period not found in session"))

    for {
      firstResponse <- penaltiesConnector.submitAppeal(modelFromRequest, mtdItId, isLPP, firstPenaltyNumber, firstCorrelationId, isMultiAppeal = true)
      secondResponse <- penaltiesConnector.submitAppeal(modelFromRequest, mtdItId, isLPP, secondPenaltyNumber, secondCorrelationId, isMultiAppeal = true)
    } yield {
      (firstResponse, secondResponse) match {
        case (Right(_), Right(_)) =>
          logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, firstCorrelationId, secondCorrelationId, mtdItId, dateFrom, dateTo)
          // TODO implement auditing
          Right((): Unit)
        case (Right(firstResponseModel), Left(secondResponseModel)) =>
          logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, firstCorrelationId, secondCorrelationId, mtdItId, dateFrom, dateTo)
          // TODO implement auditing
          logger.debug(s"[AppealService][multipleAppeal] - First penalty was $firstResponseModel, second penalty was $secondResponseModel")
          Right((): Unit)
        case (Left(firstResponseModel), Right(secondResponseModel)) =>
          logPartialFailureOfMultipleAppeal(Left(firstResponseModel), Right(secondResponseModel), firstCorrelationId, secondCorrelationId, mtdItId, dateFrom, dateTo)
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

  def isAppealLate(implicit request: Request[_]): Boolean = {
    val dateWhereLateAppealIsApplicable: LocalDate = timeMachine.getCurrentDate.minusDays(appConfig.daysRequiredForLateAppeal)

    if (request.session.get(IncomeTaxSessionKeys.doYouWantToAppealBothPenalties).contains("yes")) {
      request.session.get(IncomeTaxSessionKeys.firstPenaltyCommunicationDate).map(LocalDate.parse).exists(_.isBefore(dateWhereLateAppealIsApplicable)) ||
        request.session.get(IncomeTaxSessionKeys.secondPenaltyCommunicationDate).map(LocalDate.parse).exists(_.isBefore(dateWhereLateAppealIsApplicable))
    } else {
      request.session.get(IncomeTaxSessionKeys.dateCommunicationSent).map(LocalDate.parse).exists(_.isBefore(dateWhereLateAppealIsApplicable))
    }
  }

  private def logPartialFailureOfMultipleAppeal(lpp1Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                                lpp2Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                                firstCorrelationId: String, secondCorrelationId: String, mtdItId: String, dateFrom: String, dateTo: String): Unit = {
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
      logger.error(s"${PagerDutyKeys.MULTI_APPEAL_FAILURE} Multiple appeal covering $dateFrom-$dateTo for user with MTDITID $mtdItId failed. " + lpp1Message + lpp2Message)
    }
  }

}
