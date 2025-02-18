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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Crime, Other}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.MultiplePenaltiesData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AppealData, CrimeReportedEnum, CurrentUserRequest, CurrentUserRequestWithAnswers, PenaltyData, PenaltyTypeEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{CrimeReportedPage, HonestyDeclarationPage, ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys

import java.time.LocalDate

trait BaseFixtures {

  val testMtdItId = "123456789"
  val testArn = "00123456"
  val testJourneyId: String = "journeyId123"

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
    appealData = lateSubmissionAppealData,
    multiplePenaltiesData = None
  )

  val penaltyDataLPP: PenaltyData = PenaltyData(
    penaltyNumber = "123456790",
    appealData = latePaymentAppealData,
    multiplePenaltiesData = None
  )

  val emptyUserAnswers: UserAnswers = UserAnswers(testJourneyId)
  val emptyUerAnswersWithLSP: UserAnswers = emptyUserAnswers.setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)

  val fakeRequestForCrimeJourney: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      appealData = lateSubmissionAppealData,
      multiplePenaltiesData = None
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUerAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
        .setAnswer(ReasonableExcusePage, Crime)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyData
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "no"
      ))
  }

  val fakeRequestForCrimeJourneyMultiple: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      appealData = latePaymentAppealData,
      multiplePenaltiesData = Some(multiplePenaltiesModel)
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUerAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
        .setAnswer(ReasonableExcusePage, Crime)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyData
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "yes"
      ))
  }

  val fakeRequestForOtherJourney: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      appealData = lateSubmissionAppealData,
      multiplePenaltiesData = None
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUerAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(ReasonableExcusePage, Other)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyData
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.whyReturnSubmittedLate -> "This is a reason.",
        IncomeTaxSessionKeys.isUploadEvidence -> "yes",
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "no"
      ))
  }

  val fakeRequestForOtherJourneyDeclinedUploads: CurrentUserRequestWithAnswers[AnyContent] = {

    val penaltyData = PenaltyData(
      penaltyNumber = "123456789",
      appealData = lateSubmissionAppealData,
      multiplePenaltiesData = None
    )

    CurrentUserRequestWithAnswers(
      mtdItId = testMtdItId,
      userAnswers = emptyUerAnswersWithLSP
        .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
        .setAnswer(HonestyDeclarationPage, true)
        .setAnswer(ReasonableExcusePage, Other)
        .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
      penaltyData = penaltyData
    )(
      //TODO: These will all move to be UserAnswers as part of future stories
      FakeRequest().withSession(
        IncomeTaxSessionKeys.whyReturnSubmittedLate -> "This is a reason.",
        IncomeTaxSessionKeys.isUploadEvidence -> "no",
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "no"
      ))
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

  def userRequestWithAnswers(userAnswers: UserAnswers, penaltyData: PenaltyData = penaltyDataLSP): CurrentUserRequestWithAnswers[_] =
    CurrentUserRequestWithAnswers(userAnswers, penaltyData)(CurrentUserRequest(testMtdItId, None)(FakeRequest()))

  def agentUserRequestWithAnswers(userAnswers: UserAnswers, penaltyData: PenaltyData = penaltyDataLSP): CurrentUserRequestWithAnswers[_] =
    CurrentUserRequestWithAnswers(userAnswers, penaltyData)(CurrentUserRequest(testMtdItId, Some(testArn))(FakeRequest()))

}
