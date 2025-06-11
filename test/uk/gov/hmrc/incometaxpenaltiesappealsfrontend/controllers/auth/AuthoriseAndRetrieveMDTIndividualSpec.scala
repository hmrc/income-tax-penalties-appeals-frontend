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
import play.api.mvc.{AnyContent, BodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.{AuthMocks, IncomeTaxSessionMocks}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthoriseAndRetrieveMTDIndividual
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.AuthorisedAndEnrolledIndividual

import scala.concurrent.Future

class AuthoriseAndRetrieveMDTIndividualSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with IncomeTaxSessionMocks{

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler = app.injector.instanceOf[ErrorHandler]
  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

  def blockWithAssertion(requestTestCase: AuthorisedAndEnrolledIndividual[_] => Assertion
                        ): AuthorisedAndEnrolledIndividual[_] => Future[Result] = testRequest => {
    requestTestCase(testRequest)
    Future.successful(Results.Ok("ALL GOOD"))
  }

  val block: AuthorisedAndEnrolledIndividual[AnyContent] => Future[Result] = { _ => Future.successful(Ok("ALL GOOD")) }

  val testAction = new AuthoriseAndRetrieveMTDIndividual(
    authConnector = mockAuthConnector,
    bodyParsers,
    appConfig = appConfig,
    errorHandler,
    mcc = stubMessagesControllerComponents()
  )

  ".apply()" when {
    List(AffinityGroup.Individual, AffinityGroup.Organisation).foreach { affinityGroup =>
      s"a authenticated ${affinityGroup.toString} user" that {
          "has a nino and MTDITID enrolment" should {
            "return 200 all good" in {
              mockAuthenticatedMTDIndorOrg(affinityGroup)
              val result = testAction.invokeBlock(FakeRequest(), blockWithAssertion(
                res => {
                  res.mtdItId shouldBe "1234567"
                  res.nino shouldBe "AA123456A"
                  res.arn shouldBe None
                }
              ))
              status(result) shouldBe OK
              contentAsString(result) shouldBe "ALL GOOD"
            }
          }

          "has a nino but not a MTDITID enrolment" should {
            "return an error" in {
              mockAuthenticatedMTDIndorOrg(affinityGroup, hasEnrolment = false)
              val result = testAction.invokeBlock(FakeRequest(),block)
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }

          "has no nino" should {
            "return an error" in {
              mockAuthenticatedMTDIndorOrg(affinityGroup, hasNino = false)
              val result = testAction.invokeBlock(FakeRequest(),block)
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }
      }

    "the user is an agent" should {
      "redirect to landing page" in {
        mockAuthenticatedMTDIndorOrg(AffinityGroup.Agent)
        val result = testAction.invokeBlock(FakeRequest(),block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("/appeal-penalty/self-assessment/appeal-start")
      }
    }

    "the user is not enrolled to MTD" should {
      "handle the failure" in {
        mockAuthenticatedWithNoMTDEnrolment()
        val result = testAction.invokeBlock(FakeRequest(),block)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user has no affinity group" should {
      "redirect to gg sign in" in {
        mockAuthenticatedWithNoAffinityGroup()

        val result = testAction.invokeBlock(FakeRequest(),block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }

    "the user has No Active Session" should {
      "redirect to gg sign in" in {
        mockAuthenticatedNoActiveSession()

        val result = testAction.invokeBlock(FakeRequest(),block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }

    "the user has an expired Session" should {
      "do something" in {
        mockAuthenticatedBearerTokenExpired()

        val result = testAction.invokeBlock(FakeRequest(), block)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }


    "the user is Not Authenticated" should {
      "redirect to gg sign in" in {
        mockAuthenticatedFailure()

        val result = testAction.invokeBlock(FakeRequest(),block)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }
  }
}
