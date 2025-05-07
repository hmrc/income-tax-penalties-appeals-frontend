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
import org.scalatest.Assertion
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.Ok
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.{IncomeTaxSessionMocks, MockMessageCountConnector}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.RetrieveClientData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedUserRequest}

import scala.concurrent.Future

class RetrieveClientDataSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite
  with IncomeTaxSessionMocks
  with MockMessageCountConnector
  with BaseFixtures {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  def blockWithAssertion(requestTestCase: AuthorisedAndEnrolledAgent[_] => Assertion
                        ): AuthorisedAndEnrolledAgent[_] => Future[Result] = testRequest => {
    requestTestCase(testRequest)
    Future.successful(Results.Ok("ALL GOOD"))
  }

  val block: AuthorisedAndEnrolledAgent[_] => Future[Result] = { _ => Future.successful(Ok("ALL GOOD")) }

  val testAction = new RetrieveClientData(
    sessionDataConnector = mockSessionDataConnector,
    errorHandler = errorHandler,
    mcc = stubMessagesControllerComponents(),
    appConfig = appConfig
  )

  lazy val authorisedUserRequest: AuthorisedUserRequest[_] = AuthorisedUserRequest(AffinityGroup.Agent, Some("1"))(FakeRequest())

  ".refine()" when {
    "the agent has client session data" should {
      "return the AuthorisedAndEnrolledAgent request" in {
        mockIncomeTaxSessionDataFound()
        val result = testAction.invokeBlock(authorisedUserRequest, blockWithAssertion(
          res => {
            res.mtdItId shouldBe testMtdItId
            res.nino shouldBe testNino
            res.arn shouldBe Some("1")
          }
        ))
        status(result) shouldBe OK
        contentAsString(result) shouldBe "ALL GOOD"
      }
    }

    "the agent has no client session data" should {
      "redirect to V&C enter client utr" in {
        mockIncomeTaxSessionDataNotFound()
        val result = testAction.invokeBlock(authorisedUserRequest, block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should include("view/agents/client-utr")
      }
    }

    "an unexpected response is return from income tax session data" should {
      "render the error page" in {
        mockIncomeTaxSessionDataInternalErrorRequest()
        val result = testAction.invokeBlock(authorisedUserRequest, block)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
