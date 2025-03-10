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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.{FeatureSwitching, StubIncomeTaxSessionData, UseStubForBackend}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, FiniteDuration}

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {

  val appConfig: AppConfig = this

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  lazy val ITSAPenaltiesAppealsHomeUrl = "/view-or-appeal-penalty/self-assessment/appeal-start"
  val alphaBannerUrl: String = servicesConfig.getString("alpha-banner-url")

  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)
  def getCheckboxFeatureSwitchValues(checkboxList: String): Seq[String] = config.get[Seq[String]](checkboxList)

  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-appeals-frontend")
  def incomeTaxPenaltiesFrontendBaseUrl: String = servicesConfig.baseUrl("income-tax-penalties-frontend")

  def penaltiesServiceBaseUrl: String =
    if (isEnabled(UseStubForBackend)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else s"${servicesConfig.baseUrl("penalties")}/penalties"

  def messagesFrontendBaseUrl: String =
    if (isEnabled(UseStubForBackend)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else servicesConfig.baseUrl("message-frontend")

  def upscanInitiateBaseUrl: String = servicesConfig.baseUrl("upscan-initiate")

  def btaBaseUrl: String = servicesConfig.baseUrl("business-tax-account")

  def incomeTaxSessionDataBaseUrl: String =
    if (isEnabled(StubIncomeTaxSessionData)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else servicesConfig.baseUrl("income-tax-session-data")

  val appName: String = servicesConfig.getString("appName")

  def appealLPPDataForPenaltyUrl(penaltyId: String, mtditid: String, isAdditional: Boolean): String =
    s"$penaltiesServiceBaseUrl/ITSA/appeals-data/late-payments/MTDITID/$mtditid?penaltyId=$penaltyId&isAdditional=$isAdditional"

  def appealLSPDataForPenaltyUrl(penaltyId: String, mtditid: String): String =
    s"$penaltiesServiceBaseUrl/ITSA/appeals-data/late-submissions/MTDITID/$mtditid?penaltyId=$penaltyId"

  def multiplePenaltyDataUrl(penaltyId: String, mtditid: String): String =
    s"$penaltiesServiceBaseUrl/ITSA/appeals-data/multiple-penalties/MTDITID/$mtditid?penaltyId=$penaltyId"

  def reasonableExcuseFetchUrl(mtditid: String): String =
    s"$penaltiesServiceBaseUrl/ITSA/appeals-data/reasonable-excuses/MTDITID/$mtditid"

  def submitAppealUrl(mtditid: String, isLPP: Boolean, penaltyNumber: String, correlationId: String, isMultiAppeal: Boolean): String =
    s"$penaltiesServiceBaseUrl/ITSA/appeals/submit-appeal/MTDITID/$mtditid?isLPP=$isLPP&penaltyNumber=$penaltyNumber&correlationId=$correlationId&isMultiAppeal=$isMultiAppeal"

  lazy val signInUrl: String = config.get[String]("signIn.url")
  lazy val signOutUrl: String = config.get[String]("signOut.url")

  lazy val surveyOrigin: String = servicesConfig.getString("exit-survey-origin")
  lazy val survey = s"""${servicesConfig.getString("feedback-frontend-host")}/feedback/$surveyOrigin"""

  lazy val penaltiesHomePage: String = config.get[String]("urls.incomeTaxPenaltiesHome")

  lazy val mongoTTL: Duration = config.get[Duration]("mongodb.ttl")

  lazy val numberOfCharsInTextArea: Int = config.get[Int]("constants.numberOfCharsInTextArea")
  lazy val lateDays: Int = config.get[Int]("constants.lateDays")
  lazy val bereavementLateDays: Int = config.get[Int]("constants.bereavementLateDays")

  lazy val host: String = config.get[String]("income-tax-penalties-appeals-frontend-host")

  lazy val upscanMinFileSize: Int = config.get[Int]("upscan.minFileSize")
  lazy val upscanMaxFileSize: Int = config.get[Int]("upscan.maxFileSize")
  lazy val upscanMaxFileSizeMB: Int = upscanMaxFileSize / 1024 / 1024
  lazy val upscanMaxNumberOfFiles: Int = config.get[Int]("upscan.maxNumberOfFiles")
  lazy val upscanAcceptedMimeTypes: String = config.get[String]("upscan.acceptedMimeTypes")
  lazy val upscanCheckInterval: FiniteDuration = config.get[FiniteDuration]("upscan.checkInterval")
  lazy val upscanTimeout: FiniteDuration = config.get[FiniteDuration]("upscan.timeout")

}
