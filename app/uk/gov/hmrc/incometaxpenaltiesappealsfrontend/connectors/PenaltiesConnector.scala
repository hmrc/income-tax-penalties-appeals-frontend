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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, NotFoundException, StringContextOps}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.AppealSubmissionHTTPParser.{AppealSubmissionReads, AppealSubmissionResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.MultiplePenaltiesHttpParser.{MultiplePenaltiesResponse, MultiplePenaltiesResponseReads}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.UnexpectedFailure
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.AppealSubmission
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClientV2,
                                   appConfig: AppConfig) {

  def getAppealUrlBasedOnPenaltyType(penaltyId: String, nino: String, isLPP: Boolean, isAdditional: Boolean): String = {
    if (isLPP) {
      appConfig.appealLPPDataForPenaltyUrl(penaltyId, nino, isAdditional)
    } else appConfig.appealLSPDataForPenaltyUrl(penaltyId, nino)
  }

  def getAppealsDataForPenalty(penaltyId: String, nino: String, isLPP: Boolean, isAdditional: Boolean)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getAppealsDataForPenalty] -"
    httpClient.get(
      url"${getAppealUrlBasedOnPenaltyType(penaltyId, nino, isLPP, isAdditional)}"
    ).execute[HttpResponse].map {
      response =>
        response.status match {
          case OK =>
            logger.debug(s"$startOfLogMsg OK response returned from Penalties backend for penalty with ID: $penaltyId and enrolment key $nino")
            Some(response.json)
          case NOT_FOUND =>
            logger.info(s"$startOfLogMsg Returned 404 from Penalties backend - with body: ${response.body}")
            None
          case _ =>
            PagerDutyHelper.logStatusCode("PenaltiesConnector", "getAppealsDataForPenalty", response.status)(RECEIVED_4XX_FROM_PENALTIES, RECEIVED_5XX_FROM_PENALTIES)
            logger.warn(s"$startOfLogMsg Returned unknown response ${response.status} with body: ${response.body}")
            None
        }
    }.recover {
      case e =>
        PagerDutyHelper.log("PenaltiesConnector", "getAppealsDataForPenalty", UNKNOWN_EXCEPTION_CALLING_PENALTIES)
        logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
        None
    }
  }

  def getMultiplePenaltiesForPrincipleCharge(penaltyId: String, nino: String)
                                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MultiplePenaltiesResponse] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getMultiplePenaltiesForPrincipleCharge] -"
    logger.debug(s"$startOfLogMsg Calling penalties backend with $penaltyId and $nino")
    httpClient
      .get(url"${appConfig.multiplePenaltyDataUrl(penaltyId, nino)}")
      .execute[MultiplePenaltiesResponse](MultiplePenaltiesResponseReads, ec)
  }

  def getListOfReasonableExcuses(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getListOfReasonableExcuses] -"
    httpClient
      .get(url"${appConfig.reasonableExcuseFetchUrl(nino)}")
      .execute[HttpResponse].map(
        response => Some(response.json)
      ).recover {
        case notFoundException: NotFoundException =>
          logger.error(s"$startOfLogMsg Returned 404 from penalties. With message: ${notFoundException.getMessage}")
          None
        case internalServerException: InternalServerException =>
          PagerDutyHelper.log("PenaltiesConnector", "getListOfReasonableExcuses", RECEIVED_5XX_FROM_PENALTIES)
          logger.error(s"$startOfLogMsg Returned 500 from penalties. With message: ${internalServerException.getMessage}")
          None
        case e =>
          PagerDutyHelper.log("PenaltiesConnector", "getListOfReasonableExcuses", UNKNOWN_EXCEPTION_CALLING_PENALTIES)
          logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
          None
      }
  }

  def submitAppeal(appealSubmission: AppealSubmission, nino: String, isLPP: Boolean,
                   penaltyNumber: String, correlationId: String, isMultiAppeal: Boolean)
                  (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AppealSubmissionResponse] = {

    val url = url"${appConfig.submitAppealUrl(nino, isLPP, penaltyNumber, correlationId, isMultiAppeal)}"
    logger.debug(s"[PenaltiesConnector][submitAppeal] POST $url\nSubmitting appeal model send to backend:\n${Json.toJson(appealSubmission)}")
    httpClient
      .post(url)
      .withBody(Json.toJson(appealSubmission))
      .execute[AppealSubmissionResponse]
      .recover {
        case e => {
          logger.error(s"[PenaltiesConnector][submitAppeal] - An issue occurred whilst submitting appeal to penalties backend, error message: ${e.getMessage}")
          Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"An issue occurred whilst appealing a penalty with error: ${e.getMessage}"))
        }
      }
  }
}
