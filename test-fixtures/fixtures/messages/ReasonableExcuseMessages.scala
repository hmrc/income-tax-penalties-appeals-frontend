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

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._

object ReasonableExcuseMessages {

  sealed trait Messages { _: i18n =>
    val titleAndHeading = "What was the reason for missing the submission deadline?"
    val bereavement: String = "Bereavement (someone died)"
    val cessation: String = "Cessation of income source"
    val crime: String = "Crime"
    val fireOrFlood: String = "Fire or flood"
    val health: String = "Serious or life-threatening ill health"
    val technical: String = "Software or technology issues"
    val unexpectedHospital: String = "Unexpected hospital stay"
    val lossOfStaff: String = "TBC lossOfStaff"
    val other: String = "The reason does not fit into any of the other categories"
    val otherHint: String = "You should only choose this if the reason is not covered by any of the other options."
    val errorRequired: String = "Select the reason for missing the submission deadline"

    val cyaKey = "Reason for missing the submission deadline"
    def cyaValue(reasonableExcuse: ReasonableExcuse): String = reasonableExcuse match {
      case Bereavement => bereavement
      case Cessation => cessation
      case Crime => crime
      case FireOrFlood => fireOrFlood
      case Health => health
      case TechnicalIssues => technical
      case UnexpectedHospital => unexpectedHospital
      case LossOfStaff => lossOfStaff
      case Other => other
    }

    val cyaHidden = "reason for missing the submission deadline"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val titleAndHeading = "What was the reason for missing the submission deadline? (Welsh)"
    override val bereavement: String = "Profedigaeth (roedd rhywun wedi marw)"
    override val cessation: String = "Cessation of income source (Welsh)"
    override val crime: String = "Trosedd"
    override val fireOrFlood: String = "Tân neu lifogydd"
    override val health: String = "Salwch difrifol neu salwch a oedd yn berygl i fywyd"
    override val technical: String = "Problemau technoleg neu broblemau meddalwedd"
    override val unexpectedHospital: String = "Aros yn yr ysbyty yn annisgwyl"
    override val lossOfStaff: String = "TBC lossOfStaff (Welsh)"
    override val other: String = "Rheswm arall"
    override val otherHint: String = "You should only choose this if the reason is not covered by any of the other options. (Welsh)"
    override val errorRequired: String = "Dewiswch y rheswm dros fethu’r dyddiad cau ar gyfer cyflwyno"

    override val cyaKey = "Rheswm dros fethu’r dyddiad cau ar gyfer cyflwyno"
    override val cyaHidden = "Rheswm dros fethu’r dyddiad cau ar gyfer cyflwyno"
  }
}
