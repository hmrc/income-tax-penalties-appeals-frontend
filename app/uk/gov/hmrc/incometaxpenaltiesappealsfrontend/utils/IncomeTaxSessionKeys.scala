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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

object IncomeTaxSessionKeys {

  //Global keys that are used across MTD programme
  val origin = "Origin"
  val agentSessionMtditid = "ClientMTDID"

  //Local keys that are only used for ITSAPR.
  val pocAchievementDate = "ITSAPR_pocAchievementDate"
  val journeyId = "ITSAPR_journeyId"
  val penaltyData = "ITSAPR_penaltyData"

  //TODO: Remove all these when their answer has been moved into UserAnswers
  val wasHospitalStayRequired = "wasHospitalStayRequired"
  val hasHealthEventEnded = "hasHealthEventEnded"
}
