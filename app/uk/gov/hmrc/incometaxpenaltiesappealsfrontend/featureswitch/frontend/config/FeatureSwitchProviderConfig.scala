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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.config

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.models.FeatureSwitchProvider

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchProviderConfig @Inject()(appConfig: AppConfig) {

  lazy val selfFeatureSwitchUrl = s"${appConfig.selfUrl}/appeal-penalty/self-assessment/test-only/api/feature-switches"

  lazy val incomeTaxPenaltiesFrontendFeatureSwitchUrl = s"${appConfig.incomeTaxPenaltiesFrontendBaseUrl}/penalties/income-tax/test-only/api/feature-switches"

  lazy val selfFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "income-tax-penalties-appeals-frontend",
    appName = "Income Tax Penalties Appeals Frontend",
    url = selfFeatureSwitchUrl
  )

  lazy val incomeTaxPenaltiesFrontendFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "income-tax-penalties-frontend",
    appName = "Income Tax Penalties Frontend",
    url = incomeTaxPenaltiesFrontendFeatureSwitchUrl
  )

  lazy val featureSwitchProviders: Seq[FeatureSwitchProvider] =
    Seq(selfFeatureSwitchProvider, incomeTaxPenaltiesFrontendFeatureSwitchProvider)

}
