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
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.{Application, inject}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.helpers.DateTimeHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneOffset}

class InitialisationControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswers: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]

  val testDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

  lazy val mockDateTime: DateTimeHelper = new DateTimeHelper {
    override def dateTimeNow: LocalDateTime = testDateTime
  }

  override lazy val app: Application = appWithOverrides(
    inject.bind[DateTimeHelper].toInstance(mockDateTime)
  )

  "GET /initialise-appeal" should {

    "initialise the UserAnswers and redirect to the /appeal-start page, adding the journeyId to session" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/initialise-appeal?penaltyId=1")

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.AppealStartController.onPageLoad().url)
        SessionCookieCrumbler.getSessionMap(result).get(IncomeTaxSessionKeys.journeyId) shouldBe Some(testJourneyId)

        userAnswers.getUserAnswer(testJourneyId).futureValue shouldBe Some(UserAnswers(
          journeyId = testJourneyId,
          data = Json.obj(
            IncomeTaxSessionKeys.penaltyNumber -> "1"
          ),
          lastUpdated = testDateTime.toInstant(ZoneOffset.UTC)
        ))
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/initialise-appeal?penaltyId=1", isAgent = true)

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.AppealStartController.onPageLoad().url)
        SessionCookieCrumbler.getSessionMap(result).get(IncomeTaxSessionKeys.journeyId) shouldBe Some(testJourneyId)

        userAnswers.getUserAnswer(testJourneyId).futureValue shouldBe Some(UserAnswers(
          journeyId = testJourneyId,
          data = Json.obj(
            IncomeTaxSessionKeys.penaltyNumber -> "1"
          ),
          lastUpdated = testDateTime.toInstant(ZoneOffset.UTC)
        ))
      }
    }
  }
}
