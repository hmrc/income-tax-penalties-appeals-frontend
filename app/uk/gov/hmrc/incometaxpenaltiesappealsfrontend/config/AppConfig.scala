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
import play.api.Configuration
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {

  val appConfig: AppConfig = this

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  lazy val serviceSignOut:String = servicesConfig.getString("signOut.url")
  lazy val ITSAPenaltiesAppealsHomeUrl = "/view-or-appeal-penalty/self-assessment/appeal-start"
  val alphaBannerUrl: String = servicesConfig.getString("alpha-banner-url")

  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)
  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-appeals-frontend")
  def incomeTaxPenaltiesFrontendBaseUrl: String = servicesConfig.baseUrl("income-tax-penalties-frontend")

  def penaltiesServiceBaseUrl: String =
    if (isEnabled(UseStubForBackend)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else s"${servicesConfig.baseUrl("penalties")}/penalties"

  def messagesFrontendBaseUrl: String =
    if (isEnabled(UseStubForBackend)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else servicesConfig.baseUrl("message-frontend")

  def btaBaseUrl: String = servicesConfig.baseUrl("business-tax-account")

  val appName: String = servicesConfig.getString("appName")

  def appealLPPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String, isAdditional: Boolean): String = {
    s"$penaltiesServiceBaseUrl/appeals-data/late-payments?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey&isAdditional=$isAdditional"
  }

  def appealLSPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String): String = {
    s"$penaltiesServiceBaseUrl/appeals-data/late-submissions?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
  }

  def multiplePenaltyDataUrl(penaltyId: String, enrolmentKey: String): String =
    s"$penaltiesServiceBaseUrl/appeals-data/multiple-penalties?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"

  def reasonableExcuseFetchUrl: String = s"$penaltiesServiceBaseUrl/appeals-data/reasonable-excuses"


  def submitAppealUrl(enrolmentKey: String, isLPP: Boolean, penaltyNumber: String, correlationId: String, isMultiAppeal: Boolean): String =
    s"$penaltiesServiceBaseUrl/appeals/submit-appeal?enrolmentKey=$enrolmentKey&isLPP=$isLPP&penaltyNumber=$penaltyNumber&correlationId=$correlationId&isMultiAppeal=$isMultiAppeal"

  lazy val daysRequiredForLateAppeal: Int = config.get[Int]("constants.daysRequiredForLateAppeal")

  lazy val signInUrl: String = config.get[String]("signIn.url")
  lazy val signOutUrl: String = config.get[String]("signOut.url")

  lazy val surveyOrigin: String = servicesConfig.getString("exit-survey-origin")
  lazy val survey = s"""${servicesConfig.getString("feedback-frontend-host")}/feedback/$surveyOrigin"""


}
