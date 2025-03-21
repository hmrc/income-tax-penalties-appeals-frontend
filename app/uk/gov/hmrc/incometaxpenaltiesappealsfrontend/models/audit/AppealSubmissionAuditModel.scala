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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.AppealSubmission
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CurrentUserRequestWithAnswers, PenaltyTypeEnum}

case class AppealSubmissionAuditModel(penaltyNumber: String,
                                      penaltyType: PenaltyTypeEnum.Value,
                                      caseId: String,
                                      correlationId: String,
                                      appealSubmission: AppealSubmission)(implicit user: CurrentUserRequestWithAnswers[_]) extends AuditModel {

  override val auditType: String = "UserAppealInfo"
  override val detail: JsValue = user.auditJson ++ Json.obj(
    "penaltyNumber" -> penaltyNumber,
    "penaltyType" -> Json.toJson(penaltyType)(PenaltyTypeEnum.auditWrites),
    "caseId" -> caseId,
    "correlationId" -> correlationId,
    "appealInformation" -> Json.toJson(appealSubmission)(AppealSubmission.auditWrites)
  )
}
