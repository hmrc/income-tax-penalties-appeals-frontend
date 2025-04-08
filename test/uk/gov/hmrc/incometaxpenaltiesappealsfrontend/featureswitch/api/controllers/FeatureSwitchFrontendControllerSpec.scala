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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, MessagesControllerComponents, Request}
import play.api.test.Helpers.{CONTENT_TYPE, contentAsString, defaultAwaitTimeout, status, stubMessages}
import play.api.test.{FakeRequest, Injecting}
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

class FeatureSwitchFrontendControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockitoSugar with Injecting {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val messages: Messages = stubMessages()

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val mockFeatureSwitchService: FeatureSwitchRetrievalService = mock[FeatureSwitchRetrievalService]
  val mockFeatureSwitchView: feature_switch = mock[feature_switch]
  val mcc: MessagesControllerComponents = stubMessagesControllerComponents()
  val mockProvider: FeatureSwitchProvider = mock[FeatureSwitchProvider]

  val testAction = new FeatureSwitchFrontendController(mockFeatureSwitchService,mockFeatureSwitchView,mcc)(ec, appConfig)

  val testFeatureSwitches: Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])] = Seq(
    (mockProvider, Seq(
      FeatureSwitchSetting("config1", "feature1", isEnabled = true),
      FeatureSwitchSetting("config2", "feature2", isEnabled = false)
    ))
  )

  "FeatureSwitchFrontendController" should {

    "show" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      when(mockFeatureSwitchService.retrieveFeatureSwitches()(any[HeaderCarrier])) thenReturn Future.successful(testFeatureSwitches)

      when(mockFeatureSwitchView.apply(
          any[Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])]],
          any[play.api.mvc.Call]
        )(any[Request[_]], any[Messages])
      ) thenReturn Html("Rendered feature switches")

      val result = testAction.show()(request)

      status(result) shouldBe OK
      contentAsString(result) should include ("Rendered feature switches")

    }

    "submit" in {

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest()
          .withHeaders(CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withFormUrlEncodedBody("config1" -> "true", "config2" -> "false")

      when(mockFeatureSwitchService.updateFeatureSwitches(any[Iterable[String]])(any[HeaderCarrier])) thenReturn Future.successful(testFeatureSwitches)

      when(
        mockFeatureSwitchView.apply(
          any[Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])]],
          any[play.api.mvc.Call]
        )(any[Request[_]], any[Messages])
      ) thenReturn Html("Rendered feature switches")

      val result = testAction.submit()(request)

      status(result) shouldBe OK
      contentAsString(result) should include("Rendered feature switches")
    }
  }
}
