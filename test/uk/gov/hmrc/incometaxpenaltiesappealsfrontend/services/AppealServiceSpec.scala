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

import fixtures.FileUploadFixtures
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Crime, Other}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission.OtherAppealInformation
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.audit.AppealSubmissionAuditModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks.{MockAuditService, MockUpscanService}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{TimeMachine, UUIDGenerator}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AppealServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with LogCapturing with GuiceOneAppPerSuite
  with MockUpscanService
  with MockAuditService
  with FileUploadFixtures {

  val mockPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]
  val mockTimeMachine: TimeMachine = mock[TimeMachine]
  val mockUUIDGenerator: UUIDGenerator = mock[UUIDGenerator]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    reset(mockPenaltiesConnector)
    reset(mockTimeMachine)
    reset(mockUUIDGenerator)
    reset(mockAuditService)

    val service: AppealService =
      new AppealService(
        mockPenaltiesConnector,
        mockUpscanService,
        mockUUIDGenerator,
        mockAuditService
      )(mockTimeMachine, appConfig)

    when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2020, 2, 1))
    when(mockTimeMachine.getCurrentDateTime).thenReturn(LocalDateTime.of(2020, 2, 1, 12, 15, 45))
    when(mockUUIDGenerator.generateUUID).thenReturn("uuid-1", "uuid-2")
  }

  "validatePenaltyIdForEnrolmentKey" should {
    "return None when the connector returns None" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe None
    }

    "return None when the connectors returns Json that cannot be parsed to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(Json.parse("{}"))))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe None
    }

    "return Some when the connector returns Json that is parsable to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJson)))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe Some(Json.fromJson[AppealData](appealDataAsJson).get)
    }

    "return Some when the connector returns Json that is parsable to a model for LPP" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJsonLPP)))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe Some(Json.fromJson[AppealData](appealDataAsJsonLPP).get)
    }

    "return Some when the connector returns Json that is parsable to a model for LPP - Additional penalty" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJsonLPPAdditional)))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = true, testMtdItId)(implicitly, implicitly))

      result shouldBe Some(Json.fromJson[AppealData](appealDataAsJsonLPPAdditional).get)
    }
  }

  "validateMultiplePenaltyDataForEnrolmentKey" should {
    "return None" when {
      "the connector returns a left with an UnexpectedFailure" in new Setup {
        when(mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(eqTo("123"), eqTo(testMtdItId))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))))

        val result: Option[MultiplePenaltiesData] = await(service.validateMultiplePenaltyDataForEnrolmentKey("123", testMtdItId)(implicitly, implicitly))

        result shouldBe None
      }

      "the connector returns returns InvalidJson that cannot be parsed to a model" in new Setup {
        when(mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(eqTo("123"), eqTo(testMtdItId))(any(), any()))
          .thenReturn(Future.successful(Left(InvalidJson)))

        val result: Option[MultiplePenaltiesData] = await(service.validateMultiplePenaltyDataForEnrolmentKey("123", testMtdItId)(implicitly, implicitly))

        result shouldBe None
      }
    }

    "return Some" when {
      "the connector returns Json that can be parsed to a model" in new Setup {
        when(mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(eqTo("123"), eqTo(testMtdItId))(any(), any()))
          .thenReturn(Future.successful(Right(multiplePenaltiesModel)))

        val result: Option[MultiplePenaltiesData] = await(service.validateMultiplePenaltyDataForEnrolmentKey("123", testMtdItId)(implicitly, implicitly))

        result shouldBe Some(multiplePenaltiesModel)
      }
    }
  }

  "submitAppeal" should {
    "return Right" when {
      "the journey is 'Other' and there are uploaded files that are ready" should {
        "the connector call is successful" in new Setup {

          val submissionModelCapture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])

          when(mockPenaltiesConnector.submitAppeal(submissionModelCapture.capture(), any(), any(), any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

          mockGetAllReadyFiles(testJourneyId)(Future.successful(Seq(callbackModel, callbackModel2)))

          val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] =
            await(service.submitAppeal(Other)(fakeRequestForOtherJourney, implicitly, implicitly))

          result shouldBe Right(SuccessfulAppeal(AppealSubmissionResponseModel(Some("REV-1234"), OK)))

          val submissionModel: AppealSubmission = submissionModelCapture.getValue
          submissionModel.appealInformation.asInstanceOf[OtherAppealInformation].uploadedFiles shouldBe Some(Seq(callbackModel, callbackModel2))

          verifyAuditEvent(AppealSubmissionAuditModel(
            penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
            penaltyType = PenaltyTypeEnum.Late_Submission,
            caseId = Some("REV-1234"),
            error = None,
            correlationId = "uuid-1",
            appealSubmission = submissionModelCapture.getValue
          )(fakeRequestForOtherJourney))
        }
      }
      "the connector call is successful for appealing multiple penalties" in new Setup {

        val lpp1Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])
        val lpp2Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])

        when(mockPenaltiesConnector.submitAppeal(lpp1Capture.capture(), any(), any(), any(), eqTo("uuid-1"), any())(any(), any()))
          .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

        when(mockPenaltiesConnector.submitAppeal(lpp2Capture.capture(), any(), any(), any(), eqTo("uuid-2"), any())(any(), any()))
          .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-5678"), OK))))

        val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] =
          await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

        result shouldBe Right(
          SuccessfulMultiAppeal(
            lpp1Success = AppealSubmissionResponseModel(Some("REV-1234"), OK),
            lpp2Success = AppealSubmissionResponseModel(Some("REV-5678"), OK)
          )
        )

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Late_Payment,
          caseId = Some("REV-1234"),
          error = None,
          correlationId = "uuid-1",
          appealSubmission = lpp1Capture.getValue
        )(fakeRequestForCrimeJourneyMultiple))

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Additional,
          caseId = Some("REV-5678"),
          error = None,
          correlationId = "uuid-2",
          appealSubmission = lpp2Capture.getValue
        )(fakeRequestForCrimeJourneyMultiple))
      }
    }

    "return Left" when {

      "if one of 2 appeal submissions fail and log a PD (LPP1 fails)" in new Setup {

        val lpp1Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])
        val lpp2Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])

        when(mockPenaltiesConnector.submitAppeal(lpp1Capture.capture(), any(), any(), eqTo("123456789"), any(), eqTo(true))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))

        when(mockPenaltiesConnector.submitAppeal(lpp2Capture.capture(), any(), any(), eqTo("123456788"), any(), eqTo(true))(any(), any()))
          .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-5678"), OK))))

        withCaptureOfLoggingFrom(logger) {
          logs => {

            val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] =
              await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

            result shouldBe Left(MultiAppealFailedLPP1(AppealSubmissionResponseModel(Some("REV-5678"), OK)))

            verify(mockUUIDGenerator, times(2)).generateUUID

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Late_Payment,
              caseId = None,
              error = Some("Some issue with submission"),
              correlationId = "uuid-1",
              appealSubmission = lpp1Capture.getValue
            )(fakeRequestForCrimeJourneyMultiple))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Additional,
              caseId = Some("REV-5678"),
              error = None,
              correlationId = "uuid-2",
              appealSubmission = lpp2Capture.getValue
            )(fakeRequestForCrimeJourneyMultiple))

            logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with MTDITID 123456789 failed. " +
              s"LPP1 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP1: uuid-1. " +
              s"LPP2 appeal was submitted successfully (case ID is REV-5678). Correlation ID for LPP2: uuid-2. ") shouldBe true
          }
        }
      }

      "if one of 2 appeal submissions fail and log a PD (LPP2 fails)" in new Setup {

        val lpp1Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])
        val lpp2Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])

        when(mockPenaltiesConnector.submitAppeal(lpp1Capture.capture(), any(), any(), eqTo("123456789"), any(), eqTo(true))(any(), any()))
          .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

        when(mockPenaltiesConnector.submitAppeal(lpp2Capture.capture(), any(), any(), eqTo("123456788"), any(), eqTo(true))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] = await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

            result shouldBe Left(MultiAppealFailedLPP2(AppealSubmissionResponseModel(Some("REV-1234"), OK)))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Late_Payment,
              caseId = Some("REV-1234"),
              error = None,
              correlationId = "uuid-1",
              appealSubmission = lpp1Capture.getValue
            )(fakeRequestForCrimeJourneyMultiple))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Additional,
              caseId = None,
              error = Some("Some issue with submission"),
              correlationId = "uuid-2",
              appealSubmission = lpp2Capture.getValue
            )(fakeRequestForCrimeJourneyMultiple))

            logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with MTDITID 123456789 failed. " +
              s"LPP1 appeal was submitted successfully (case ID is REV-1234). Correlation ID for LPP1: uuid-1. " +
              s"LPP2 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP2: uuid-2. ") shouldBe true
          }
        }
      }

      "if both of the appeal submissions fail" in new Setup {

        val lpp1Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])
        val lpp2Capture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])

        when(mockPenaltiesConnector.submitAppeal(lpp1Capture.capture(), any(), any(), eqTo("123456789"), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, "Error1"))))

        when(mockPenaltiesConnector.submitAppeal(lpp2Capture.capture(), any(), any(), eqTo("123456788"), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, "Error2"))))

        val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] = await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
        result shouldBe Left(MultiAppealFailedBoth)

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Late_Payment,
          caseId = None,
          error = Some("Error1"),
          correlationId = "uuid-1",
          appealSubmission = lpp1Capture.getValue
        )(fakeRequestForCrimeJourneyMultiple))

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Additional,
          caseId = None,
          error = Some("Error2"),
          correlationId = "uuid-2",
          appealSubmission = lpp2Capture.getValue
        )(fakeRequestForCrimeJourneyMultiple))
      }

      "the connector throws an exception" in new Setup {

        val e = new Exception("I failed.")

        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.failed(e))

        val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] = await(service.submitAppeal(Crime)(fakeRequestForCrimeJourney, implicitly, implicitly))

        result shouldBe Left(UnexpectedFailedFuture(e))
      }
    }
  }
}
