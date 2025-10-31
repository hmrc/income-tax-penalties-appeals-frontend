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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.api.services

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitchRegistry
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.{CheckboxFeatureSwitch, CheckboxFeatureSwitchSetting, FeatureSwitch, FeatureSwitchSetting}

class FeatureSwitchServiceSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with Injecting {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  private val mockFeatureSwitchRegistry = mock[FeatureSwitchRegistry]

  val testSwitch: FeatureSwitch = mock[FeatureSwitch]
  val testCheckboxSwitch: CheckboxFeatureSwitch = mock[CheckboxFeatureSwitch]

  val testAction = new FeatureSwitchService(mockFeatureSwitchRegistry,appConfig)

  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .configure(
        "testSwitch" -> false,
        "testCheckboxSwitch" -> Seq("option1", "option2")
      )
      .build()
  }

  "FeatureSwitchService " should {

    "getFeatureSwitches" in {

      (() => testSwitch.configName).expects().returning("testSwitch").atLeastOnce()
      (() => testCheckboxSwitch.configName).expects().returning("testCheckboxSwitch").atLeastOnce()
      (() => testSwitch.displayName).expects().returning("Test Switch").atLeastOnce()
      (() => testCheckboxSwitch.displayName).expects().returning("Test Checkbox Switch").atLeastOnce()
      (() => mockFeatureSwitchRegistry.switches).expects().returning(Seq(testSwitch, testCheckboxSwitch)).atLeastOnce()
      (() => testCheckboxSwitch.checkboxValues).expects().returning(Seq("option1", "option2")).atLeastOnce()

      val expected = List(
        FeatureSwitchSetting("testSwitch",
          "Test Switch",
          isEnabled = false,
          None
        ),
        FeatureSwitchSetting("testCheckboxSwitch",
          "Test Checkbox Switch",
          isEnabled = true,
          Some(List(CheckboxFeatureSwitchSetting("option1", enabled = true), CheckboxFeatureSwitchSetting("option2", enabled = true))
          )
        )
      )

      val result = testAction.getFeatureSwitches()
      result shouldBe expected
    }

    "updateFeatureSwitches" in {

      (() => testSwitch.configName).expects().returning("testSwitch").atLeastOnce()
      (() => testCheckboxSwitch.configName).expects().returning("testCheckboxSwitch").atLeastOnce()
      (() => testSwitch.displayName).expects().returning("Test Switch").atLeastOnce()
      (() => testCheckboxSwitch.displayName).expects().returning("Test Checkbox Switch").atLeastOnce()
      (() => testCheckboxSwitch.checkboxValues).expects().returning(Seq("option1", "option2")).atLeastOnce()
      (() => mockFeatureSwitchRegistry.switches).expects().returning(Seq(testSwitch, testCheckboxSwitch)).atLeastOnce()
      (mockFeatureSwitchRegistry.get(_: String)).expects("testSwitch").returning(Some(testSwitch)).atLeastOnce()
      (mockFeatureSwitchRegistry.get(_: String)).expects("testCheckboxSwitch").returning(Some(testCheckboxSwitch)).atLeastOnce()

      val updateSwitch = List(
        FeatureSwitchSetting("testSwitch",
          "Test Switch",
          isEnabled = true,
          None
        ),
        FeatureSwitchSetting("testCheckboxSwitch",
          "Test Checkbox Switch",
          isEnabled = true,
          Some(List(CheckboxFeatureSwitchSetting("option1", enabled = false), CheckboxFeatureSwitchSetting("option2", enabled = false))
          )
        )
      )

      val result = testAction.updateFeatureSwitches(updateSwitch)

      result shouldBe updateSwitch

    }

  }

}
