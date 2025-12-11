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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse


import java.time.LocalDateTime

class TechnicalIssuesAppealInformationSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite{

  "TechnicalIssuesAppealInformation.technicalIssuesAppealWrites" should {

    "serialize TechnicalIssuesAppealInformation to JSON correctly" in {

      val technicalIssuesAppealInformation = TechnicalIssuesAppealInformation(
        reasonableExcuse = ReasonableExcuse.TechnicalIssues,
        honestyDeclaration = true,
        startDateOfEvent = LocalDateTime.of(2025,4,6,1,1,1),
        endDateOfEvent = LocalDateTime.of(2025,4,7,1,1,1),
        statement = Some("A fire destroyed the records"),
        lateAppeal = true,
        lateAppealReason = Some("Was not aware of the penalty"),
        isClientResponsibleForSubmission = Some(true),
        isClientResponsibleForLateSubmission = Some(false)
      )

      val expectedJson = Json.obj(
        "reasonableExcuse" -> "technicalIssue",
        "honestyDeclaration" -> true,
        "startDateOfEvent" -> LocalDateTime.of(2025,4,6,1,1,1),
        "endDateOfEvent" -> LocalDateTime.of(2025,4,7,1,1,1),
        "lateAppeal" -> true,
        "statement" -> "A fire destroyed the records",
        "lateAppealReason" -> "Was not aware of the penalty",
        "isClientResponsibleForSubmission" -> true,
        "isClientResponsibleForLateSubmission" -> false
      )

      Json.toJson(technicalIssuesAppealInformation)(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites) shouldBe expectedJson
    }
  }
}
