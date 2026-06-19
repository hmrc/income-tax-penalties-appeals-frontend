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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.mappings

import play.api.data.Forms.of
import play.api.data.{FieldMapping, Forms}
import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import java.time.LocalDate

trait Mappings extends Formatters {

  protected def text(message: String = "error.required"): FieldMapping[String] =
    Forms.of(stringFormatter(message))

  protected def localDate(
                           invalidKey: String,
                           allRequiredKey: String,
                           twoRequiredKey: String,
                           requiredKey: String,
                           futureKey: Option[String] = None,
                           dateNotEqualOrAfterKeyAndCompareDate: Option[(String, LocalDate)] = None,
                           args: Seq[String] = Seq.empty)(implicit messages: Messages, appConfig: AppConfig, timeMachine: TimeMachine): FieldMapping[LocalDate] =
    Forms.of(new LocalDateFormatter(timeMachine)(invalidKey, allRequiredKey, twoRequiredKey,  requiredKey, futureKey, dateNotEqualOrAfterKeyAndCompareDate, args))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))
}
