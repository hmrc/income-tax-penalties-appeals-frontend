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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.btaNavBar

import play.api.i18n.Messages
import play.api.libs.json.{Format, Json}

case class NavLink(en: String,
                   cy: String,
                   url: String,
                   alerts: Option[Int] = None) {
  def message(implicit messages: Messages): String = if (messages.lang.code.toUpperCase == "CY") cy else en
}

object NavLink {
  implicit val format: Format[NavLink] = Json.format[NavLink]
}
