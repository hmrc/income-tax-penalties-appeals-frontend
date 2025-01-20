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

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import play.api.mvc.Results.Ok
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.Page
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.{ExecutionContext, Future}

class BaseUserAnswersControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with LogCapturing {

  implicit lazy val ec: ExecutionContext = ExecutionContext.global
  lazy val errHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  lazy val testBaseController: BaseUserAnswersController = new BaseUserAnswersController {
    override val controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()
    override val errorHandler: ErrorHandler = errHandler
  }

  val testPage: Page[String] = new Page[String] { val key: String = "testPageKey" }

  val f: String => Future[Result] = { answer => Future.successful(Ok(answer)) }

  "BaseUserAnswersController" when {

    "calling .withAnswer()" when {

      "an answer exists" should {

        implicit val user: CurrentUserRequestWithAnswers[_] = CurrentUserRequestWithAnswers(
          mtdItId = "123456789",
          userAnswers = UserAnswers("1234").setAnswer(testPage, "foo")
        )(FakeRequest())

        "execute the function with that answer" in {

          val result = testBaseController.withAnswer(testPage)(f)

          status(result) shouldBe OK
          contentAsString(result) shouldBe "foo"
        }
      }

      "an answer does NOT exist" should {

        implicit val user: CurrentUserRequestWithAnswers[_] = CurrentUserRequestWithAnswers(
          mtdItId = "123456789",
          userAnswers = UserAnswers("1234")
        )(FakeRequest())

        //TODO: In future, redirect to SessionTimeout, or JourneyExpired page???
        "log an error and render an ISE" in {

          withCaptureOfLoggingFrom(logger) { logs =>
            val result = testBaseController.withAnswer(testPage)(f)

            status(result) shouldBe INTERNAL_SERVER_ERROR
            contentAsString(result) shouldBe await(errHandler.internalServerErrorTemplate).toString

            logs.exists(_.getMessage.contains(s"[BaseUserAnswersController][withAnswer] No answer found for pageKey: ${testPage.key}, mtditid: ${user.mtdItId}"))
          }
        }
      }
    }
  }
}
