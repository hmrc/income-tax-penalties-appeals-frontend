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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.SessionData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys.{INVALID_JSON_RECEIVED_FROM_INCOME_TAX_SESSION_DATA, RECEIVED_4XX_FROM_INCOME_TAX_SESSION_DATA, RECEIVED_5XX_FROM_INCOME_TAX_SESSION_DATA}

object IncomeTaxSessionDataHttpParser {

  type GetSessionDataResponse = Either[ErrorResponse, Option[SessionData]]

  def reads()(implicit user: CurrentUserRequestWithAnswers[_]): HttpReads[GetSessionDataResponse] =
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          response.json.validate[SessionData] match {
            case JsSuccess(model, _) =>
              logger.debug(s"[GetSessionData][read] Successful call for journeyId: ${user.journeyId}, response:\n${Json.toJson(model)}")
              Right(Some(model))
            case JsError(errors) =>
              logger.debug(s"[GetSessionData][read] Failed to parse session data response for journeyId: ${user.journeyId} - failures: $errors")
              logger.error(s"[GetSessionData][read] Failed to parse session data response for journeyId: ${user.journeyId}")
              PagerDutyHelper.log("GetSessionData", "read", INVALID_JSON_RECEIVED_FROM_INCOME_TAX_SESSION_DATA)
              Left(InvalidJson)
          }
        case NOT_FOUND =>
          logger.warn(s"[GetSessionData][read] No session data was returned for journeyId: ${user.journeyId}")
          Right(None)
        case BAD_REQUEST =>
          logger.error(s"[GetSessionData][read]: Bad request returned for journeyId: ${user.journeyId} with reason: ${response.body}")
          PagerDutyHelper.log("GetSessionData", "read", RECEIVED_4XX_FROM_INCOME_TAX_SESSION_DATA)
          Left(BadRequest)
        case status =>
          logger.error(s"[GetSessionData][read]: Unexpected response, status $status returned with reason: ${response.body}")
          PagerDutyHelper.logStatusCode("GetSessionData", "read", status)(RECEIVED_4XX_FROM_INCOME_TAX_SESSION_DATA, RECEIVED_5XX_FROM_INCOME_TAX_SESSION_DATA)
          Left(UnexpectedFailure(status, response.body))
      }
}
