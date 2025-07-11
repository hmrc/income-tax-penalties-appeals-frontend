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

package fixtures.messages

import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.Language

sealed trait i18n {
  def lspCaption(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"Late submission penalty point: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }
  def lppCaption(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"First late payment penalty: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }

  def lppCaptionAppealStart(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"Late payment penalty: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }

  def lspCaptionMultiple(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"Late submission penalty points: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }

  def lppCaptionMultiple(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"Late payment penalties: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }


  val serviceName = "Manage your Self Assessment"
  def titleWithSuffix(title: String): String = title + s" - Manage your Self Assessment - GOV.UK"
  val continue = "Continue"
  val remove = "Remove"
  val change = "Change"
  val yes: String = "Yes"
  val no: String = "No"
  val errorPrefix: String = "Error: "
  val thereIsAProblem: String = "There is a problem"
  val day = "day"
  val month = "month"
  val year = "year"
  val or = "or"
  val lang: Language
}

trait En extends i18n {
  override val lang: Language = language.En
}
object English extends En

trait Cy extends i18n {
  override def lspCaption(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"Pwynt cosb am gyflwyno’n hwyr: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }
  override def lppCaption(from: String, to: String, removeNBSP: Boolean = true): String = {
    val message = s"Cosb am dalu’n hwyr: $from to $to"
    if (removeNBSP) message.replace("\u00A0", " ") else message
  }
  override val serviceName = "Manage your Self Assessment (Welsh)"
  override def titleWithSuffix(title: String): String = title + s" - Manage your Self Assessment - GOV.UK (Welsh)"
  override val continue = "Yn eich blaen"
  override val remove = "Tynnu"
  override val change = "Newid"
  override val yes: String = "Iawn"
  override val no: String = "Na"
  override val errorPrefix: String = "Gwall:"
  override val thereIsAProblem: String = "Mae problem wedi codi"
  override val day = "diwrnod"
  override val month = "mis"
  override val year = "blwyddyn"
  override val or = "neu"
  override val lang: Language = language.Cy
}
object Welsh extends Cy
