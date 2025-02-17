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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.MultiplePenaltiesData

case class PenaltyData(penaltyNumber: String,
                       appealData: AppealData,
                       multiplePenaltiesData: Option[MultiplePenaltiesData]) {

  val isLPP: Boolean = Seq(PenaltyTypeEnum.Late_Payment, PenaltyTypeEnum.Additional).contains(appealData.`type`)
  val isAdditional: Boolean = appealData.`type` == PenaltyTypeEnum.Additional
}

object PenaltyData {
  implicit val format: Format[PenaltyData] = Json.format[PenaltyData]
}
