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
import play.api.http.Status.OK
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.{defaultAwaitTimeout, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.api.services.FeatureSwitchService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.FeatureSwitchSetting

class FeatureSwitchApiControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockitoSugar with Injecting {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val cc: ControllerComponents = stubControllerComponents()
  private val mockFeatureSwitchService = mock[FeatureSwitchService]

  val testAction: FeatureSwitchApiController = new FeatureSwitchApiController(
    mockFeatureSwitchService,
    appConfig
  ){
  override protected def controllerComponents: ControllerComponents = cc
}

  val testFeatureSwitches: Seq[FeatureSwitchSetting] = Seq(
    FeatureSwitchSetting("config1","feature1",isEnabled = true),
    FeatureSwitchSetting("config2","feature2",isEnabled = false)
  )

  val testUpdatedFeatureSwitches: Seq[FeatureSwitchSetting] = Seq(
    FeatureSwitchSetting("config1","feature1",isEnabled = false),
    FeatureSwitchSetting("config2","feature2",isEnabled = true)
  )

  "FeatureSwitchApiController" should {

    "Get Feature Switches using getFeatureSwitches " in {

      when(mockFeatureSwitchService.getFeatureSwitches()) thenReturn testFeatureSwitches
      val result = testAction.getFeatureSwitches(FakeRequest())
      status(result) shouldBe OK
    }

    "Post Feature Switches using updateFeatureSwitches " in {

      when(mockFeatureSwitchService.updateFeatureSwitches(any())) thenReturn testUpdatedFeatureSwitches
      val result = testAction.updateFeatureSwitches()(FakeRequest().withBody(testFeatureSwitches))
      status(result) shouldBe OK
    }
  }
}
