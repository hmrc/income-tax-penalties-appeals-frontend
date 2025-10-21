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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyTypeEnum
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.PenaltiesStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.*

import java.time.{LocalDate, ZoneOffset}

class InitialisationControllerISpec extends ControllerISpecHelper
  with PenaltiesStub {

  lazy val userAnswers: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]
  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  List(true, false).foreach { isAgent =>

    s"GET /initialise-appeal (isAgent = $isAgent)" when {
      "penalty appeal data is successfully returned from penalties BE" when {
        "initialise the UserAnswers and redirect to the /appeal-start page, adding the journeyId to session" when {
          "the user is authorised (is2ndStageAppeal==false)" in {

            stubAuthRequests(isAgent)
            successfulGetAppealDataResponse(penaltyDataLSP.penaltyNumber, testNino)

            val result = get(s"/initialise-appeal?penaltyId=${penaltyDataLSP.penaltyNumber}&isAgent=$isAgent&isLPP=false&isAdditional=false&is2ndStageAppeal=false")

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.AppealStartController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = false).url)
            SessionCookieCrumbler.getSessionMap(result).get(IncomeTaxSessionKeys.journeyId) shouldBe Some(testJourneyId)

            userAnswers.getUserAnswer(testJourneyId).futureValue
              .map(_.copy(lastUpdated = testDate.atStartOfDay().toInstant(ZoneOffset.UTC))) shouldBe 
              Some(UserAnswers(
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
              lastUpdated = testDate.atStartOfDay().toInstant(ZoneOffset.UTC)
            ))
          }
        }
      }

      "penalty appeal data fails to be returned from penalties BE" should {
        "render an ISE" in {

          stubAuthRequests(isAgent)
          failedGetAppealDataResponse(penaltyDataLSP.penaltyNumber, testNino)

          val result = get(s"/initialise-appeal?penaltyId=${penaltyDataLSP.penaltyNumber}&isAgent=$isAgent&isLPP=false&isAdditional=false")

          result.status shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
