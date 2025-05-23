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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import play.api.i18n.Messages

import java.time.LocalDate

trait DateFormatter {

  def dateToString(date: LocalDate, withNBSP: Boolean = true)(implicit messages: Messages): String = {
    val dateString = s"${date.getDayOfMonth} ${messages(s"month.${date.getMonthValue}")} ${date.getYear}"
    if(withNBSP) htmlNonBroken(dateString) else dateString
  }

  def htmlNonBroken(string: String): String =
    string.replace(" ", "\u00A0")

}

object DateFormatter extends DateFormatter
