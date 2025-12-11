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

class HealthAppealInformationSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite{

  "OtherAppealInformation.otherAppealInformationWrites" should {

    "serialize HealthAppealInformation to JSON correctly" in {

      """
        |case class HealthAppealInformation(
        |                                    reasonableExcuse: ReasonableExcuse,
        |                                    honestyDeclaration: Boolean,
        |                                    hospitalStayInvolved: Boolean,
        |                                    startDateOfEvent: Option[LocalDateTime],
        |                                    endDateOfEvent: Option[LocalDateTime],
        |                                    eventOngoing: Boolean,
        |                                    statement: Option[String],
        |                                    lateAppeal: Boolean,
        |                                    lateAppealReason: Option[String],
        |                                    isClientResponsibleForSubmission: Option[Boolean],
        |                                    isClientResponsibleForLateSubmission: Option[Boolean]
        |                                  ) extends AppealInformation
        |""".stripMargin

      val healthAppealInformation = HealthAppealInformation(
        reasonableExcuse = ReasonableExcuse.Health,
        honestyDeclaration = true,
        hospitalStayInvolved = true,
        startDateOfEvent = Some(LocalDateTime.of(2025,4,6,1,1,1)),
        endDateOfEvent = Some(LocalDateTime.of(2025,4,7,1,1,1)),
        eventOngoing = true,
        statement = Some("Health"),
        lateAppeal = true,
        lateAppealReason = Some("Was in Hospital"),
        isClientResponsibleForSubmission = Some(true),
        isClientResponsibleForLateSubmission = Some(false)
      )

      val expectedJson = Json.obj(
        "reasonableExcuse" -> "health",
        "honestyDeclaration" -> true,
        "hospitalStayInvolved" -> true,
        "startDateOfEvent" -> LocalDateTime.of(2025,4,6,1,1,1),
        "endDateOfEvent" -> LocalDateTime.of(2025,4,7,1,1,1),
        "eventOngoing" -> true,
        "lateAppeal" -> true,
        "statement" -> "Health",
        "lateAppealReason" -> "Was in Hospital",
        "isClientResponsibleForSubmission" -> true,
        "isClientResponsibleForLateSubmission" -> false
      )

      Json.toJson(healthAppealInformation)(HealthAppealInformation.healthAppealWrites) shouldBe expectedJson
    }
  }

}
