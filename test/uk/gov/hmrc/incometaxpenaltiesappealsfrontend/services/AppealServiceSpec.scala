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
import org.scalamock.matchers.ArgCapture.CaptureOne
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Crime, Other}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission.OtherAppealInformation
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.audit.AppealSubmissionAuditModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks.{MockAuditService, MockUpscanService}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{TimeMachine, UUIDGenerator}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AppealServiceSpec extends AnyWordSpec with Matchers with MockFactory with LogCapturing with GuiceOneAppPerSuite
  with MockUpscanService
  with MockAuditService
  with FileUploadFixtures {

  val mockPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]
  val mockTimeMachine: TimeMachine = mock[TimeMachine]
  val mockUUIDGenerator: UUIDGenerator = mock[UUIDGenerator]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    val service: AppealService =
      new AppealService(
        mockPenaltiesConnector,
        mockUpscanService,
        mockUUIDGenerator,
        stubAuditService
      )(mockTimeMachine, appConfig)
  }

  "validatePenaltyIdForEnrolmentKey" should {
    "return None when the connector returns None" in new Setup {
      (mockPenaltiesConnector.getAppealsDataForPenalty(_: String, _: String, _: Boolean, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *, *, *)
        .returning(Future.successful(None))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe None
    }

    "return None when the connectors returns Json that cannot be parsed to a model" in new Setup {
      (mockPenaltiesConnector.getAppealsDataForPenalty(_: String, _: String, _: Boolean, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *, *, *)
        .returning(Future.successful(Some(Json.parse("{}"))))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe None
    }

    "return Some when the connector returns Json that is parsable to a model" in new Setup {
      (mockPenaltiesConnector.getAppealsDataForPenalty(_: String, _: String, _: Boolean, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *, *, *)
        .returning(Future.successful(Some(appealDataAsJson)))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe Some(Json.fromJson[AppealData](appealDataAsJson).get)
    }

    "return Some when the connector returns Json that is parsable to a model for LPP" in new Setup {

      (mockPenaltiesConnector.getAppealsDataForPenalty(_: String, _: String, _: Boolean, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *, *, *)
        .returning(Future.successful(Some(appealDataAsJsonLPP)))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = false, testMtdItId)(implicitly, implicitly))

      result shouldBe Some(Json.fromJson[AppealData](appealDataAsJsonLPP).get)
    }

    "return Some when the connector returns Json that is parsable to a model for LPP - Additional penalty" in new Setup {
      (mockPenaltiesConnector.getAppealsDataForPenalty(_: String, _: String, _: Boolean, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *, *, *)
        .returning(Future.successful(Some(appealDataAsJsonLPPAdditional)))

      val result: Option[AppealData] = await(service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = true, testMtdItId)(implicitly, implicitly))

      result shouldBe Some(Json.fromJson[AppealData](appealDataAsJsonLPPAdditional).get)
    }
  }

  "validateMultiplePenaltyDataForEnrolmentKey" should {
    "return None" when {
      "the connector returns a left with an UnexpectedFailure" in new Setup {
        (mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(
          _: String,
          _: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("123", testMtdItId, *, *)
          .returning(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))))

        val result: Option[MultiplePenaltiesData] = await(service.validateMultiplePenaltyDataForEnrolmentKey("123", testMtdItId)(implicitly, implicitly))

        result shouldBe None
      }

      "the connector returns returns InvalidJson that cannot be parsed to a model" in new Setup {
        (mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(
          _: String,
          _: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("123", testMtdItId, *, *)
          .returning(Future.successful(Left(InvalidJson)))

        val result: Option[MultiplePenaltiesData] = await(service.validateMultiplePenaltyDataForEnrolmentKey("123", testMtdItId)(implicitly, implicitly))

        result shouldBe None
      }
    }

    "return Some" when {
      "the connector returns Json that can be parsed to a model" in new Setup {
        (mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(
          _: String,
          _: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("123", testMtdItId, *, *)
          .returning(Future.successful(Right(multiplePenaltiesModel)))

        val result: Option[MultiplePenaltiesData] = await(service.validateMultiplePenaltyDataForEnrolmentKey("123", testMtdItId)(implicitly, implicitly))

        result shouldBe Some(multiplePenaltiesModel)
      }
    }
  }

  "submitAppeal" should {
    "return Right" when {
      "the journey is 'Other' and there are uploaded files that are ready" should {
        "the connector call is successful" in new Setup {

          (() => mockTimeMachine.getCurrentDate).expects().returning(LocalDate.of(2020, 2, 1)).atLeastOnce().atLeastOnce()
          (() => mockTimeMachine.getCurrentDateTime).expects().returning(LocalDateTime.of(2020, 2, 1, 12, 15, 45)).atLeastOnce()
          (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-1")

          val submissionModelCapture: CaptureOne[AppealSubmission] = CaptureOne[AppealSubmission]()
          (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _:String, _:Boolean, _:String, _:String,_:Boolean)(_:ExecutionContext, _:HeaderCarrier))
            .expects(capture(submissionModelCapture), *, *, *, *, *, *, *)
            .returning(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

          mockGetAllReadyFiles(testJourneyId)(Future.successful(Seq(callbackModel, callbackModel2)))

          stubAuditEvent()

          val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] =
            await(service.submitAppeal(Other)(fakeRequestForOtherJourney, implicitly, implicitly))

          verifyAuditEvent(AppealSubmissionAuditModel(
            penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
            penaltyType = PenaltyTypeEnum.Late_Submission,
            caseId = Some("REV-1234"),
            error = None,
            correlationId = "uuid-1",
            appealSubmission = submissionModelCapture.value
          )(fakeRequestForOtherJourney))

          result shouldBe Right(SuccessfulAppeal(AppealSubmissionResponseModel(Some("REV-1234"), OK)))

          val submissionModel: AppealSubmission = submissionModelCapture.value
          submissionModel.appealInformation.asInstanceOf[OtherAppealInformation].uploadedFiles shouldBe Some(Seq(callbackModel, callbackModel2))
        }
      }
      "the connector call is successful for appealing multiple penalties" in new Setup {

        (() => mockTimeMachine.getCurrentDateTime).expects().returning(LocalDateTime.of(2020, 2, 1, 12, 15, 45)).atLeastOnce()
        (() => mockTimeMachine.getCurrentDate).expects().returning(LocalDate.of(2020, 2, 1)).atLeastOnce().atLeastOnce()
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-1")
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-2")

        val lpp1Capture: CaptureOne[AppealSubmission] = CaptureOne[AppealSubmission]()
        val lpp2Capture: CaptureOne[AppealSubmission] = CaptureOne[AppealSubmission]()

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp1Capture),*,*,*,"uuid-1",*,*,*)
          .returning(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp2Capture),*,*,*,"uuid-2",*,*,*)
          .returning(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-5678"), OK))))

        stubAuditEvent()

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
          appealSubmission = lpp1Capture.value
        )(fakeRequestForCrimeJourneyMultiple))

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Additional,
          caseId = Some("REV-5678"),
          error = None,
          correlationId = "uuid-2",
          appealSubmission = lpp2Capture.value
        )(fakeRequestForCrimeJourneyMultiple))
      }
    }

    "return Left" when {

      "if one of 2 appeal submissions fail and log a PD (LPP1 fails)" in new Setup {

        (() => mockTimeMachine.getCurrentDateTime).expects().returning(LocalDateTime.of(2020, 2, 1, 12, 15, 45)).atLeastOnce()
        (() => mockTimeMachine.getCurrentDate).expects().returning(LocalDate.of(2020, 2, 1)).atLeastOnce()
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-1")
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-2")

        val lpp1Capture = CaptureOne[AppealSubmission]()
        val lpp2Capture = CaptureOne[AppealSubmission]()

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp1Capture), *, *, "123456789", *, true, *, *)
          .returning(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp2Capture), *, *, "123456788", *, true, *, *)
          .returning(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-5678"), OK))))

        withCaptureOfLoggingFrom(logger) {
          logs => {

            val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] =
              await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

            stubAuditEvent()

            result shouldBe Left(MultiAppealFailedLPP1(AppealSubmissionResponseModel(Some("REV-5678"), OK)))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Late_Payment,
              caseId = None,
              error = Some("Some issue with submission"),
              correlationId = "uuid-1",
              appealSubmission = lpp1Capture.value
            )(fakeRequestForCrimeJourneyMultiple))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Additional,
              caseId = Some("REV-5678"),
              error = None,
              correlationId = "uuid-2",
              appealSubmission = lpp2Capture.value
            )(fakeRequestForCrimeJourneyMultiple))

            logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with MTDITID 123456789 failed. " +
              s"LPP1 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP1: uuid-1. " +
              s"LPP2 appeal was submitted successfully (case ID is REV-5678). Correlation ID for LPP2: uuid-2. ") shouldBe true
          }
        }
      }

      "if one of 2 appeal submissions fail and log a PD (LPP2 fails)" in new Setup {

        (() => mockTimeMachine.getCurrentDateTime).expects().returning(LocalDateTime.of(2020, 2, 1, 12, 15, 45)).atLeastOnce()
        (() => mockTimeMachine.getCurrentDate).expects().returning(LocalDate.of(2020, 2, 1)).atLeastOnce().atLeastOnce()
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-1")
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-2")


        val lpp1Capture = CaptureOne[AppealSubmission]()
        val lpp2Capture = CaptureOne[AppealSubmission]()

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp1Capture), *, *, "123456789", *, true, *, *)
          .returning(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp2Capture), *, *, "123456788", *, true, *, *)
          .returning(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] = await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

            stubAuditEvent()

            result shouldBe Left(MultiAppealFailedLPP2(AppealSubmissionResponseModel(Some("REV-1234"), OK)))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Late_Payment,
              caseId = Some("REV-1234"),
              error = None,
              correlationId = "uuid-1",
              appealSubmission = lpp1Capture.value
            )(fakeRequestForCrimeJourneyMultiple))

            verifyAuditEvent(AppealSubmissionAuditModel(
              penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
              penaltyType = PenaltyTypeEnum.Additional,
              caseId = None,
              error = Some("Some issue with submission"),
              correlationId = "uuid-2",
              appealSubmission = lpp2Capture.value
            )(fakeRequestForCrimeJourneyMultiple))

            logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with MTDITID 123456789 failed. " +
              s"LPP1 appeal was submitted successfully (case ID is REV-1234). Correlation ID for LPP1: uuid-1. " +
              s"LPP2 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP2: uuid-2. ") shouldBe true
          }
        }
      }

      "if both of the appeal submissions fail" in new Setup {

        (() => mockTimeMachine.getCurrentDateTime).expects().returning(LocalDateTime.of(2020, 2, 1, 12, 15, 45)).atLeastOnce()
        (() => mockTimeMachine.getCurrentDate).expects().returning(LocalDate.of(2020, 2, 1)).atLeastOnce().atLeastOnce()
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-1")
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-2")


        val lpp1Capture = CaptureOne[AppealSubmission]()
        val lpp2Capture = CaptureOne[AppealSubmission]()

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp1Capture), *, *, "123456789", *, *, *, *)
          .returning(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, "Error1"))))


        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(capture(lpp2Capture), *, *, "123456788", *, *, *, *)
          .returning(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, "Error2"))))


        stubAuditEvent()

        val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] = await(service.submitAppeal(Crime)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
        result shouldBe Left(MultiAppealFailedBoth)

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Late_Payment,
          caseId = None,
          error = Some("Error1"),
          correlationId = "uuid-1",
          appealSubmission = lpp1Capture.value
        )(fakeRequestForCrimeJourneyMultiple))

        verifyAuditEvent(AppealSubmissionAuditModel(
          penaltyNumber = fakeRequestForOtherJourney.penaltyNumber,
          penaltyType = PenaltyTypeEnum.Additional,
          caseId = None,
          error = Some("Error2"),
          correlationId = "uuid-2",
          appealSubmission = lpp2Capture.value
        )(fakeRequestForCrimeJourneyMultiple))
      }

      "the connector throws an exception" in new Setup {

        (() => mockTimeMachine.getCurrentDateTime).expects().returning(LocalDateTime.of(2020, 2, 1, 12, 15, 45)).atLeastOnce()
        (() => mockTimeMachine.getCurrentDate).expects().returning(LocalDate.of(2020, 2, 1)).atLeastOnce().atLeastOnce()
        (() => mockUUIDGenerator.generateUUID).expects().returning("uuid-1")

        val e = new Exception("I failed.")

        (mockPenaltiesConnector.submitAppeal(_: AppealSubmission, _: String, _:Boolean, _:String, _: String, _:Boolean)(_: ExecutionContext, _: HeaderCarrier))
          .expects(*, *, *, *, *, *, *, *)
          .returning(Future.failed(e))

        val result: Either[SubmissionErrorResponse, SubmissionSuccessResponse] = await(service.submitAppeal(Crime)(fakeRequestForCrimeJourney, implicitly, implicitly))

        result shouldBe Left(UnexpectedFailedFuture(e))
      }
    }
  }
}
