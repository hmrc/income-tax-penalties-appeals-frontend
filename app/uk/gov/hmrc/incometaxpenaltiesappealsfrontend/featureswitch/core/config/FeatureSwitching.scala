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

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.{CheckboxFeatureSwitch, FeatureSwitch}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait FeatureSwitching {

  val appConfig: AppConfig

  val FEATURE_SWITCH_ON = "true"
  val FEATURE_SWITCH_OFF = "false"
  val TIME_MACHINE_NOW = "TIME_MACHINE_NOW"

  def isEnabled(featureSwitch: FeatureSwitch): Boolean =
    sys.props get featureSwitch.configName match {
      case Some(value) => value == FEATURE_SWITCH_ON
      case None => appConfig.getFeatureSwitchValue(featureSwitch.configName)
    }

  def enable(featureSwitch: FeatureSwitch): Unit = {
    logger.warn(s"[enable] $featureSwitch")
    sys.props += featureSwitch.configName -> FEATURE_SWITCH_ON
  }

  def disable(featureSwitch: FeatureSwitch): Unit = {
    logger.warn(s"[disable] $featureSwitch")
    sys.props += featureSwitch.configName -> FEATURE_SWITCH_OFF
  }

  //Checkbox List Based Features
  def isEnabled(checkboxFeatureSwitch: CheckboxFeatureSwitch, value: String): Boolean =
    sys.props get checkboxFeatureSwitch.configName match {
      case Some(values) => values.split(",").contains(value)
      case None => appConfig.getCheckboxFeatureSwitchValues(checkboxFeatureSwitch.configName).contains(value)
    }

  def setEnabledSwitches(checkboxFeatureSwitch: CheckboxFeatureSwitch, values: Seq[String]): Unit = {
    logger.warn(s"[enable] $checkboxFeatureSwitch with values: \n - ${values.mkString("\n - ")}")
    sys.props += checkboxFeatureSwitch.configName -> values.mkString(",")
  }

  val timeMachineDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")  
  
  def setFeatureDate(dateToSet: Option[LocalDate]): Unit = {
    logger.debug(s"[FeatureSwitching][setFeatureDate] - Setting time machine date to: $dateToSet")
    dateToSet.fold(sys.props -= TIME_MACHINE_NOW)(sys.props += TIME_MACHINE_NOW -> _.format(timeMachineDateFormatter))
  }

  def getFeatureDate(implicit appConfig: AppConfig): LocalDate = {
    sys.props.get(TIME_MACHINE_NOW).fold({
      val optDateAsString = appConfig.config.getOptional[String]("feature.switch.time-machine-now")
      val dateAsString = optDateAsString.getOrElse("")
      if(dateAsString.isEmpty){
        LocalDate.now()
      }else{
        LocalDate.parse(dateAsString)
      }
    })(LocalDate.parse(_))
  }

}
