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

import org.scalatest.Assertion
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.Ok
import play.api.mvc.{BodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.{AuthMocks, IncomeTaxSessionMocks}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthoriseAndRetrieveMtdAgent
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.AuthorisedAndEnrolledAgent

import scala.concurrent.Future

class AuthoriseAndRetrieveMDTAgentSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with IncomeTaxSessionMocks {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler = app.injector.instanceOf[ErrorHandler]
  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

  def blockWithAssertion(requestTestCase: AuthorisedAndEnrolledAgent[_] => Assertion
                        ): AuthorisedAndEnrolledAgent[_] => Future[Result] = testRequest => {
    requestTestCase(testRequest)
    Future.successful(Results.Ok("ALL GOOD"))
  }

  val block: AuthorisedAndEnrolledAgent[_] => Future[Result] = { _ => Future.successful(Ok("ALL GOOD")) }

  val testAction = new AuthoriseAndRetrieveMtdAgent(
    authConnector = mockAuthConnector,
    appConfig = appConfig,
    errorHandler,
    mcc = stubMessagesControllerComponents()
  )

  lazy val authorisedAndEnrolledAgent: AuthorisedAndEnrolledAgent[_] = AuthorisedAndEnrolledAgent(sessionData, Some("1"))(FakeRequest())


  ".apply()" when {
    s"a authenticated Agent user" that {
      "has a delegated MTDIT enrolment" should {
        "return 200 all good" in {
          mockAuthEnrolledAgent()
          val result = testAction.invokeBlock(authorisedAndEnrolledAgent, blockWithAssertion(
            res => {
              res shouldBe authorisedAndEnrolledAgent
            }
          ))
          status(result) shouldBe OK
          contentAsString(result) shouldBe "ALL GOOD"
        }
      }

      "does not have delegated MTDIT enrolment" should {
        "return an error" in {
          mockAgentWithoutDelegatedEnrolment()
          val result = testAction.invokeBlock(authorisedAndEnrolledAgent, block)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user has No Active Session" should {
      "do something" in {
        mockAuthenticatedNoActiveSession()

        val result = testAction.invokeBlock(authorisedAndEnrolledAgent, block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }

    "the user has an expired Session" should {
      "do something" in {
        mockAuthenticatedBearerTokenExpired()

        val result = testAction.invokeBlock(authorisedAndEnrolledAgent, block)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }


    "the user is Not Authenticated" should {
      "do something" in {
        mockAuthenticatedFailure()

        val result = testAction.invokeBlock(authorisedAndEnrolledAgent, block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }
  }
}
