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

package fixtures

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Bereavement, Crime, FireOrFlood, Health, LossOfStaff, Other, TechnicalIssues}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.MultiplePenaltiesData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.{SessionData, UserAnswers}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys

import java.time.LocalDate
import java.util.UUID

trait BaseFixtures {

  val testMtdItId = "123456789"
  val testArn = "00123456"
  val testJourneyId: String = "journeyId123"
  val testNino = "AA123456A"
  val testUtr = "9999912345"
  val testInternalId: String = UUID.randomUUID().toString
  val testSessionId: String = UUID.randomUUID().toString

  val sessionData: SessionData = SessionData(
    mtditid = testMtdItId,
    nino = testNino,
    utr = testUtr,
    internalId = testInternalId,
    sessionId = testSessionId,
  )

  val latePaymentAppealData: AppealData = AppealData(
    `type` = PenaltyTypeEnum.Late_Payment,
    startDate = LocalDate.of(2024, 1, 1),
    endDate = LocalDate.of(2024, 1, 31),
    dueDate = LocalDate.of(2022, 1, 1),
    dateCommunicationSent = LocalDate.of(2021, 12, 1)
  )

  val lateSubmissionAppealData: AppealData = AppealData(
    `type` = PenaltyTypeEnum.Late_Submission,
    startDate = LocalDate.of(2024, 1, 1),
    endDate = LocalDate.of(2024, 1, 31),
    dueDate = LocalDate.of(2022, 1, 1),
    dateCommunicationSent = LocalDate.of(2021, 12, 1)
  )

  val multiplePenaltiesModel: MultiplePenaltiesData = MultiplePenaltiesData(
    firstPenaltyChargeReference = "123456789",
    firstPenaltyAmount = 101.01,
    secondPenaltyChargeReference = "123456788",
    secondPenaltyAmount = 101.02,
    firstPenaltyCommunicationDate = LocalDate.parse("2022-01-01"),
    secondPenaltyCommunicationDate = LocalDate.parse("2022-01-02")
  )

  val penaltyDataLSP: PenaltyData = PenaltyData(
    penaltyNumber = "123456789",
    is2ndStageAppeal = false,
    appealData = lateSubmissionAppealData,
    multiplePenaltiesData = None
  )

  val penaltyDataLPP: PenaltyData = PenaltyData(
    penaltyNumber = "123456790",
    is2ndStageAppeal = false,
    appealData = latePaymentAppealData,
    multiplePenaltiesData = None
  )

  val emptyUserAnswers: UserAnswers = UserAnswers(testJourneyId)
  val emptyUserAnswersWithLSP: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
  val emptyUserAnswersWithLPP: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLPP)
  val emptyUserAnswersWithMultipleLPPs: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLPP.copy(multiplePenaltiesData = Some(multiplePenaltiesModel)))
  val emptyUserAnswersWithLSP2ndStage: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP.copy(is2ndStageAppeal = true))
  val emptyUserAnswersWithLPP2ndStage: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLPP.copy(is2ndStageAppeal = true))
  val emptyUserAnswersWithMultipleLPPs2ndStage: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLPP.copy(multiplePenaltiesData = Some(multiplePenaltiesModel), is2ndStageAppeal = true))

  val fakeRequestForBereavementJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
        .setAnswer(ReasonableExcusePage, Bereavement)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyDataLSP
    )(FakeRequest())

  val fakeRequestForCrimeJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
        .setAnswer(ReasonableExcusePage, Crime)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyDataLSP
    )(FakeRequest())

  val fakeRequestForCrimeJourneyMultiple: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      is2ndStageAppeal = false,
      appealData = latePaymentAppealData,
      multiplePenaltiesData = Some(multiplePenaltiesModel)
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(JointAppealPage, true)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
        .setAnswer(ReasonableExcusePage, Crime)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyData
    )(FakeRequest())
  }

  val fakeRequestForFireAndFloodJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
        .setAnswer(ReasonableExcusePage, FireOrFlood)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyDataLSP
    )(FakeRequest())

  val fakeRequestForLossOfStaffJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
        .setAnswer(ReasonableExcusePage, LossOfStaff)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyDataLSP
    )(FakeRequest())

  val fakeRequestForTechnicalIssuesJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
        .setAnswer(ReasonableExcusePage, TechnicalIssues)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
        .setAnswer(WhenDidEventEndPage, LocalDate.of(2022, 1, 4)),
      penaltyData = penaltyDataLSP
    )(FakeRequest())

  val fakeRequestForHealthIssuesJourney: CurrentUserRequestWithAnswers[AnyContent] =
    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)
        .setAnswer(ReasonableExcusePage, Health)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
        .setAnswer(WhenDidEventEndPage, LocalDate.of(2022, 1, 4)),
      penaltyData = penaltyDataLSP
    )(FakeRequest().withSession(
        //TODO: These will be removed and become pages from UserAnswers when the Health journey is implemented
        IncomeTaxSessionKeys.wasHospitalStayRequired -> "yes",
        IncomeTaxSessionKeys.hasHealthEventEnded-> "no"
    ))

  val fakeRequestForOtherJourney: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      is2ndStageAppeal = false,
      appealData = lateSubmissionAppealData,
      multiplePenaltiesData = None
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(JointAppealPage, false)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(ReasonableExcusePage, Other)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
        .setAnswer(ExtraEvidencePage, true)
        .setAnswer(MissedDeadlineReasonPage, "This is a reason."),
      penaltyData = penaltyData
    )(FakeRequest())
  }

  val fakeRequestForOtherJourneyDeclinedUploads: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      is2ndStageAppeal = false,
      appealData = lateSubmissionAppealData,
      multiplePenaltiesData = None
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUserAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(JointAppealPage, false)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(ReasonableExcusePage, Other)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1))
        .setAnswer(ExtraEvidencePage, false)
        .setAnswer(MissedDeadlineReasonPage, "This is a reason."),
      penaltyData = penaltyData
    )(FakeRequest())
  }

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

  def userRequestWithAnswers(userAnswers: UserAnswers, arn: Option[String] = None): CurrentUserRequestWithAnswers[_] = {
    val penaltyData = userAnswers.getAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData).getOrElse(penaltyDataLSP)
    CurrentUserRequestWithAnswers(userAnswers, penaltyData)(CurrentUserRequest(testMtdItId, arn)(FakeRequest()))
  }

  def agentUserRequestWithAnswers(userAnswers: UserAnswers): CurrentUserRequestWithAnswers[_] =
    userRequestWithAnswers(userAnswers, Some(testArn))

}
