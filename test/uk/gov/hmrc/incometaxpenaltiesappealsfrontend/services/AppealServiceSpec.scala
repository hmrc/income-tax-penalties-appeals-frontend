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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission.OtherAppealInformation
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.{AppealSubmission, AppealSubmissionResponseModel, MultiplePenaltiesData}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{CrimeReportedPage, HonestyDeclarationPage, ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks.MockUpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine, UUIDGenerator}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AppealServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with LogCapturing with GuiceOneAppPerSuite
  with MockUpscanService
  with FileUploadFixtures {

  val mockPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]
  val mockTimeMachine: TimeMachine = mock[TimeMachine]
  val mockUUIDGenerator: UUIDGenerator = mock[UUIDGenerator]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val fakeRequestForCrimeJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = UserAnswers(testJourneyId)
        .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "123456789")
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
        .setAnswer(ReasonableExcusePage, "crime")
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.dateCommunicationSent -> "2021-12-01",
        IncomeTaxSessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "no"
      ))

  val fakeRequestForCrimeJourneyMultiple: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = UserAnswers(testJourneyId)
        .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "123456789")
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
        .setAnswer(ReasonableExcusePage, "crime")
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.dateCommunicationSent -> "2021-12-01",
        IncomeTaxSessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "yes",
        IncomeTaxSessionKeys.firstPenaltyChargeReference -> "123456789",
        IncomeTaxSessionKeys.secondPenaltyChargeReference -> "123456788",
        IncomeTaxSessionKeys.startDateOfPeriod -> "2024-01-01",
        IncomeTaxSessionKeys.endDateOfPeriod -> "2024-01-31"
      ))

  val fakeRequestForOtherJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = UserAnswers(testJourneyId)
        .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "123456789")
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(ReasonableExcusePage, "other")
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.dateCommunicationSent -> "2021-12-01",
        IncomeTaxSessionKeys.whyReturnSubmittedLate -> "This is a reason.",
        IncomeTaxSessionKeys.isUploadEvidence -> "yes",
        IncomeTaxSessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "no"
      ))

  val fakeRequestForOtherJourneyDeclinedUploads: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = UserAnswers(testJourneyId)
        .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "123456789")
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(ReasonableExcusePage, "other")
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.dateCommunicationSent -> "2021-12-01",
        IncomeTaxSessionKeys.whyReturnSubmittedLate -> "This is a reason.",
        IncomeTaxSessionKeys.isUploadEvidence -> "no",
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "no"
      ))

  val appealDataAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01",
      | "dueDate": "2020-02-07",
      | "dateCommunicationSent": "2020-02-08"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPP: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_PAYMENT",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01",
      | "dueDate": "2020-02-07",
      | "dateCommunicationSent": "2020-02-08"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPPAdditional: JsValue = Json.parse(
    """
      |{
      | "type": "ADDITIONAL",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01",
      | "dueDate": "2020-02-07",
      | "dateCommunicationSent": "2020-02-08"
      |}
      |""".stripMargin)

  val multiplePenaltiesModel: MultiplePenaltiesData = MultiplePenaltiesData(
    firstPenaltyChargeReference = "123456789",
    firstPenaltyAmount = 101.01,
    secondPenaltyChargeReference = "123456790",
    secondPenaltyAmount = 101.02,
    firstPenaltyCommunicationDate = LocalDate.parse("2022-01-01"),
    secondPenaltyCommunicationDate = LocalDate.parse("2022-01-02")
  )

  class Setup {
    reset(mockPenaltiesConnector)
    reset(mockTimeMachine)
    reset(mockUUIDGenerator)

    val service: AppealService =
      new AppealService(mockPenaltiesConnector, mockUpscanService, mockTimeMachine, mockUUIDGenerator, appConfig)

    when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2020, 2, 1))
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

  "getReasonableExcuses" should {
    s"call the connector and parse the result to $Some $Seq $ReasonableExcuse" in new Setup {
      val jsonRepresentingSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavement"
          |    },
          |    {
          |      "type": "crime",
          |      "descriptionKey": "reasonableExcuses.crime"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFlood"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      when(mockPenaltiesConnector.getListOfReasonableExcuses(eqTo(testMtdItId))(any(), any()))
        .thenReturn(Future.successful(
          Some(jsonRepresentingSeqOfReasonableExcuses)
        ))

      val result: Option[Seq[ReasonableExcuse]] = await(service.getReasonableExcuses(testMtdItId))

      result shouldBe Some(Seq(
        ReasonableExcuse(
          `type` = "bereavement",
          descriptionKey = "reasonableExcuses.bereavement",
          isOtherOption = false
        ),
        ReasonableExcuse(
          `type` = "crime",
          descriptionKey = "reasonableExcuses.crime",
          isOtherOption = false
        ),
        ReasonableExcuse(
          `type` = "fireOrFlood",
          descriptionKey = "reasonableExcuses.fireOrFlood",
          isOtherOption = false
        )
      ))
    }

    s"call the connector and return $None" when {
      "the connector call succeeds but invalid json is returned and therefore can not be parsed" in new Setup {
        val jsonRepresentingInvalidSeqOfReasonableExcuses: JsValue = Json.parse(
          """
            |{
            |  "excusesssss": [
            |    {
            |      "type": "bereavement",
            |      "descriptionKey": "reasonableExcuses.bereavement"
            |    },
            |    {
            |      "type": "crime",
            |      "descriptionKey": "reasonableExcuses.crime"
            |    },
            |    {
            |      "type": "fireOrFlood",
            |      "descriptionKey": "reasonableExcuses.fireOrFlood"
            |    }
            |  ]
            |}
            |""".stripMargin
        )
        when(mockPenaltiesConnector.getListOfReasonableExcuses(eqTo(testMtdItId))(any(), any()))
          .thenReturn(Future.successful(
            Some(jsonRepresentingInvalidSeqOfReasonableExcuses)
          ))

        val result: Option[Seq[ReasonableExcuse]] = await(service.getReasonableExcuses(testMtdItId))
        result shouldBe None
      }

      "the connector call fails" in new Setup {
        when(mockPenaltiesConnector.getListOfReasonableExcuses(eqTo(testMtdItId))(any(), any()))
          .thenReturn(Future.successful(None))

        val result: Option[Seq[ReasonableExcuse]] = await(service.getReasonableExcuses(testMtdItId))
        result shouldBe None
      }
    }
  }

  "submitAppeal" should {
    "parse the session keys into a model and return true" when {
      "the journey is 'Other' and there are uploaded files that are ready" should {
        "the connector call is successful" in new Setup {

          val submissionModelCapture: ArgumentCaptor[AppealSubmission] = ArgumentCaptor.forClass(classOf[AppealSubmission])

          when(mockPenaltiesConnector.submitAppeal(submissionModelCapture.capture(), any(), any(), any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

          when(mockPenaltiesConnector.submitAppeal(submissionModelCapture.capture(), any(), any(), any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

          when(mockPenaltiesConnector.submitAppeal(submissionModelCapture.capture(), any(), any(), any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

          mockGetAllReadyFiles(testJourneyId)(Future.successful(Seq(callbackModel, callbackModel2)))

          val result: Either[Int, Unit] = await(service.submitAppeal("other", testMtdItId, None)(fakeRequestForOtherJourney, implicitly, implicitly))
          result shouldBe Right((): Unit)

          submissionModelCapture.getValue.appealInformation.asInstanceOf[OtherAppealInformation].uploadedFiles shouldBe Some(Seq(callbackModel, callbackModel2))
        }
      }
      "the connector call is successful for appealing multiple penalties" in new Setup {

        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

        val result: Either[Int, Unit] = await(service.submitAppeal("crime", testMtdItId, None)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

        result shouldBe Right((): Unit)
      }
    }

    "succeed if one of 2 appeal submissions fail and log a PD (LPP1 fails)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), ArgumentMatchers.eq(true))(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), ArgumentMatchers.eq(true))(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))

      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime", testMtdItId, None)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

          result shouldBe Right((): Unit)

          verify(mockUUIDGenerator, times(2)).generateUUID

          logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with MTDITID 123456789 failed. " +
            s"LPP1 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was submitted successfully, case ID is Some(REV-1234). Correlation ID for LPP2: uuid-2. ") shouldBe true
        }
      }
    }

    "succeed if one of 2 appeal submissions fail and log a PD (LPP2 fails)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), ArgumentMatchers.eq(true))(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), ArgumentMatchers.eq(true))(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))

      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime", testMtdItId, None)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))

          result shouldBe Right((): Unit)

          logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with MTDITID 123456789 failed. " +
            s"LPP1 appeal was submitted successfully, case ID is Some(REV-1234). Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP2: uuid-2. ") shouldBe true
        }
      }
    }

    "return Left" when {
      "the connector returns a non-200 response" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, ""))))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime", testMtdItId, None)(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(BAD_GATEWAY)
      }

      "the connector returns a non-200 response for multiple submissions" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, ""))))
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, ""))))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime", testMtdItId, None)(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
        result shouldBe Left(BAD_GATEWAY)
      }

      "the connector throws an exception" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.failed(new Exception("I failed.")))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime", testMtdItId, None)(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }
    }
  }

  "isAppealLate" should {
    val fakeRequestForAppealingBothPenalties: (LocalDate, LocalDate) => CurrentUserRequestWithAnswers[AnyContent] = (lpp1Date: LocalDate, lpp2Date: LocalDate) =>
      CurrentUserRequestWithAnswers(
        mtdItId = testMtdItId,
        userAnswers = UserAnswers(testJourneyId)
          .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "123456789")
          .setAnswer(HonestyDeclarationPage, true)
          .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
          .setAnswer(ReasonableExcusePage, "crime")
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
      )(
        //TODO: These will all move to be UserAnswers as part of future stories
        FakeRequest().withSession(
          IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "yes",
          IncomeTaxSessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
          IncomeTaxSessionKeys.firstPenaltyCommunicationDate -> lpp1Date.toString,
          IncomeTaxSessionKeys.secondPenaltyCommunicationDate -> lpp2Date.toString
        ))

    val fakeRequestForAppealingSinglePenalty: LocalDate => CurrentUserRequestWithAnswers[AnyContent] = (date: LocalDate) =>
      CurrentUserRequestWithAnswers(
        mtdItId = testMtdItId,
        userAnswers = UserAnswers(testJourneyId)
          .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "123456789")
          .setAnswer(HonestyDeclarationPage, true)
          .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
          .setAnswer(ReasonableExcusePage, "crime")
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
      )(
        //TODO: These will all move to be UserAnswers as part of future stories
        FakeRequest().withSession(
          IncomeTaxSessionKeys.dateCommunicationSent -> date.toString,
          IncomeTaxSessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString
        ))

    "return true" when {
      "communication date of penalty > 30 days ago" in new Setup {
        when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2022, 1, 1))
        val result: Boolean = service.isAppealLate(fakeRequestForAppealingSinglePenalty(LocalDate.of(2021, 12, 1)))
        result shouldBe true
      }

      "appealing both penalties and LPP1 is late" in new Setup {
        when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2022, 1, 1))
        val result: Boolean = service.isAppealLate(fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 1)))
        result shouldBe true
      }

      "appealing both penalties and both are late" in new Setup {
        when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2022, 4, 1))
        val result: Boolean = service.isAppealLate(fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 1)))
        result shouldBe true
      }
    }

    "return false" when {
      "communication date of penalty < 30 days ago" in new Setup {
        when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2022, 1, 1))
        val result: Boolean = service.isAppealLate(fakeRequestForAppealingSinglePenalty(LocalDate.of(2021, 12, 31)))
        result shouldBe false
      }

      "appealing both penalties and LPP1 and LPP2 are not late" in new Setup {
        when(mockTimeMachine.getCurrentDate).thenReturn(LocalDate.of(2022, 1, 1))
        val result: Boolean = service.isAppealLate(fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 31), LocalDate.of(2021, 12, 31)))
        result shouldBe false
      }
    }
  }
}
