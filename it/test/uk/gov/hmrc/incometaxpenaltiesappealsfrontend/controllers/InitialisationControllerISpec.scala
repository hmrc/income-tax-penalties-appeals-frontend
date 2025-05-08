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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.libs.json.Json
import play.api.{Application, inject}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.UseStubForBackend
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyTypeEnum
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.PenaltiesStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, ZoneOffset}

class InitialisationControllerISpec extends ControllerISpecHelper
  with PenaltiesStub {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswers: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]

  val testDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

  lazy val timeMachine: TimeMachine = new TimeMachine {
    override def getCurrentDateTime: LocalDateTime = testDateTime
  }

  override lazy val app: Application = appWithOverrides(
    inject.bind[TimeMachine].toInstance(timeMachine)
  )

  override def beforeEach(): Unit = {
    disable(UseStubForBackend)
    super.beforeEach()
  }

  "GET /initialise-appeal" when {
    "penalty appeal data is successfully returned from penalties BE" when {
      "initialise the UserAnswers and redirect to the /appeal-start page, adding the journeyId to session" when {
        "the user is an authorised individual (is2ndStageAppeal==false)" in {

          stubAuthRequests(false)
          successfulGetAppealDataResponse(penaltyDataLSP.penaltyNumber, testMtdItId)

          val result = get(s"/initialise-appeal?penaltyId=${penaltyDataLSP.penaltyNumber}&isLPP=false&isAdditional=false&is2ndStageAppeal=false")

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.AppealStartController.onPageLoad().url)
          SessionCookieCrumbler.getSessionMap(result).get(IncomeTaxSessionKeys.journeyId) shouldBe Some(testJourneyId)

          userAnswers.getUserAnswer(testJourneyId).futureValue shouldBe Some(UserAnswers(
            journeyId = testJourneyId,
            data = Json.obj(
              IncomeTaxSessionKeys.penaltyData -> Json.obj(
                "penaltyNumber" -> penaltyDataLSP.penaltyNumber,
                "is2ndStageAppeal" -> false,
                "appealData" -> Json.obj(
                  "type" -> PenaltyTypeEnum.Late_Submission,
                  "startDate" -> LocalDate.of(2020, 1, 1),
                  "endDate" -> LocalDate.of(2020, 1, 31),
                  "dueDate" -> LocalDate.of(2020, 3, 7),
                  "dateCommunicationSent" -> LocalDate.of(2020, 3, 8)
                )
              )
            ),
            lastUpdated = testDateTime.toInstant(ZoneOffset.UTC)
          ))
        }

        "the user is an authorised agent (and LPP with multiple) (is2ndStageAppeal==true)" in {

          stubAuthRequests(true)
          successfulGetAppealDataResponse(penaltyDataLPP.penaltyNumber, testMtdItId, isLPP = true)
          successfulGetMultiplePenalties(penaltyDataLPP.penaltyNumber, testMtdItId)

          val result = get(s"/initialise-appeal?penaltyId=${penaltyDataLPP.penaltyNumber}&isLPP=true&isAdditional=false&is2ndStageAppeal=true", isAgent = true)

          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.AppealStartController.onPageLoad().url)
          SessionCookieCrumbler.getSessionMap(result).get(IncomeTaxSessionKeys.journeyId) shouldBe Some(testJourneyId)

          userAnswers.getUserAnswer(testJourneyId).futureValue shouldBe Some(UserAnswers(
            journeyId = testJourneyId,
            data = Json.obj(
              IncomeTaxSessionKeys.penaltyData -> Json.obj(
                "penaltyNumber" -> penaltyDataLPP.penaltyNumber,
                "is2ndStageAppeal" -> true,
                "appealData" -> Json.obj(
                  "type" -> PenaltyTypeEnum.Late_Payment,
                  "startDate" -> LocalDate.of(2020, 1, 1),
                  "endDate" -> LocalDate.of(2020, 1, 31),
                  "dueDate" -> LocalDate.of(2020, 3, 7),
                  "dateCommunicationSent" -> LocalDate.of(2020, 3, 8)
                ),
                "multiplePenaltiesData" -> Json.obj(
                  "firstPenaltyChargeReference" -> "123456789",
                  "firstPenaltyAmount" -> 101.01,
                  "secondPenaltyChargeReference" -> "123456790",
                  "secondPenaltyAmount" -> 1.02,
                  "firstPenaltyCommunicationDate" -> "2023-04-06",
                  "secondPenaltyCommunicationDate" -> "2023-04-07"
                )
              )
            ),
            lastUpdated = testDateTime.toInstant(ZoneOffset.UTC)
          ))
        }
      }
    }

    "penalty appeal data fails to be returned from penalties BE" should {
      "render an ISE" in {

        stubAuthRequests(false)
        failedGetAppealDataResponse(penaltyDataLSP.penaltyNumber, testMtdItId)

        val result = get(s"/initialise-appeal?penaltyId=${penaltyDataLSP.penaltyNumber}&isLPP=false&isAdditional=false")

        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
