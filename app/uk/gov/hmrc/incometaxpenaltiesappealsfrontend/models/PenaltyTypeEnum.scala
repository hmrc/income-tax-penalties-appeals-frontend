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

import play.api.libs.json._

object PenaltyTypeEnum extends Enumeration {
  val Late_Submission: PenaltyTypeEnum.Value = Value
  val Late_Payment: PenaltyTypeEnum.Value = Value
  val Additional: PenaltyTypeEnum.Value = Value

  implicit val format: Format[PenaltyTypeEnum.Value] = new Format[PenaltyTypeEnum.Value] {
    override def writes(o: PenaltyTypeEnum.Value): JsValue = {
      JsString(o.toString.toUpperCase)
    }

    override def reads(json: JsValue): JsResult[PenaltyTypeEnum.Value] = {
      json.as[String] match {
        case "LATE_SUBMISSION" => JsSuccess(Late_Submission)
        case "LATE_PAYMENT" => JsSuccess(Late_Payment)
        case "ADDITIONAL" => JsSuccess(Additional)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  val auditWrites: Writes[PenaltyTypeEnum.Value] = Writes {
    case Late_Submission => JsString("Late Submission Penalty")
    case Late_Payment => JsString("Late Payment Penalty 1")
    case _ => JsString("Late Payment Penalty 2")
  }
}
