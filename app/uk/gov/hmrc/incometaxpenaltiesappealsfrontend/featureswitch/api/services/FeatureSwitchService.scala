/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.{FeatureSwitchRegistry, FeatureSwitching}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.{CheckboxFeatureSwitch, CheckboxFeatureSwitchSetting, FeatureSwitchSetting}

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchService @Inject()(featureSwitchRegistry: FeatureSwitchRegistry,
                                     val appConfig: AppConfig) extends FeatureSwitching {

  def getFeatureSwitches(): Seq[FeatureSwitchSetting] =
    featureSwitchRegistry.switches.map {
      case checkboxSwitch: CheckboxFeatureSwitch =>
        FeatureSwitchSetting(
          checkboxSwitch.configName,
          checkboxSwitch.displayName,
          isEnabled = true, //Not Used by checkbox switches
          Some(checkboxSwitch.checkboxValues.map(value =>
            CheckboxFeatureSwitchSetting(value, isEnabled(checkboxSwitch, value))
          )))
      case switch =>
        FeatureSwitchSetting(
          switch.configName,
          switch.displayName,
          isEnabled = isEnabled(switch)
        )
    }

  def updateFeatureSwitches(updatedFeatureSwitches: Seq[FeatureSwitchSetting]): Seq[FeatureSwitchSetting] = {
    updatedFeatureSwitches.foreach(
      featureSwitchSetting =>
        featureSwitchRegistry.get(featureSwitchSetting.configName).foreach {
          case featureSwitch: CheckboxFeatureSwitch =>
            setEnabledSwitches(featureSwitch, featureSwitchSetting.enabledCheckboxValues)
          case featureSwitch =>
            if (featureSwitchSetting.isEnabled) enable(featureSwitch) else disable(featureSwitch)
        }
    )

    getFeatureSwitches()
  }
}
