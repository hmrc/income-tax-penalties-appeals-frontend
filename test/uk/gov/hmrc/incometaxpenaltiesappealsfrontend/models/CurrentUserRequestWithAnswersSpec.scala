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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import fixtures.BaseFixtures
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{CrimeReportedPage, HonestyDeclarationPage, ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, TimeMachine}

import java.time.LocalDate

class CurrentUserRequestWithAnswersSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val mockTimeMachine: TimeMachine = mock[TimeMachine]

  "isAppealLate" should {

    class Setup(date: LocalDate) {
      when(mockTimeMachine.getCurrentDate).thenReturn(date)
    }

    val fakeRequestForAppealingBothPenalties: (LocalDate, LocalDate) => CurrentUserRequestWithAnswers[AnyContent] = (lpp1Date: LocalDate, lpp2Date: LocalDate) => {

      val penaltyData = PenaltyData(
        penaltyNumber = "123456789",
        appealData = latePaymentAppealData,
        multiplePenaltiesData = Some(multiplePenaltiesModel.copy(
          firstPenaltyCommunicationDate = lpp1Date,
          secondPenaltyCommunicationDate = lpp2Date
        ))
      )

      CurrentUserRequestWithAnswers(
        mtdItId = testMtdItId,
        userAnswers = emptyUerAnswersWithLSP
          .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
          .setAnswer(HonestyDeclarationPage, true)
          .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
          .setAnswer(ReasonableExcusePage, "crime")
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
        penaltyData = penaltyData
      )(FakeRequest().withSession(
        //TODO: This will be moved to be a UserAnswer when the page is built
        IncomeTaxSessionKeys.doYouWantToAppealBothPenalties -> "yes")
      )
    }

    val fakeRequestForAppealingSinglePenalty: LocalDate => CurrentUserRequestWithAnswers[AnyContent] = (date: LocalDate) => {

      val penaltyData = PenaltyData(
        penaltyNumber = "123456789",
        appealData = latePaymentAppealData.copy(dateCommunicationSent = date),
        multiplePenaltiesData = None
      )

      CurrentUserRequestWithAnswers(
        mtdItId = testMtdItId,
        userAnswers = emptyUerAnswersWithLSP
          .setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyData)
          .setAnswer(HonestyDeclarationPage, true)
          .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
          .setAnswer(ReasonableExcusePage, "crime")
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2022, 1, 1)),
        penaltyData = penaltyData
      )(FakeRequest())
    }

    "return true" when {
      "communication date of penalty > 30 days ago" in new Setup(LocalDate.of(2022, 1, 1)) {
        fakeRequestForAppealingSinglePenalty(LocalDate.of(2021, 12, 1)).isAppealLate() shouldBe true
      }

      "appealing both penalties and LPP1 is late" in new Setup(LocalDate.of(2022, 1, 1)) {
        fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 1)).isAppealLate() shouldBe true
      }

      "appealing both penalties and both are late" in new Setup(LocalDate.of(2022, 4, 1)) {
        fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 1)).isAppealLate() shouldBe true
      }
    }

    "return false" when {
      "communication date of penalty < 30 days ago" in new Setup(LocalDate.of(2022, 1, 1)) {
        fakeRequestForAppealingSinglePenalty(LocalDate.of(2021, 12, 31)).isAppealLate() shouldBe false
      }

      "appealing both penalties and LPP1 and LPP2 are not late" in new Setup(LocalDate.of(2022, 1, 1)) {
        fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 31), LocalDate.of(2021, 12, 31)).isAppealLate() shouldBe false
      }
    }
  }
}
