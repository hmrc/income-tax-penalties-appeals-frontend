/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs

import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.test.Helpers.UNAUTHORIZED
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{EnrolmentUtil, WiremockMethods}

trait AuthStub extends WiremockMethods {

  val authUrl = "/auth/authorise"

  def stubAuth[T](status: Int, body: T)(implicit writes: Writes[T]): Unit =
    when(method = POST, uri = authUrl)
      .thenReturn(status = status, body = writes.writes(body))

  def stubAuthFailure(): Unit =
    when(method = POST, uri = authUrl)
      .thenReturn(status = UNAUTHORIZED, headers = Map(HeaderNames.WWW_AUTHENTICATE -> s"""MDTP detail="MissingBearerToken""""))

  val successfulIndividualAuthResponse: JsObject =
    Json.obj(
      "affinityGroup" -> "Individual",
      "allEnrolments" -> Json.arr(
        Json.obj(
          "key" -> EnrolmentUtil.incomeTaxEnrolmentKey,
          "identifiers" -> Json.arr(
            Json.obj(
              "key" -> EnrolmentUtil.mtdItIdKey,
              "value" -> "123456789",
              "state" -> "Activated"
            )
          )
        )
      ),
      "internalId" -> "internal",
      "groupIdentifier" -> "123456789-ABCD-123456789",
      "credentialRole" -> "role1",
      "optionalCredentials" -> Json.obj(
        "providerId" -> "12345",
        "providerType" -> "credType"
      ),
      "nino" -> "AA123456A"
    )

  val successfulAgentAuthResponse: JsObject =
    Json.obj(
      "affinityGroup" -> "Agent",
      "allEnrolments" -> Json.arr(
        Json.obj(
          "key" -> EnrolmentUtil.agentEnrolmentKey,
          "identifiers" -> Json.arr(
            Json.obj(
              "key" -> "AgentReferenceNumber",
              "value" -> "123456789",
              "state" -> "Activated"
            )
          )
        )
      ),
      "delegatedAuthRule" -> EnrolmentUtil.agentDelegatedAuthRuleKey,
      "internalId" -> "internal",
      "groupIdentifier" -> "123456789-ABCD-123456789",
      "credentialRole" -> "role1",
      "optionalCredentials" -> Json.obj(
        "providerId" -> "12345",
        "providerType" -> "credType"
      )
    )

}
