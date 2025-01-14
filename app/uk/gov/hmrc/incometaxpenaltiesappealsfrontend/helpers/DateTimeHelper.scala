/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.helpers

import play.api.Configuration
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching

import java.time.LocalDateTime
import javax.inject.Inject

class DateTimeHelper @Inject()(implicit val appConfig: AppConfig) extends FeatureSwitching {
  implicit val config: Configuration = appConfig.config

  def dateTimeNow: LocalDateTime = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
}
