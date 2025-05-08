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

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, BodyParsers, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.{AuthMocks, IncomeTaxSessionMocks}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthoriseAndRetrieve

class AuthoriseAndRetrieveSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with IncomeTaxSessionMocks{

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler = app.injector.instanceOf[ErrorHandler]
  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

  val block: Request[AnyContent] => Result = { _ => Ok("ALL GOOD") }

  val testAction = new AuthoriseAndRetrieve(
    authConnector = mockAuthConnector,
    bodyParsers,
    mockSessionDataConnector,
    appConfig = appConfig,
    errorHandler,
    mcc = stubMessagesControllerComponents()
  )

  ".apply()" when {
    List(AffinityGroup.Individual, AffinityGroup.Organisation, AffinityGroup.Agent).foreach { affinityGroup =>
      s"a authenticated ${affinityGroup.toString} user" that {
        if (affinityGroup != AffinityGroup.Agent) {
          "has a nino and MTDITID enrolment" should {
            "return 200 all good" in {
              mockAuthenticated(affinityGroup)
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe OK
              contentAsString(result) shouldBe "ALL GOOD"
            }
          }

          "has a nino but is MTDITID enrolment" should {
            "return an error" in {
              mockAuthenticated(affinityGroup, hasEnrolment = false)
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }

          "has no nino" should {
            "return an error" in {
              mockAuthenticated(affinityGroup, hasNino = false)
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        } else {
          "has a HMRC-AS_AGENT enrolment, client session data and delegated MTDIT enrolment" should {
            "return 200 all good" in {
              mockAuthenticated(affinityGroup, hasNino = false)
              mockIncomeTaxSessionDataFound()
              mockAuthEnrolledAgent()
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe OK
              contentAsString(result) shouldBe "ALL GOOD"
            }
          }

          "has a HMRC-AS_AGENT enrolment, client session data but does not have delegated MTDIT enrolment" should {
            "return an error" in {
              mockAuthenticated(affinityGroup, hasNino = false)
              mockIncomeTaxSessionDataFound()
              mockAgentWithoutDelegatedEnrolment()
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }

          "has a HMRC-AS_AGENT enrolment but does not have client session data" should {
            "redirect to V&C enter client utr" in {
              mockAuthenticated(affinityGroup, hasNino = false)
              mockIncomeTaxSessionDataNotFound()
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe SEE_OTHER
              redirectLocation(result).get should include("/view/agents/client-utr")
            }
          }

          "has a HMRC-AS_AGENT enrolment but getting client session data fails" should {
            "render the error page" in {
              mockAuthenticated(affinityGroup, hasNino = false)
              mockIncomeTaxSessionDataBadRequest()
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }

          "does not have a HMRC-AS_AGENT enrolment" should {
            "return an error" in {
              mockAgentWithoutAgentEnrolment()
              val result = testAction(block)(FakeRequest())
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }
      }
    }

    "the user has no affinity group" should {
      "do something" in {
        mockAuthenticatedWithNoAffinityGroup()

        val result = testAction(block)(FakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }

    "the user has No Active Session" should {
      "do something" in {
        mockAuthenticatedNoActiveSession()

        val result = testAction(block)(FakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }

    "the user has an expired Session" should {
      "do something" in {
        mockAuthenticatedBearerTokenExpired()

        val result = testAction(block)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }


    "the user is Not Authenticated" should {
      "do something" in {
        mockAuthenticatedFailure()

        val result = testAction(block)(FakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
      }
    }
  }
}
