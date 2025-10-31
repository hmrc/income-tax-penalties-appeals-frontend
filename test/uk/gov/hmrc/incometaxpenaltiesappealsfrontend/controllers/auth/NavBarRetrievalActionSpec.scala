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
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.MessageCountHttpParser.MessagesCountResponseMalformed
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.MockMessageCountConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.NavBarRetrievalAction
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledIndividual, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.messageCount.MessageCount
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks.MockBtaNavBarService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.navBar.PtaNavBar

import scala.concurrent.{ExecutionContext, Future}

class NavBarRetrievalActionSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite
  with MockBtaNavBarService
  with MockMessageCountConnector
  with BaseFixtures {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  lazy val ptaNavBar: PtaNavBar = app.injector.instanceOf[PtaNavBar]

  val block: CurrentUserRequest[AnyContent] => Result = { _ => Ok("ALL GOOD") }

  val testAction = new NavBarRetrievalAction(
    messageCountConnector = mockMessageCountConnector,
    btaNavBarService = mockBtaNavBarService,
    ptaNavBar = ptaNavBar
  )(appConfig, ExecutionContext.global, messagesApi)

  ".refine()" when {
    "being called by an Agent user" should {
      "return the CurrentUserRequest without the NavBar being added" in {

        implicit val request =  FakeRequest()
        val agentRequest = AuthorisedAndEnrolledAgent(sessionData, Some("ARN1234"))

        val result = testAction.refine(agentRequest)
        await(result) shouldBe Right(agentRequest)
      }
    }

    "being called by an Individual user" when {
      "the origin is PTA" when {
        "a successful response is returned from the connector" should {
          "return the CurrentUserRequest with the NavBar being added" in {

            implicit lazy val request: FakeRequest[_] = FakeRequest().withSession(IncomeTaxSessionKeys.origin -> "PTA")
            implicit lazy val messages = messagesApi.preferred(request)
            val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

            mockGetMessageCount()(Future.successful(Right(MessageCount(1))))

            val result = testAction.refine(userRequest)
            await(result) shouldBe Right(userRequest.copy(navBar = Some(ptaNavBar(1))))
          }
        }

        "an error response is returned from the connector" should {
          "return the CurrentUserRequest with the NavBar being added, but the messages count defaulted to 0" in {

            implicit lazy val request: FakeRequest[_] = FakeRequest().withSession(IncomeTaxSessionKeys.origin -> "PTA")
            implicit lazy val messages = messagesApi.preferred(request)
            val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

            mockGetMessageCount()(Future.successful(Left(MessagesCountResponseMalformed)))

            val result = testAction.refine(userRequest)
            await(result) shouldBe Right(userRequest.copy(navBar = Some(ptaNavBar(0))))
          }
        }
      }

      "the origin is BTA" when {
        "a successful response is returned from the BtaNavBarService" should {
          "return the CurrentUserRequest with the NavBar being added" in {

            implicit lazy val request: FakeRequest[_] = FakeRequest().withSession(IncomeTaxSessionKeys.origin -> "BTA")
            val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

            mockRetrieveBtaLinksAndRenderNavBar()(Future.successful(Some(Html("BTA Nav Bar"))))

            val result = testAction.refine(userRequest)
            await(result) shouldBe Right(userRequest.copy(navBar = Some(Html("BTA Nav Bar"))))
          }
        }

        "a None is returned from BtaNavBarService" should {
          "return the CurrentUserRequest without a NavBar" in {

            implicit lazy val request: FakeRequest[_] = FakeRequest().withSession(IncomeTaxSessionKeys.origin -> "BTA")
            val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None)

            mockRetrieveBtaLinksAndRenderNavBar()(Future.successful(None))

            val result = testAction.refine(userRequest)
            await(result) shouldBe Right(userRequest.copy(navBar = None))
          }
        }
      }
    }
  }
  }
