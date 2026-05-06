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
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.Aliases.{ServiceNavigation, ServiceNavigationItem, Text}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.MockMessageCountConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.NavBarRetrievalAction
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledIndividual, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.messageCount.MessageCount
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys

import scala.concurrent.{ExecutionContext, Future}

class NavBarRetrievalActionSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite
  with MockMessageCountConnector
  with BaseFixtures {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val testAction = new NavBarRetrievalAction(
    messageCountConnector = mockMessageCountConnector
  )(appConfig, ExecutionContext.global, messagesApi)

  def expectedPtaServiceNavigation(implicit messages: Messages): ServiceNavigation = {
    ServiceNavigation(
      navigation = Seq(
        ServiceNavigationItem(
          content = Text(messages("pta.navigation.messages")),
          href = appConfig.personalTaxAccountMessagesUrl
        ),
        ServiceNavigationItem(
          content = Text(messages("pta.navigation.checkProgress")),
          href = appConfig.personalTaxAccountCheckProgressUrl
        ),
        ServiceNavigationItem(
          content = Text(messages("pta.navigation.profileAndSettings")),
          href = appConfig.personalTaxAccountProfileUrl
        )
      ),
      navigationId = "pta-service-navigation"
    )
  }

  def expectedBtaServiceNavigation(implicit messages: Messages): ServiceNavigation = {
    ServiceNavigation(
      navigation = Seq(
        ServiceNavigationItem(
          content = Text(messages("bta.navigation.manageAccount")),
          href = appConfig.businessTaxAccountManageAccountUrl
        ),
        ServiceNavigationItem(
          content = Text(messages("bta.navigation.messages")),
          href = appConfig.businessTaxAccountMessagesUrl
        ),
        ServiceNavigationItem(
          content = Text(messages("bta.navigation.helpAndContact")),
          href = appConfig.businessTaxAccountHelpUrl
        )
      ),
      navigationId = "bta-service-navigation"
    )
  }

  ".refine()" when {

    "being called by an Agent user" should {
      "return the CurrentUserRequest without service navigation being added" in {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val agentRequest = AuthorisedAndEnrolledAgent(sessionData, Some("ARN1234"))

        val result = testAction.refine(agentRequest)
        await(result) shouldBe Right(agentRequest)
      }
    }

    "being called by an Individual user" when {

      "the origin is PTA" when {

        "return the CurrentUserRequest with PTA service navigation added" in {
          implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession((IncomeTaxSessionKeys.origin, "PTA"))
          implicit val messages: Messages = messagesApi.preferred(request)
          val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None, None)

          mockGetMessageCount()(Future.successful(Right(MessageCount(0))))

          val result = testAction.refine(userRequest)
          val refinedRequest = await(result)

          refinedRequest.isRight shouldBe true
          val enrichedRequest = refinedRequest.toOption.get.asInstanceOf[AuthorisedAndEnrolledIndividual[?]]
          enrichedRequest.serviceNavigationPartial shouldBe Some(expectedPtaServiceNavigation)
        }
      }

      "the origin is BTA" should {
        "return the CurrentUserRequest with BTA service navigation added" in {

          implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession((IncomeTaxSessionKeys.origin, "BTA"))
          implicit val messages: Messages = messagesApi.preferred(request)
          val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None, None)

          val result = testAction.refine(userRequest)
          val refinedRequest = await(result)

          refinedRequest.isRight shouldBe true
          val enrichedRequest = refinedRequest.toOption.get.asInstanceOf[AuthorisedAndEnrolledIndividual[?]]
          enrichedRequest.serviceNavigationPartial shouldBe Some(expectedBtaServiceNavigation)
        }
      }

      "there is no origin in session" should {
        "return the CurrentUserRequest without service navigation being added" in {

          implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None, None)

          val result = testAction.refine(userRequest)
          val refinedRequest = await(result)

          refinedRequest.isRight shouldBe true
          val enrichedRequest = refinedRequest.toOption.get.asInstanceOf[AuthorisedAndEnrolledIndividual[?]]
          enrichedRequest.serviceNavigationPartial shouldBe None
        }
      }

      "the origin is an unrecognised value" should {
        "return the CurrentUserRequest without service navigation being added" in {

          implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession((IncomeTaxSessionKeys.origin, "UNKNOWN"))
          val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None, None)

          val result = testAction.refine(userRequest)
          val refinedRequest = await(result)

          refinedRequest.isRight shouldBe true
          val enrichedRequest = refinedRequest.toOption.get.asInstanceOf[AuthorisedAndEnrolledIndividual[?]]
          enrichedRequest.serviceNavigationPartial shouldBe None
        }
      }
    }

    "the PTA service navigation" should {
      "contain the correct navigation items with correct links and text" in {

        implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession((IncomeTaxSessionKeys.origin, "PTA"))
        val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None, None)

        mockGetMessageCount()(Future.successful(Right(MessageCount(0))))

        val result = testAction.refine(userRequest)
        val refinedRequest = await(result)
        val enrichedRequest = refinedRequest.toOption.get.asInstanceOf[AuthorisedAndEnrolledIndividual[?]]
        val serviceNav = enrichedRequest.serviceNavigationPartial.get

        serviceNav.navigationId shouldBe "pta-service-navigation"
        serviceNav.navigation.length shouldBe 3

        serviceNav.navigation.head.content shouldBe Text("Messages")
        serviceNav.navigation.head.href shouldBe appConfig.personalTaxAccountMessagesUrl

        serviceNav.navigation(1).content shouldBe Text("Check progress")
        serviceNav.navigation(1).href shouldBe appConfig.personalTaxAccountCheckProgressUrl

        serviceNav.navigation(2).content shouldBe Text("Profile and settings")
        serviceNav.navigation(2).href shouldBe appConfig.personalTaxAccountProfileUrl

      }
    }

    "the BTA service navigation" should {
      "contain the correct navigation items with correct links and text" in {

        implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession((IncomeTaxSessionKeys.origin, "BTA"))
        val userRequest = AuthorisedAndEnrolledIndividual(testMtdItId, testNino, None, None)

        val result = testAction.refine(userRequest)
        val refinedRequest = await(result)
        val enrichedRequest = refinedRequest.toOption.get.asInstanceOf[AuthorisedAndEnrolledIndividual[?]]
        val serviceNav = enrichedRequest.serviceNavigationPartial.get

        serviceNav.navigationId shouldBe "bta-service-navigation"
        serviceNav.navigation.length shouldBe 3

        serviceNav.navigation.head.content shouldBe Text("Manage account")
        serviceNav.navigation.head.href shouldBe appConfig.businessTaxAccountManageAccountUrl

        serviceNav.navigation(1).content shouldBe Text("Messages")
        serviceNav.navigation(1).href shouldBe appConfig.businessTaxAccountMessagesUrl

        serviceNav.navigation(2).content shouldBe Text("Help and contact")
        serviceNav.navigation(2).href shouldBe appConfig.businessTaxAccountHelpUrl
      }
    }
  }
}