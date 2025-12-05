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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.Evidence
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{UploadJourney, UploadStatusEnum}

import java.time.{LocalDate, LocalDateTime}

class OtherAppealInformationSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite{

  "OtherAppealInformation.otherAppealInformationWrites" should {

    "serialize OtherAppealInformation to JSON correctly" in {

      val otherAppealInformation = OtherAppealInformation(
        reasonableExcuse = ReasonableExcuse.FireOrFlood,
        honestyDeclaration = true,
        startDateOfEvent = LocalDateTime.of(2025,4,6,1,1,1),
        statement = Some("A fire destroyed the records"),
        supportingEvidence = Some(Evidence(1L)),
        lateAppeal = true,
        lateAppealReason = Some("Was not aware of the penalty"),
        isClientResponsibleForSubmission = Some(true),
        isClientResponsibleForLateSubmission = Some(false),
        uploadedFiles = Some(Seq(UploadJourney("file-ref-1", UploadStatusEnum.READY,lastUpdated = LocalDateTime.of(2025,4,7,1,1,1))))
      )

      val expectedJson = Json.obj(
        "reasonableExcuse" -> "fireandflood",
        "honestyDeclaration" -> true,
        "startDateOfEvent" -> LocalDate.of(2025,4,6),
        "lateAppeal" -> true,
        "statement" -> "A fire destroyed the records",
        "lateAppealReason" -> "Was not aware of the penalty",
        "supportingEvidence" -> Json.obj(
          "noOfUploadedFiles" -> 1
        ),
        "isClientResponsibleForSubmission" -> true,
        "isClientResponsibleForLateSubmission" -> false,
        "uploadedFiles" -> Json.arr(
          Json.obj(
            "reference" -> "file-ref-1",
            "fileStatus" -> "READY",
            "lastUpdated" -> "2025-04-07T01:01:01"
          )
        )
      )

      Json.toJson(otherAppealInformation)(OtherAppealInformation.otherAppealInformationWrites) shouldBe expectedJson
    }
  }
}
