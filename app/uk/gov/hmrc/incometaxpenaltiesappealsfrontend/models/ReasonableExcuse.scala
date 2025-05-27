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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import play.api.i18n.Messages
import play.api.libs.json.{JsString, Reads, Writes}
import play.api.mvc.JavascriptLiteral
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, RadioItem, Text}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.ReasonableExcusesEnabled

sealed trait ReasonableExcuse {
  def isEnabled()(implicit appConfig: AppConfig): Boolean =
    appConfig.isEnabled(ReasonableExcusesEnabled, toString)
}

class WithName(string: String) {
  override val toString: String = string
}

object ReasonableExcuse {
  case object Bereavement        extends WithName("bereavement")          with ReasonableExcuse
  case object Cessation          extends WithName("cessation")            with ReasonableExcuse
  case object Crime              extends WithName("crime")                with ReasonableExcuse
  case object FireOrFlood        extends WithName("fireandflood")         with ReasonableExcuse
  case object Health             extends WithName("health")               with ReasonableExcuse
  case object TechnicalIssues    extends WithName("technicalIssue")       with ReasonableExcuse
  case object UnexpectedHospital extends WithName("unexpectedHospital")   with ReasonableExcuse
  case object LossOfStaff        extends WithName("lossOfEssentialStaff") with ReasonableExcuse
  case object Other              extends WithName("other")                with ReasonableExcuse

  val allReasonableExcuses: Seq[ReasonableExcuse] = {
    Seq(
      Bereavement,
      Cessation,
      Crime,
      FireOrFlood,
      Health,
      TechnicalIssues,
      UnexpectedHospital,
      LossOfStaff,
      Other
    )
  }

  def radioOptions()(implicit messages: Messages, appConfig: AppConfig): Seq[RadioItem] =
    allReasonableExcuses.filter(_.isEnabled()).filterNot(_ == Other).map { reason =>
      RadioItem(
        id = Some(reason.toString),
        content = Text(messages(s"reasonableExcuses.$reason")),
        value = Some(reason.toString)
      )
    } ++ {
      if(Other.isEnabled()) {
        Seq(
          RadioItem(
            id = Some(Other.toString),
            content = Text(messages("reasonableExcuses.other")),
            value = Some(Other.toString)
          )
        )
      } else {
        Seq.empty
      }
    }

  def apply(name: String): ReasonableExcuse = name match {
    case "bereavement"          => Bereavement
    case "cessation"            => Cessation
    case "crime"                => Crime
    case "fireandflood"         => FireOrFlood
    case "health"               => Health
    case "technicalIssue"       => TechnicalIssues
    case "unexpectedHospital"   => UnexpectedHospital
    case "lossOfEssentialStaff" => LossOfStaff
    case "other"                => Other
    case _ => throw new IllegalArgumentException(s"Invalid ReasonableExcuse: $name")
  }

  implicit def writes: Writes[ReasonableExcuse] = Writes { model =>
    JsString(model.toString)
  }

  implicit val reads: Reads[ReasonableExcuse] = Reads { json =>
    json.validate[String].map(ReasonableExcuse.apply)
  }

  implicit val jsLiteral: JavascriptLiteral[ReasonableExcuse] = new JavascriptLiteral[ReasonableExcuse] {
    override def to(reasonableExcuse: ReasonableExcuse): String = {reasonableExcuse.toString}
    
  }
}
