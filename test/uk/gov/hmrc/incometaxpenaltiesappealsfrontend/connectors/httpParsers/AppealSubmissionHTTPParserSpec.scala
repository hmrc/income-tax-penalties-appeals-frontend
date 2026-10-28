/*
 * Copyright 2026 HM Revenue & Customs
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

import ch.qos.logback.classic.Level
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Logger
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR, MULTI_STATUS, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.AppealSubmissionResponseModel
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

class AppealSubmissionHTTPParserSpec extends AnyWordSpec with Matchers with MockFactory with LogCapturing {

  private val parserLogger = Logger("uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.AppealSubmissionHTTPParser")
  private val sensitiveBody = "PERSONAL_DATA_SHOULD_NOT_BE_LOGGED"

  "AppealSubmissionReads" should {
    "log successful responses at INFO and parse OK and MULTI_STATUS responses" in {
      Seq(OK, MULTI_STATUS).foreach { status =>
        val response = mock[HttpResponse]
        val expected = AppealSubmissionResponseModel(Some("REV-1234"), status)
        (() => response.status).expects().returning(status).twice()
        (() => response.json).expects().returning(Json.toJson(expected))

        withCaptureOfLoggingFrom(parserLogger) { logs =>
          AppealSubmissionHTTPParser.AppealSubmissionReads.read("POST", "/appeals", response) shouldBe Right(expected)
          logs.exists(log => log.getLevel == Level.INFO && log.getMessage.contains(s"Received $status Response")) shouldBe true
        }
      }
    }

    "warn on conflict without logging the backend response body" in {
      val response = mock[HttpResponse]
      (() => response.status).expects().returning(CONFLICT)
      (() => response.body).expects().returning(sensitiveBody)

      withCaptureOfLoggingFrom(parserLogger) { logs =>
        AppealSubmissionHTTPParser.AppealSubmissionReads.read("POST", "/appeals", response) shouldBe
          Left(UnexpectedFailure(CONFLICT, sensitiveBody))
        logs.exists(log => log.getLevel == Level.WARN && log.getMessage.contains("Conflict status has been returned")) shouldBe true
        logs.exists(_.getMessage.contains(sensitiveBody)) shouldBe false
      }
    }

    "warn on unexpected statuses without logging the backend response body" in {
      val response = mock[HttpResponse]
      (() => response.status).expects().returning(INTERNAL_SERVER_ERROR)
      (() => response.body).expects().returning(sensitiveBody)

      withCaptureOfLoggingFrom(parserLogger) { logs =>
        AppealSubmissionHTTPParser.AppealSubmissionReads.read("POST", "/appeals", response) shouldBe
          Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, sensitiveBody))
        logs.exists(log => log.getLevel == Level.WARN && log.getMessage.contains(s"status $INTERNAL_SERVER_ERROR returned")) shouldBe true
        logs.exists(_.getMessage.contains(sensitiveBody)) shouldBe false
      }
    }
  }
}



