/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)
  lazy val serviceSignOut:String = servicesConfig.getString("service-signout.url")
  lazy val ITSAPenaltiesAppealsHomeUrl = "/penalties-appeals/income-tax"
  val alphaBannerUrl: String = servicesConfig.getString("alpha-banner-url")
  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)
  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-appeals-frontend")

}
