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
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.messageCount.MessageCount
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys.{INVALID_JSON_RECEIVED_FROM_MESSAGE_FRONTEND, RECEIVED_4XX_FROM_MESSAGE_FRONTEND, RECEIVED_5XX_FROM_MESSAGE_FRONTEND}

object MessageCountHttpParser {

  sealed trait MessagesCountFailure {
    val message: String
  }

  case class MessagesCountUnexpectedFailure(status: Int) extends MessagesCountFailure {
    override val message: String = s"Unexpected response, status $status returned when retrieving messages count"
  }

  case object MessagesCountResponseBadRequest extends MessagesCountFailure {
    override val message: String = "A bad request was returned when calling message-frontend for messages count"
  }

  case object MessagesCountResponseMalformed extends MessagesCountFailure {
    override val message: String = "Response body received from message-frontend for messages count was malformed"
  }

  type GetMessageCountResponse = Either[MessagesCountFailure, MessageCount]

  implicit object GetMessageCount extends HttpReads[GetMessageCountResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetMessageCountResponse =
      response.status match {
        case OK =>
          response.json.validate[MessageCount] match {
            case JsSuccess(model, _) =>
              logger.info(s"[GetMessageCount][read] Successful call to retrieve messages count, count was: ${model.count}.")
              Right(model)
            case JsError(errors) =>
              logger.debug(s"[GetMessageCount][read] Failed to parse messages count response from message-frontend to MessageCount model - failures: $errors")
              logger.error("[GetMessageCount][read] Failed to parse messages count response from message-frontend to MessageCount model")
              PagerDutyHelper.log("GetMessageCount", "read", INVALID_JSON_RECEIVED_FROM_MESSAGE_FRONTEND)
              Left(MessagesCountResponseMalformed)
          }
        case BAD_REQUEST =>
          logger.error(s"[GetMessageCount][read]: Bad request returned with reason: ${response.body}")
          PagerDutyHelper.log("GetMessageCount", "read", RECEIVED_4XX_FROM_MESSAGE_FRONTEND)
          Left(MessagesCountResponseBadRequest)
        case status =>
          logger.error(s"[GetMessageCount][read]: Unexpected response, status $status returned with reason: ${response.body}")
          PagerDutyHelper.logStatusCode("GetMessageCount", "read", status)(RECEIVED_4XX_FROM_MESSAGE_FRONTEND, RECEIVED_5XX_FROM_MESSAGE_FRONTEND)
          Left(MessagesCountUnexpectedFailure(status))
      }
  }
}
