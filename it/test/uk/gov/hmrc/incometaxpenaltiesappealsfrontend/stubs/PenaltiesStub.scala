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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyTypeEnum

import java.time.LocalDate
import scala.jdk.CollectionConverters._

trait PenaltiesStub {

  private def appealUriForLSP(mtditid: String, isStubbed: Boolean): String = {
    if (isStubbed) "/income-tax-penalties-stubs" else "/penalties"
  } + s"/ITSA/appeals-data/late-submissions/MTDITID/$mtditid"

  private def appealUriForLPP(mtditid: String, isStubbed: Boolean): String = {
    if (isStubbed) "/income-tax-penalties-stubs" else "/penalties"
  } + s"/ITSA/appeals-data/late-payments/MTDITID/$mtditid"

  private def fetchReasonableExcuseUri(mtditid: String, isStubbed: Boolean): String = {
    if (isStubbed) "/income-tax-penalties-stubs" else "/penalties"
  } + s"/ITSA/appeals-data/reasonable-excuses/MTDITID/$mtditid"

  private def submitAppealUri(mtditid: String, isStubbed: Boolean): String = {
    if (isStubbed) "/income-tax-penalties-stubs" else "/penalties"
  } + s"/ITSA/appeals/submit-appeal/MTDITID/$mtditid"

  private val submitAppealQueryParams = (isLPP: Boolean, penaltyNumber: String) => Map[String, StringValuePattern](
    "isLPP" -> equalTo(isLPP.toString),
    "penaltyNumber" -> equalTo(penaltyNumber),
    "correlationId" -> matching(".*")
  )

  private def multiplePenaltiesUri(penaltyId: String, mtditid: String, isStubbed: Boolean): String = {
    if (isStubbed) "/income-tax-penalties-stubs" else "/penalties"
  } + s"/ITSA/appeals-data/multiple-penalties/MTDITID/$mtditid?penaltyId=$penaltyId"

  def successfulGetAppealDataResponse(
                                       penaltyId: String,
                                       mtditid: String,
                                       isLPP: Boolean = false,
                                       isAdditional: Boolean = false,
                                       isStubbed: Boolean = false
                                     ): StubMapping = {
    val typeOfPenalty =
      if (isAdditional) PenaltyTypeEnum.Additional
      else if (isLPP) PenaltyTypeEnum.Late_Payment
      else PenaltyTypeEnum.Late_Submission
    val uri = if (isLPP) appealUriForLPP(mtditid, isStubbed) else appealUriForLSP(mtditid, isStubbed)
    val extraAdditionalParam = if (isLPP) s"&isAdditional=$isAdditional" else ""
    stubFor(
      get(
        urlEqualTo(
          s"$uri?penaltyId=$penaltyId$extraAdditionalParam"
        )
      ).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json.obj(
              "type" -> typeOfPenalty,
              "startDate" -> LocalDate.of(2020, 1, 1).toString,
              "endDate" -> LocalDate.of(2020, 1, 31).toString,
              "dueDate" -> LocalDate.of(2020, 3, 7).toString,
              "dateCommunicationSent" -> LocalDate.of(2020, 3, 8).toString
            ).toString()
          )
      )
    )
  }

  def successfulFetchReasonableExcuseResponse(mtditid: String, isStubbed: Boolean = false): StubMapping = {

    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri(mtditid, isStubbed)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(
              Json
                .obj(
                  "excuses" -> Json.arr(
                    Json.obj(
                      "type" -> "type1",
                      "descriptionKey" -> "key1"
                    ),
                    Json.obj(
                      "type" -> "type2",
                      "descriptionKey" -> "key2"
                    ),
                    Json.obj(
                      "type" -> "other",
                      "descriptionKey" -> "key3"
                    )
                  )
                )
                .toString()
            )
        )
    )
  }

  def successfulAppealSubmission(mtditid: String, isLPP: Boolean, penaltyNumber: String,isStubbed: Boolean = false): StubMapping = {
    val responseBody =
      """
        |{
        | "caseId": "PR-1234",
        | "status": 200
        |}
        |""".stripMargin
    stubFor(
      post(urlPathMatching(submitAppealUri(mtditid, isStubbed))).withQueryParams(submitAppealQueryParams(isLPP, penaltyNumber).asJava)
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(responseBody)
        )
    )
  }

  def successfulGetMultiplePenalties(penaltyId: String, mtditid: String, isStubbed: Boolean = false): StubMapping = {
    stubFor(
      get(urlEqualTo(multiplePenaltiesUri(penaltyId, mtditid, isStubbed)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(
              Json.obj(
                "firstPenaltyChargeReference" -> "123456789",
                "firstPenaltyAmount" -> "101.01",
                "secondPenaltyChargeReference" -> "123456790",
                "secondPenaltyAmount" -> "1.02",
                "firstPenaltyCommunicationDate" -> "2023-04-06",
                "secondPenaltyCommunicationDate" -> "2023-04-07"
              ).toString()
            )
        )
    )
  }

  def failedGetMultiplePenalties(penaltyId: String, mtditid: String, status: Int = Status.INTERNAL_SERVER_ERROR, isStubbed: Boolean = false): StubMapping = {
    stubFor(
      get(urlEqualTo(multiplePenaltiesUri(penaltyId, mtditid, isStubbed)))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
  }

  def failedAppealSubmissionWithFault(mtditid: String, isLPP: Boolean, penaltyNumber: String, isStubbed: Boolean = false): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri(mtditid, isStubbed))).withQueryParams(submitAppealQueryParams(isLPP, penaltyNumber).asJava)
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )
  }

  def failedAppealSubmission(mtditid: String, isLPP: Boolean, penaltyNumber: String, status: Option[Int] = None, isStubbed: Boolean = false): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri(mtditid, isStubbed))).withQueryParams(submitAppealQueryParams(isLPP, penaltyNumber).asJava)
        .willReturn(
          aResponse()
            .withStatus(status.fold(Status.INTERNAL_SERVER_ERROR)(identity))
            .withBody("Some issue with document storage")
        )
    )
  }

  def failedGetAppealDataResponse(
                                   penaltyId: String,
                                   mtditid: String,
                                   status: Int = Status.INTERNAL_SERVER_ERROR,
                                   isStubbed: Boolean = false
                                 ): StubMapping = {
    stubFor(
      get(
        urlEqualTo(
          appealUriForLSP(mtditid, isStubbed) + s"?penaltyId=$penaltyId"
        )
      ).willReturn(
        aResponse()
          .withStatus(status)
      )
    )
  }

  def failedFetchReasonableExcuseListResponse(mtditid: String,
                                              status: Int = Status.INTERNAL_SERVER_ERROR,
                                              isStubbed: Boolean = false): StubMapping = {
    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri(mtditid, isStubbed)))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
  }

  def failedCall(penaltyId: String, mtditid: String, isStubbed: Boolean = false): StubMapping = {
    stubFor(
      get(
        urlEqualTo(
          appealUriForLSP(mtditid, isStubbed) + s"?penaltyId=$penaltyId"
        )
      ).willReturn(
        aResponse()
          .withFault(Fault.CONNECTION_RESET_BY_PEER)
      )
    )
  }

  def failedCallForFetchingReasonableExcuse(mtditid: String, isStubbed: Boolean = false): StubMapping = {
    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri(mtditid, isStubbed)))
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )
  }
}
