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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.audit

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{PenaltyData, PenaltyTypeEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.AppealSubmission

case class ViewStatusAuditModel(penaltyNumber: String,
                                penaltyType: PenaltyTypeEnum.Value,
                                appealSubmission: AppealSubmission,
                                penaltyData: PenaltyData)(implicit user: CurrentUserRequestWithAnswers[_]) extends AuditModel {

  override val auditType: String = "UserViewAppealStatus"
  override val detail: JsValue = user.auditJson ++ Json.obj(
    Seq[Option[(String, JsValueWrapper)]](
      Some("penaltyNumber" -> penaltyNumber),
      Some("penaltyType" -> Json.toJson(penaltyType)(PenaltyTypeEnum.auditWrites)),
      Some("appealDateTime" -> appealSubmission.dateOfAppeal),
      Some("isMultipleAppeal" -> penaltyData.multiplePenaltiesData.isDefined),
      Some("appealInformation" -> Json.toJson(appealSubmission)(AppealSubmission.auditWrites))
    ).flatten:_*
  )
}
