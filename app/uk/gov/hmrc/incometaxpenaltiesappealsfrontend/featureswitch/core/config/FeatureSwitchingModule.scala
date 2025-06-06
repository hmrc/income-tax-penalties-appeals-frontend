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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.{CheckboxFeatureSwitch, FeatureSwitch}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse

import javax.inject.Singleton

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches: Seq[FeatureSwitch] = Seq(
    UseStubForBackend,
    ReasonableExcusesEnabled
  )

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[FeatureSwitchRegistry].to(this).eagerly()
    )
  }
}

case object UseStubForBackend extends FeatureSwitch {
  override val configName: String = "features.useStubForBackend"
  override val displayName: String = "Use stub instead of Penalties backend service"
}

case object ReasonableExcusesEnabled extends CheckboxFeatureSwitch {
  override val configName: String = "features.reasonableExcusesEnabled"
  override val displayName: String = "Enable/Disable Reasonable Excuses"
  override val checkboxValues: Seq[String] = ReasonableExcuse.allReasonableExcuses.map(_.toString)
}
