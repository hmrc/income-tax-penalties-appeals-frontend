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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers

import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UpscanInitiateResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys.{INVALID_JSON_RECEIVED_FROM_UPSCAN, RECEIVED_4XX_FROM_UPSCAN, RECEIVED_5XX_FROM_UPSCAN}

trait UpscanInitiateHttpParser {

  def read(journeyId: String): HttpReads[Either[ErrorResponse, UpscanInitiateResponse]] =
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          response.json.validate[UpscanInitiateResponse] match {
            case JsSuccess(model, _) => Right(model)
            case _ =>
              PagerDutyHelper.log("UpscanInitiateHttpParser", "read", INVALID_JSON_RECEIVED_FROM_UPSCAN, Map("journeyId" -> journeyId))
              Left(InvalidJson)
          }
        case BAD_REQUEST =>
          logger.debug(s"[UpScanInitiateResponseReads][read] Bad request returned with reason: ${response.body}")
          PagerDutyHelper.log("UpscanInitiateHttpParser", "read", RECEIVED_4XX_FROM_UPSCAN, Map("journeyId" -> journeyId))
          Left(BadRequest)
        case status =>
          PagerDutyHelper.logStatusCode("UpscanInitiateHttpParser", "read", status, Map("journeyId" -> journeyId))(RECEIVED_4XX_FROM_UPSCAN, RECEIVED_5XX_FROM_UPSCAN)
          Left(UnexpectedFailure(status, s"Unexpected response, status $status returned"))
      }
}
