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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.FeatureSwitchSetting
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.config.FeatureSwitchProviderConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.connectors.FeatureSwitchApiConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.models.FeatureSwitchProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class FeatureSwitchRetrievalService @Inject()(featureSwitchConfig: FeatureSwitchProviderConfig,
                                              featureSwitchApiConnector: FeatureSwitchApiConnector)
                                             (implicit ec: ExecutionContext) {

  def retrieveFeatureSwitches()(implicit hc: HeaderCarrier): Future[Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])]] = {

    val featureSwitchSeq: Seq[(FeatureSwitchProvider, Future[Seq[FeatureSwitchSetting]])] =
      featureSwitchConfig.featureSwitchProviders.map {
        featureSwitchProvider =>
          featureSwitchProvider -> featureSwitchApiConnector.retrieveFeatureSwitches(featureSwitchProvider.url)
      }

    Future.traverse(featureSwitchSeq) {
      case (featureSwitchProvider, futureSeqFeatureSwitchSetting) =>
        futureSeqFeatureSwitchSetting.map {
          featureSwitchSettingSeq => featureSwitchProvider -> featureSwitchSettingSeq
        }
    }
  }

  val featureSwitchKeyRegex: Regex = "(.+?)\\.(.+)".r
  val checkboxFeatureSwitchKeyRegex: Regex = "(.+?)\\.(.+)\\.(.+)".r

  def updateFeatureSwitches(updatedFeatureSwitchKeys: Iterable[String]
                           )(implicit hc: HeaderCarrier): Future[Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])]] = {
    val updatedFeatureSwitches: Future[Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])]] =
      retrieveFeatureSwitches().map {
        currentFeatureSwitches =>
          currentFeatureSwitches.map {
            case (featureSwitchProvider, providerFeatureSwitches) =>
              featureSwitchProvider -> providerFeatureSwitches.map {
                currentFeatureSwitch =>
                  if(currentFeatureSwitch.isCheckBoxFeatureSwitch) {
                    currentFeatureSwitch.copy(
                      checkboxValues = Some(currentFeatureSwitch.checkboxValues.get.map {
                        checkboxFeatureSwitchSetting =>
                          val isEnabled = updatedFeatureSwitchKeys.exists {
                            case checkboxFeatureSwitchKeyRegex(microserviceKey, featureSwitchKey, checkboxValue) =>
                              microserviceKey == featureSwitchProvider.id && featureSwitchKey == currentFeatureSwitch.configName && checkboxValue == checkboxFeatureSwitchSetting.value
                            case _ =>
                              false
                          }
                          checkboxFeatureSwitchSetting.copy(enabled = isEnabled)
                      })
                    )
                  } else {
                    val isEnabled = updatedFeatureSwitchKeys.exists {
                      case featureSwitchKeyRegex(microserviceKey, featureSwitchKey) =>
                        microserviceKey == featureSwitchProvider.id && featureSwitchKey == currentFeatureSwitch.configName
                      case _ =>
                        false
                    }
                    currentFeatureSwitch.copy(isEnabled = isEnabled)
                  }
              }
          }
      }

    updatedFeatureSwitches.flatMap {
      Future.traverse(_) {
        case (featureSwitchProvider, featureSwitchSettings) =>
          featureSwitchApiConnector.updateFeatureSwitches(featureSwitchProvider.url, featureSwitchSettings).map {
            updatedFeatureSwitches => featureSwitchProvider -> updatedFeatureSwitches
          }
      }
    }
  }

}
