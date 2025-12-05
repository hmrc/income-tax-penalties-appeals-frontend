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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json, OFormat, Writes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CrimeReportedEnum, ReasonableExcuse}

import java.time.LocalDateTime

case class CrimeAppealInformation(
                                   reasonableExcuse: ReasonableExcuse,
                                   honestyDeclaration: Boolean,
                                   startDateOfEvent: LocalDateTime,
                                   reportedIssueToPolice: CrimeReportedEnum.Value,
                                   statement: Option[String],
                                   lateAppeal: Boolean,
                                   lateAppealReason: Option[String],
                                   isClientResponsibleForSubmission: Option[Boolean],
                                   isClientResponsibleForLateSubmission: Option[Boolean]
                                 ) extends AppealInformation

object CrimeAppealInformation {

  implicit val crimeAppealInformationFormatter: OFormat[CrimeAppealInformation] = Json.format[CrimeAppealInformation]

  val crimeAppealWrites: Writes[CrimeAppealInformation] = Writes { model =>
    Json.obj(Seq[Option[(String, JsValueWrapper)]](
      Some("reasonableExcuse" -> model.reasonableExcuse),
      Some("honestyDeclaration" -> model.honestyDeclaration),
      Some("startDateOfEvent" -> model.startDateOfEvent.toLocalDate),
      Some("reportedIssueToPolice" -> model.reportedIssueToPolice),
      Some("lateAppeal" -> model.lateAppeal),
      model.statement.map("statement" -> _),
      model.lateAppealReason.map("lateAppealReason" -> _),
      model.isClientResponsibleForSubmission.map("isClientResponsibleForSubmission" -> _),
      model.isClientResponsibleForLateSubmission.map("isClientResponsibleForLateSubmission" -> _)
    ).flatten: _*)
  }

  val auditWrites: Writes[CrimeAppealInformation] = Writes { model =>
    Json.toJson(model.asInstanceOf[AppealInformation])(AppealInformation.auditWrites).as[JsObject] ++ Json.obj(
      Seq[Option[(String, JsValueWrapper)]](
        Some("startDateOfEvent" -> model.startDateOfEvent.toLocalDate),
        Some("wasCrimeReported" -> (if(model.reportedIssueToPolice == CrimeReportedEnum.yes) true else false)),
        Some("submittedAppealLate" -> model.lateAppeal),
        model.lateAppealReason.map("lateAppealReason" -> _)
      ).flatten: _*
    )
  }
}
