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

import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Crime
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CrimeReportedEnum, PenaltyTypeEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission.CrimeAppealInformation
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.{AppealSubmission, MultiplePenaltiesData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.PenaltiesStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.ComponentSpecHelper

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext

class PenaltiesConnectorISpec extends ComponentSpecHelper with PenaltiesStub with FeatureSwitching {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val penaltiesConnector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]
  val correlationId: String = "correlationId"

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  "getAppealUrlBasedOnPenaltyType" should {
    "return the correct url for LPP" when {
      "the feature switch is disabled" in {
        val expectedResult =
          "http://localhost:11111/penalties/ITSA/appeals-data/late-payments/MTDITID/123456789?penaltyId=1234&isAdditional=false"

        val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "123456789", isLPP = true, isAdditional = false)
        actualResult shouldBe expectedResult
      }
      "the feature switch is enabled" in {
        enable(UseStubForBackend)
        val expectedResult =
          "http://localhost:11111/income-tax-penalties-stubs/ITSA/appeals-data/late-payments/MTDITID/123456789?penaltyId=1234&isAdditional=false"

        val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "123456789", isLPP = true, isAdditional = false)
        actualResult shouldBe expectedResult
      }
    }
    "return the correct url for LPP Additional" when {
      "the feature switch is disabled" in {
        val expectedResult =
          "http://localhost:11111/penalties/ITSA/appeals-data/late-payments/MTDITID/123456789?penaltyId=1234&isAdditional=true"
        val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "123456789", isLPP = true, isAdditional = true)
        actualResult shouldBe expectedResult
      }
      "the feature switch is enabled" in {
        enable(UseStubForBackend)
        val expectedResult =
          "http://localhost:11111/income-tax-penalties-stubs/ITSA/appeals-data/late-payments/MTDITID/123456789?penaltyId=1234&isAdditional=true"
        val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "123456789", isLPP = true, isAdditional = true)
        actualResult shouldBe expectedResult
      }
    }

    "return the correct url for LSP" when {
      "the feature switch is disabled" in {
        val expectedResult =
          "http://localhost:11111/penalties/ITSA/appeals-data/late-submissions/MTDITID/123456789?penaltyId=1234"
        val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "123456789", isLPP = false, isAdditional = false)
        actualResult shouldBe expectedResult
      }
      "the feature switch is enabled" in {
        enable(UseStubForBackend)
        val expectedResult =
          "http://localhost:11111/income-tax-penalties-stubs/ITSA/appeals-data/late-submissions/MTDITID/123456789?penaltyId=1234"
        val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "123456789", isLPP = false, isAdditional = false)
        actualResult shouldBe expectedResult
      }
    }
  }

  "getAppealsDataForPenalty" should {
    s"return $Some and the $JsValue returned by the call" when {
      "the call to the backend is successful" in {
        successfulGetAppealDataResponse("1234", testMtdItId)
        val sampleJsonToPassBack: JsValue = Json.obj(
          "type" -> PenaltyTypeEnum.Late_Submission,
          "startDate" -> LocalDate.of(2020, 1, 1).toString,
          "endDate" -> LocalDate.of(2020, 1, 31).toString,
          "dueDate" -> LocalDate.of(2020, 3, 7).toString,
          "dateCommunicationSent" -> LocalDate.of(2020, 3, 8).toString
        )
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789", isLPP = false, isAdditional = false))
        result.isDefined shouldBe true
        result.get shouldBe sampleJsonToPassBack
      }
      "the call to the stub is successful" in {
        enable(UseStubForBackend)
        successfulGetAppealDataResponse("1234", testMtdItId, isStubbed = true)
        val sampleJsonToPassBack: JsValue = Json.obj(
          "type" -> PenaltyTypeEnum.Late_Submission,
          "startDate" -> LocalDate.of(2020, 1, 1).toString,
          "endDate" -> LocalDate.of(2020, 1, 31).toString,
          "dueDate" -> LocalDate.of(2020, 3, 7).toString,
          "dateCommunicationSent" -> LocalDate.of(2020, 3, 8).toString
        )
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789", isLPP = false, isAdditional = false))
        result.isDefined shouldBe true
        result.get shouldBe sampleJsonToPassBack
      }
    }

    s"return $None" when {
      "the call returns 404" in {
        failedGetAppealDataResponse("1234", testMtdItId, status = Status.NOT_FOUND)
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", testMtdItId, isLPP = false, isAdditional = false))
        result.isDefined shouldBe false
      }

      "the call returns some unknown response" in {
        failedGetAppealDataResponse("1234", testMtdItId, status = Status.IM_A_TEAPOT)
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", testMtdItId, isLPP = false, isAdditional = false))
        result.isDefined shouldBe false
      }

      "the call fails completely with no response" in {
        failedCall("1234", testMtdItId)
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", testMtdItId, isLPP = false, isAdditional = false))
        result.isDefined shouldBe false
      }
    }
  }

  "getMultiplePenaltiesForPrincipleCharge" should {
    s"return Right with the parsed model" when {
      "the call to the backend is successful" in {
        successfulGetMultiplePenalties("1234", testMtdItId)
        val expectedResponse: MultiplePenaltiesData = MultiplePenaltiesData(
          firstPenaltyChargeReference = "123456789",
          firstPenaltyAmount = 101.01,
          secondPenaltyChargeReference = "123456790",
          secondPenaltyAmount = 1.02,
          firstPenaltyCommunicationDate = LocalDate.parse("2023-04-06"),
          secondPenaltyCommunicationDate = LocalDate.parse("2023-04-07")
        )
        val result = await(penaltiesConnector.getMultiplePenaltiesForPrincipleCharge("1234", testMtdItId))
        result.isRight shouldBe true
        result shouldBe Right(expectedResponse)
      }
      "the call to the stub is successful" in {
        enable(UseStubForBackend)
        successfulGetMultiplePenalties("1234", testMtdItId, isStubbed = true)
        val expectedResponse: MultiplePenaltiesData = MultiplePenaltiesData(
          firstPenaltyChargeReference = "123456789",
          firstPenaltyAmount = 101.01,
          secondPenaltyChargeReference = "123456790",
          secondPenaltyAmount = 1.02,
          firstPenaltyCommunicationDate = LocalDate.parse("2023-04-06"),
          secondPenaltyCommunicationDate = LocalDate.parse("2023-04-07")
        )
        val result = await(penaltiesConnector.getMultiplePenaltiesForPrincipleCharge("1234", testMtdItId))
        result.isRight shouldBe true
        result shouldBe Right(expectedResponse)
      }
    }

    "return Left" when {
      s"only a single penalty is found for the principle charge and ${Status.NO_CONTENT} is returned" in {
        failedGetMultiplePenalties("1234", testMtdItId, Status.NO_CONTENT)
        val result = await(penaltiesConnector.getMultiplePenaltiesForPrincipleCharge("1234", testMtdItId))
        result.isLeft shouldBe true
      }

      s"${Status.NOT_FOUND} is returned" in {
        failedGetMultiplePenalties("1234", testMtdItId, Status.NOT_FOUND)
        val result = await(penaltiesConnector.getMultiplePenaltiesForPrincipleCharge("1234", testMtdItId))
        result.isLeft shouldBe true
      }

      s"${Status.INTERNAL_SERVER_ERROR} is returned" in {
        failedGetMultiplePenalties("1234", testMtdItId, Status.INTERNAL_SERVER_ERROR)
        val result = await(penaltiesConnector.getMultiplePenaltiesForPrincipleCharge("1234", testMtdItId))
        result.isLeft shouldBe true
      }
    }
  }

  "getListOfReasonableExcuses" should {
    s"return $Some and the $JsValue returned by the call" when {
      "the call to the backend is successful" in {
        successfulFetchReasonableExcuseResponse(testMtdItId)
        val sampleJsonToPassBack: JsValue = Json.obj(
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
        val result = await(penaltiesConnector.getListOfReasonableExcuses(testMtdItId))
        result.isDefined shouldBe true
        result.get shouldBe sampleJsonToPassBack
      }
      "the call to the stub is successful" in {
        enable(UseStubForBackend)
        successfulFetchReasonableExcuseResponse(testMtdItId, isStubbed = true)
        val sampleJsonToPassBack: JsValue = Json.obj(
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
        val result = await(penaltiesConnector.getListOfReasonableExcuses(testMtdItId))
        result.isDefined shouldBe true
        result.get shouldBe sampleJsonToPassBack
      }
    }

    s"return $None" when {
      "the call returns 404" in {
        failedFetchReasonableExcuseListResponse(testMtdItId, Status.NOT_FOUND)
        val result = await(penaltiesConnector.getListOfReasonableExcuses(testMtdItId))
        result.isDefined shouldBe false
      }

      "the call returns 500" in {
        failedFetchReasonableExcuseListResponse(testMtdItId, Status.INTERNAL_SERVER_ERROR)
        val result = await(penaltiesConnector.getListOfReasonableExcuses(testMtdItId))
        result.isDefined shouldBe false
      }


      "the call returns some unknown response" in {
        failedFetchReasonableExcuseListResponse(testMtdItId, Status.IM_A_TEAPOT)
        val result = await(penaltiesConnector.getListOfReasonableExcuses(testMtdItId))
        result.isDefined shouldBe false
      }

      "the call fails completely with no response" in {
        failedCallForFetchingReasonableExcuse(testMtdItId)
        val result = await(penaltiesConnector.getListOfReasonableExcuses(testMtdItId))
        result.isDefined shouldBe false
      }
    }
  }

  "submitAppeal" should {
    "return the response of the call to the backend" in {
      successfulAppealSubmission(testMtdItId, isLPP = false, penaltyNumber = "123456789")
      val model = AppealSubmission(
        sourceSystem = "MDTP",
        taxRegime = "ITSA",
        customerReferenceNo = "MTDITID123456789",
        dateOfAppeal = LocalDateTime.of(2023, 3, 1, 0, 0, 0),
        isLPP = false,
        appealSubmittedBy = "client",
        agentDetails = None,
        appealInformation = CrimeAppealInformation(
          reasonableExcuse = Crime,
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2023-01-01").atStartOfDay(),
          reportedIssueToPolice = CrimeReportedEnum.yes,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "123456789", isLPP = false, "123456789", correlationId, isMultiAppeal = true))
      result.isRight shouldBe true
      result.toOption.get.status shouldBe OK
    }
    "return the response of the call to the stub" in {
      enable(UseStubForBackend)
      successfulAppealSubmission(testMtdItId, isLPP = false, penaltyNumber = "123456789", isStubbed = true)
      val model = AppealSubmission(
        sourceSystem = "MDTP",
        taxRegime = "ITSA",
        customerReferenceNo = "MTDITID123456789",
        dateOfAppeal = LocalDateTime.of(2023, 3, 1, 0, 0, 0),
        isLPP = false,
        appealSubmittedBy = "client",
        agentDetails = None,
        appealInformation = CrimeAppealInformation(
          reasonableExcuse = Crime,
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2023-01-01").atStartOfDay(),
          reportedIssueToPolice = CrimeReportedEnum.yes,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "123456789", isLPP = false, "123456789", correlationId, isMultiAppeal = true))
      result.isRight shouldBe true
      result.toOption.get.status shouldBe OK
    }

    "return the response of the call for LPP" in {
      successfulAppealSubmission(testMtdItId, isLPP = true, penaltyNumber = "123456789")
      val model = AppealSubmission(
        sourceSystem = "MDTP",
        taxRegime = "ITSA",
        customerReferenceNo = "MTDITID123456789",
        dateOfAppeal = LocalDateTime.of(2023, 3, 1, 0, 0, 0),
        isLPP = true,
        appealSubmittedBy = "client",
        agentDetails = None,
        appealInformation = CrimeAppealInformation(
          reasonableExcuse = Crime,
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2023-01-01").atStartOfDay(),
          reportedIssueToPolice = CrimeReportedEnum.yes,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "123456789", isLPP = true, "123456789", correlationId, isMultiAppeal = true))
      result.isRight shouldBe true
      result.toOption.get.status shouldBe OK
    }

    "return Internal Server Error if an exception occurs" in {
      failedAppealSubmissionWithFault(testMtdItId, isLPP = true, penaltyNumber = "123456789")
      val model = AppealSubmission(
        sourceSystem = "MDTP",
        taxRegime = "ITSA",
        customerReferenceNo = "MTDITID123456789",
        dateOfAppeal = LocalDateTime.of(2023, 3, 1, 0, 0, 0),
        isLPP = true,
        appealSubmittedBy = "client",
        agentDetails = None,
        appealInformation = CrimeAppealInformation(
          reasonableExcuse = Crime,
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2023-01-01").atStartOfDay(),
          reportedIssueToPolice = CrimeReportedEnum.yes,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "123456789", isLPP = true, "123456789", correlationId, isMultiAppeal = true))
      result.isLeft shouldBe true
      result.left.toOption.get.status shouldBe INTERNAL_SERVER_ERROR
      result.left.toOption.get.body should include("An issue occurred whilst appealing a penalty with error: Connection reset")
    }
  }
}
