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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth

import fixtures.BaseFixtures
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.UserAnswersAction
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledIndividual, CurrentUserRequestWithAnswers}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks.MockSessionService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys

import java.time.Instant
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class UserAnswersActionSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite
  with MockSessionService with BaseFixtures {

  implicit lazy val ec: ExecutionContextExecutor = ExecutionContext.global

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  val block: CurrentUserRequestWithAnswers[AnyContent] => Future[Result] = {
    user => Future(Ok(Json.toJson(user.userAnswers)))
  }

  val testAction: UserAnswersAction = new UserAnswersAction(
    sessionService = mockSessionService,
    errorHandler,
    appConfig
  )

  ".refine()" when {
    "a journey ID does NOT exist in the session" should {

      implicit lazy val request: Request[AnyContent] = FakeRequest()

      "redirect to the Income Tax penalties home page" in {

        val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

        val result = testAction.invokeBlock(userRequest, block)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes.PageNotFoundController.onPageLoad(isAgent = userRequest.isAgent).url)
      }
    }

    "a journey ID does exist in the session" when {

      val testJourneyId = "1234"
      implicit lazy val request: Request[AnyContent] =
        FakeRequest().withSession(IncomeTaxSessionKeys.journeyId -> testJourneyId)

      "no UserAnswers are returned from the UserAnswers service" should {

        "redirect to the Income Tax penalties home page" in {

          mockGetUserAnswers(testJourneyId)(Future.successful(None))

          val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

          val result = testAction.invokeBlock(userRequest, block)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes.PageNotFoundController.onPageLoad(isAgent = userRequest.isAgent).url)
        }
      }

      "UserAnswers are returned from the UserAnswers service" when {

        "the UserAnswers contains penalty appeal data" should {

          val testUserAnswers = UserAnswers(
            journeyId = testJourneyId,
            data = Json.obj(),
            lastUpdated = Instant.ofEpochMilli(1)
          ).setAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData, penaltyDataLSP)

          "execute the block adding the answers to the UserRequest" in {

            mockGetUserAnswers(testJourneyId)(Future.successful(Some(testUserAnswers)))

            val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

            val result = testAction.invokeBlock(userRequest, block)

            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.toJson(testUserAnswers)
          }
        }

        "the UserAnswers contains NO penalty appeal data" should {

          val testUserAnswers = UserAnswers(
            journeyId = testJourneyId,
            data = Json.obj(),
            lastUpdated = Instant.ofEpochMilli(1)
          )

          "redirect to the penalties frontend" in {

            mockGetUserAnswers(testJourneyId)(Future.successful(Some(testUserAnswers)))

            val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

            val result = testAction.invokeBlock(userRequest, block)

            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes.PageNotFoundController.onPageLoad(isAgent = userRequest.isAgent).url)
          }
        }
      }
    }

    "a mongo error occurs" should {

      "render the error page" in {
        implicit lazy val request: Request[AnyContent] =
          FakeRequest().withSession(IncomeTaxSessionKeys.journeyId -> testJourneyId)

        mockGetUserAnswersMongoFailure(testJourneyId)

        val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

        val result = testAction.invokeBlock(userRequest, block)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
