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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models

import play.api.libs.json.{Json, OFormat}

case class CheckboxFeatureSwitchSetting(value: String,
                                        enabled: Boolean)

object CheckboxFeatureSwitchSetting {
  implicit val format: OFormat[CheckboxFeatureSwitchSetting] = Json.format[CheckboxFeatureSwitchSetting]
}

case class FeatureSwitchSetting(configName: String,
                                displayName: String,
                                isEnabled: Boolean,
                                checkboxValues: Option[Seq[CheckboxFeatureSwitchSetting]] = None) {
  val isCheckBoxFeatureSwitch: Boolean = checkboxValues.nonEmpty
  val enabledCheckboxValues: Seq[String] = checkboxValues.map(_.filter(_.enabled).map(_.value)).getOrElse(Seq.empty)
}

object FeatureSwitchSetting {

  implicit val format: OFormat[FeatureSwitchSetting] = Json.format[FeatureSwitchSetting]

}
