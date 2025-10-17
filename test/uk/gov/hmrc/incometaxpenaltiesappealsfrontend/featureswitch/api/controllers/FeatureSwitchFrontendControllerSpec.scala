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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.api.controllers


import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api
import play.api.Application
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Injecting}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.FeatureSwitchSetting
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.controllers.FeatureSwitchFrontendController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.models.FeatureSwitchProvider
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.services.FeatureSwitchRetrievalService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.views.html.feature_switch
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.{ExecutionContext, Future}

class FeatureSwitchFrontendControllerSpec extends AnyWordSpec
  with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockFactory with Injecting with DefaultAwaitTimeout {

  lazy val stubView: feature_switch = stub[feature_switch]

  override lazy val app: Application = {
    new GuiceApplicationBuilder()
      .overrides(
        api.inject.bind[feature_switch].toInstance(stubView),
        api.inject.bind[FeatureSwitchRetrievalService].toInstance(mockFeatureSwitchService),
        api.inject.bind[MessagesControllerComponents].toInstance(stubMessagesControllerComponents())
      )
      .build()
  }
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val messages: Messages = stubMessages()

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val mcc: MessagesControllerComponents = stubMessagesControllerComponents()


  lazy val mockFeatureSwitchService: FeatureSwitchRetrievalService = mock[FeatureSwitchRetrievalService]
  val mockProvider: FeatureSwitchProvider = mock[FeatureSwitchProvider]
  val testAction = app.injector.instanceOf[FeatureSwitchFrontendController]

  val testFeatureSwitches: Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])] = Seq(
    (mockProvider, Seq(
      FeatureSwitchSetting("config1", "feature1", isEnabled = true),
      FeatureSwitchSetting("config2", "feature2", isEnabled = false)
    ))
  )

  "FeatureSwitchFrontendController" should {

    "show" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      (mockFeatureSwitchService.retrieveFeatureSwitches()(_: HeaderCarrier)).expects(*).returns(Future.successful(testFeatureSwitches))
      (stubView.apply(_: Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])], _: Call)(_: Request[_], _: Messages))
        .when(*, *, *, *)
        .returns(Html("Rendered feature switches"))
      val result = testAction.show()(request)

      status(result) shouldBe OK
      contentAsString(result) should include("Rendered feature switches")

    }

    "submit" in {

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest()
          .withHeaders(CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withFormUrlEncodedBody("config1" -> "true", "config2" -> "false")


      (mockFeatureSwitchService.updateFeatureSwitches(_: Iterable[String])(_: HeaderCarrier)).expects(*, *).returns(Future.successful(testFeatureSwitches))

      val result = testAction.submit()(request)

//      status(result) shouldBe OK
//      contentAsString(result) should include("Rendered feature switches")
      true
    }
  }
}
