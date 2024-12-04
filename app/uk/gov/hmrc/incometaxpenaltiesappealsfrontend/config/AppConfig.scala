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
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, RedirectUrl, RedirectUrlPolicy, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrlPolicy.Id

import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig) {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)
  lazy val serviceSignOut:String = servicesConfig.getString("service-signout.url")
  lazy val ITSAPenaltiesAppealsHomeUrl = "/penalties-appeals/income-tax/appeal-start"
  val alphaBannerUrl: String = servicesConfig.getString("alpha-banner-url")
  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)
  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-appeals-frontend")

  lazy val payApiUrl: String = servicesConfig.baseUrl("pay-api")
  lazy val penaltiesServiceBaseUrl: String = servicesConfig.baseUrl("penalties")
  val appName: String = servicesConfig.getString("appName")

  def appealLPPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String, isAdditional: Boolean): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-payments?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey&isAdditional=$isAdditional"
  }

  def appealLSPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-submissions?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
  }

  def multiplePenaltyDataUrl(penaltyId: String, enrolmentKey: String): String =
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/multiple-penalties?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"

  lazy val reasonableExcuseFetchUrl: String = penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.fetchUrl")

  lazy val essttpBackendUrl: String = servicesConfig.baseUrl("essttp-backend")

  def submitAppealUrl(enrolmentKey: String, isLPP: Boolean, penaltyNumber: String, correlationId: String, isMultiAppeal: Boolean): String =
    penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.submitUrl") + s"?enrolmentKey=$enrolmentKey&isLPP=$isLPP&penaltyNumber=$penaltyNumber&correlationId=$correlationId&isMultiAppeal=$isMultiAppeal"

  lazy val upscanInitiateBaseUrl: String = servicesConfig.baseUrl("upscan-initiate")

  lazy val mongoTTL: Duration = config.get[Duration]("mongodb.ttl")

  lazy val daysRequiredForLateAppeal: Int = config.get[Int]("constants.daysRequiredForLateAppeal")

  lazy val signInUrl: String = config.get[String]("signIn.url")

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"

  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"

  private lazy val allowedHostname = config.get[String]("urls.allowedHostname")

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  private lazy val platformHost = servicesConfig.getString("income-tax-penalties-appeals-frontend-host")
  private lazy val permitAllRedirectPolicy = config.get[Boolean]("urls.permitAllRedirectPolicy")

  private lazy val absoluteRedirectPolicy: RedirectUrlPolicy[Id] = if(!permitAllRedirectPolicy) AbsoluteWithHostnameFromAllowlist(allowedHostname) else UnsafePermitAll

  private lazy val agentClientLookupRedirectUrl: String => String = uri => RedirectUrl(platformHost + uri).get(absoluteRedirectPolicy).encodedUrl


  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

}
