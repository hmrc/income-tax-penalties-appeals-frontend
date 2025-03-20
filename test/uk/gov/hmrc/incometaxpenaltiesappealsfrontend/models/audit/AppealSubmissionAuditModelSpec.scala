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

import fixtures.BaseFixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.AppealSubmission
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission.BereavementAppealInformation
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, CurrentUserRequestWithAnswers, PenaltyTypeEnum, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{WhatCausedYouToMissDeadlinePage, WhoPlannedToSubmitPage}

import java.time.LocalDate

class AppealSubmissionAuditModelSpec extends AnyWordSpec with Matchers with BaseFixtures {

  "AppealSubmissionAuditModel" when {

    "auditing a Bereavement request" when {

      "penalty is an LSP (late appeal)" should {

        "serialise the Audit event correctly" in {

          implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForBereavementJourney

          AppealSubmissionAuditModel(
            penaltyNumber = "pen1234",
            penaltyType = PenaltyTypeEnum.Late_Submission,
            caseId = "case1234",
            correlationId = "correlation1234",
            appealSubmission =
              AppealSubmission(
                sourceSystem = "MDTP",
                taxRegime = "ITSA",
                customerReferenceNo = "123456789",
                dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                isLPP = false,
                appealSubmittedBy = "customer",
                agentDetails = None,
                appealInformation = BereavementAppealInformation(
                  reasonableExcuse = ReasonableExcuse.Bereavement,
                  honestyDeclaration = true,
                  startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                  statement = Some("Bereavement statement"),
                  lateAppeal = true,
                  lateAppealReason = Some("Bereavement late appeal reason"),
                  isClientResponsibleForSubmission = Some(true),
                  isClientResponsibleForLateSubmission = Some(true)
                )
              )
          ).detail shouldBe Json.obj(
            "submittedBy" -> "customer",
            "identifierType" -> "MTDITID",
            "taxIdentifier" -> "123456789",
            "penaltyNumber" -> "pen1234",
            "penaltyType" -> "Late Submission Penalty",
            "caseId" -> "case1234",
            "correlationId" -> "correlation1234",
            "appealInformation" -> Json.obj(
              "reasonForAppeal" -> "bereavement",
              "honestyDeclaration" -> true,
              "statementToExplainAppealReason" -> "Bereavement statement",
              "startDateOfEvent" -> "2023-01-01T00:00:00",
              "submittedAppealLate" -> true,
              "lateAppealReason" -> "Bereavement late appeal reason"
            )
          )
        }
      }

      "penalty is an LPP1 (not late)" should {

        "serialise the Audit event correctly" in {

          implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForBereavementJourney

          AppealSubmissionAuditModel(
            penaltyNumber = "pen1234",
            penaltyType = PenaltyTypeEnum.Late_Payment,
            caseId = "case1234",
            correlationId = "correlation1234",
            appealSubmission =
              AppealSubmission(
                sourceSystem = "MDTP",
                taxRegime = "ITSA",
                customerReferenceNo = "123456789",
                dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                isLPP = false,
                appealSubmittedBy = "customer",
                agentDetails = None,
                appealInformation = BereavementAppealInformation(
                  reasonableExcuse = ReasonableExcuse.Bereavement,
                  honestyDeclaration = true,
                  startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                  statement = Some("Bereavement statement"),
                  lateAppeal = false,
                  lateAppealReason = None,
                  isClientResponsibleForSubmission = Some(true),
                  isClientResponsibleForLateSubmission = Some(true)
                )
              )
          ).detail shouldBe Json.obj(
            "submittedBy" -> "customer",
            "identifierType" -> "MTDITID",
            "taxIdentifier" -> "123456789",
            "penaltyNumber" -> "pen1234",
            "penaltyType" -> "Late Payment Penalty 1",
            "caseId" -> "case1234",
            "correlationId" -> "correlation1234",
            "appealInformation" -> Json.obj(
              "reasonForAppeal" -> "bereavement",
              "honestyDeclaration" -> true,
              "statementToExplainAppealReason" -> "Bereavement statement",
              "startDateOfEvent" -> "2023-01-01T00:00:00",
              "submittedAppealLate" -> false
            )
          )
        }
      }

      "penalty is an LPP2 (agent - at fault)" should {

        "serialise the Audit event correctly" in {

          implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForBereavementJourney.copy(
            arn = Some("XARN1234567"),
            userAnswers = fakeRequestForBereavementJourney.userAnswers
              .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.agent)
              .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
          )(FakeRequest())

          AppealSubmissionAuditModel(
            penaltyNumber = "pen1234",
            penaltyType = PenaltyTypeEnum.Additional,
            caseId = "case1234",
            correlationId = "correlation1234",
            appealSubmission =
              AppealSubmission(
                sourceSystem = "MDTP",
                taxRegime = "ITSA",
                customerReferenceNo = "123456789",
                dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                isLPP = false,
                appealSubmittedBy = "customer",
                agentDetails = None,
                appealInformation = BereavementAppealInformation(
                  reasonableExcuse = ReasonableExcuse.Bereavement,
                  honestyDeclaration = true,
                  startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                  statement = Some("Bereavement statement"),
                  lateAppeal = false,
                  lateAppealReason = None,
                  isClientResponsibleForSubmission = Some(true),
                  isClientResponsibleForLateSubmission = Some(true)
                )
              )
          ).detail shouldBe Json.obj(
            "submittedBy" -> "agent",
            "identifierType" -> "MTDITID",
            "taxIdentifier" -> "123456789",
            "agentDetails" -> Json.obj(
              "agentReferenceNo" -> "XARN1234567",
              "isExcuseRelatedToAgent" -> true
            ),
            "penaltyNumber" -> "pen1234",
            "penaltyType" -> "Late Payment Penalty 2",
            "caseId" -> "case1234",
            "correlationId" -> "correlation1234",
            "appealInformation" -> Json.obj(
              "reasonForAppeal" -> "bereavement",
              "honestyDeclaration" -> true,
              "statementToExplainAppealReason" -> "Bereavement statement",
              "startDateOfEvent" -> "2023-01-01T00:00:00",
              "submittedAppealLate" -> false
            )
          )
        }
      }
    }
  }
}