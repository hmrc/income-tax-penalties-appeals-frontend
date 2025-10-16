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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services

import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.api.services.FeatureSwitchService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitchRegistry
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.{CheckboxFeatureSwitch, CheckboxFeatureSwitchSetting, FeatureSwitch, FeatureSwitchSetting}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse

import scala.concurrent.ExecutionContext

class FeatureSwitchServiceSpec extends AnyWordSpec with Matchers with MockFactory with GuiceOneAppPerSuite {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val featureSwichRegistry: FeatureSwitchRegistry = mock[FeatureSwitchRegistry]
  implicit val ec: ExecutionContext = ExecutionContext.global

  val testService = new FeatureSwitchService(
    featureSwichRegistry,
    appConfig
  )

  case object ReasonableExcusesEnabled extends CheckboxFeatureSwitch {
    override val configName: String = "features.reasonableExcusesEnabled"
    override val displayName: String = "Enable/Disable Reasonable Excuses"
    override val checkboxValues: Seq[String] = ReasonableExcuse.allReasonableExcuses.map(_.toString)
  }

  val reasonableExcuseFSSettings: FeatureSwitchSetting = {
    val enabledCheckboxFeatureSwitches = appConfig.getCheckboxFeatureSwitchValues("features.reasonableExcusesEnabled")
    FeatureSwitchSetting(
      "features.reasonableExcusesEnabled",
      "Enable/Disable Reasonable Excuses",
      true,
      Some(ReasonableExcusesEnabled.checkboxValues.map(value => CheckboxFeatureSwitchSetting(value, enabledCheckboxFeatureSwitches.contains(value))))
    )
  }

  case object UseStubForBackend extends FeatureSwitch {
    override val configName: String = "features.useStubForBackend"
    override val displayName: String = "Use stub instead of Penalties backend service"
  }

  val useStubForBackendFSSettings: FeatureSwitchSetting = FeatureSwitchSetting(
    "features.useStubForBackend",
    "Use stub instead of Penalties backend service",
    appConfig.getFeatureSwitchValue("features.useStubForBackend")
  )

  "calling getFeatureSwitches" when {
    "there is no feature switches" should {
      "return an empty sequence" in {
        (featureSwichRegistry.switches _).expects().returning(Seq.empty[FeatureSwitch])

        testService.getFeatureSwitches() shouldBe Seq.empty[FeatureSwitchSetting]
      }
    }

    "there is only a checkboxSwitch" should {
      "return a sequence with checkbox feature switch settings" in {
        (featureSwichRegistry.switches _).expects().returning(Seq(ReasonableExcusesEnabled))

        testService.getFeatureSwitches() shouldBe Seq(reasonableExcuseFSSettings)
      }
    }

    "there is only a UseStubForBackend" should {
      "return a sequence with UseStubForBackend feature switch settings" in {
        (featureSwichRegistry.switches _).expects().returning(Seq(UseStubForBackend))

        testService.getFeatureSwitches() shouldBe Seq(useStubForBackendFSSettings)
      }
    }

    "all feature switches present" should {
      "return feature switch settings for all feature switches" in {
        (featureSwichRegistry.switches _).expects().returning(Seq(UseStubForBackend, ReasonableExcusesEnabled))

        testService.getFeatureSwitches() shouldBe Seq(useStubForBackendFSSettings, reasonableExcuseFSSettings)
      }
    }
  }

  "calling updateFeatureSwitches" when {
    "there is only a checkboxSwitch" should {
      "update and return a sequence with checkbox feature switch settings" in {
        val featureSwitchSettings = Seq(reasonableExcuseFSSettings)
        (featureSwichRegistry.get(_: String)).expects(ReasonableExcusesEnabled.configName).returning(Some(ReasonableExcusesEnabled))
        (featureSwichRegistry.switches _).expects().returning(Seq(ReasonableExcusesEnabled))
        testService.updateFeatureSwitches(featureSwitchSettings) shouldBe featureSwitchSettings
      }
    }

    "there is only a UseStubForBackend" should {
      "return a sequence with UseStubForBackend feature switch settings" in {
        val featureSwitchSettings = Seq(useStubForBackendFSSettings)
        (featureSwichRegistry.get(_: String)).expects(UseStubForBackend.configName).returning(Some(UseStubForBackend))
        (featureSwichRegistry.switches _).expects().returning(Seq(UseStubForBackend))
        testService.updateFeatureSwitches(featureSwitchSettings) shouldBe featureSwitchSettings
      }
    }

    "all feature switches present" should {
      "return feature switch settings for all feature switches" in {
        val featureSwitchSettings = Seq(useStubForBackendFSSettings, reasonableExcuseFSSettings)
        (featureSwichRegistry.get(_: String)).expects(UseStubForBackend.configName).returning(Some(UseStubForBackend))
        (featureSwichRegistry.get(_: String)).expects(ReasonableExcusesEnabled.configName).returning(Some(ReasonableExcusesEnabled))

        (featureSwichRegistry.switches _).expects().returning(Seq(UseStubForBackend, ReasonableExcusesEnabled))

        testService.updateFeatureSwitches(featureSwitchSettings) shouldBe featureSwitchSettings
      }
    }
  }
}
