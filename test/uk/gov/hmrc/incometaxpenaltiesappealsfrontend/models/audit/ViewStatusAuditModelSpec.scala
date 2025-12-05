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

import fixtures.FileUploadFixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.submission._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.{AppealSubmission, Evidence}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{WhatCausedYouToMissDeadlinePage, WhoPlannedToSubmitPage}

import java.time.LocalDate

class ViewStatusAuditModelSpec extends AnyWordSpec with Matchers with FileUploadFixtures {

  "ViewStatusAuditModel" when {

    "auditing a Bereavement request" when {

      "penalty is an LSP (late appeal)" should {

        "serialise the Audit event correctly" in {

          implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForBereavementJourney

          ViewStatusAuditModel(
            penaltyNumber = "pen1234",
            penaltyType = PenaltyTypeEnum.Late_Submission,
            penaltyData = penaltyDataLSP,
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
                ),
                appealLevel = "01"
              )
          ).detail shouldBe Json.obj(
            "submittedBy" -> "customer",
            "identifierType" -> "NINO",
            "taxIdentifier" -> "AA123456A",
            "penaltyNumber" -> "pen1234",
            "penaltyType" -> "Late Submission Penalty",
            "appealDateTime" -> "2023-01-05T00:00:00",
            "isMultipleAppeal" -> false,
            "appealInformation" -> Json.obj(
              "reasonForAppeal" -> "bereavement",
              "honestyDeclaration" -> true,
              "statementToExplainAppealReason" -> "Bereavement statement",
              "startDateOfEvent" -> "2023-01-01",
              "submittedAppealLate" -> true,
              "lateAppealReason" -> "Bereavement late appeal reason"
            )
          )
        }
      }

      "penalty is an LPP1 (not late - submission failed in PEGA/ETMP)" should {

        "serialise the Audit event correctly" in {

          implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForBereavementJourney

          ViewStatusAuditModel(
            penaltyNumber = "pen1234",
            penaltyType = PenaltyTypeEnum.Late_Payment,
            penaltyData = penaltyDataLPP,
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
                ),
                appealLevel = "01"
              )
          ).detail shouldBe Json.obj(
            "submittedBy" -> "customer",
            "identifierType" -> "NINO",
            "taxIdentifier" -> "AA123456A",
            "penaltyNumber" -> "pen1234",
            "penaltyType" -> "Late Payment Penalty 1",
            "appealDateTime" -> "2023-01-05T00:00:00",
            "isMultipleAppeal" -> false,
            "appealInformation" -> Json.obj(
              "reasonForAppeal" -> "bereavement",
              "honestyDeclaration" -> true,
              "statementToExplainAppealReason" -> "Bereavement statement",
              "startDateOfEvent" -> "2023-01-01",
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

          ViewStatusAuditModel(
            penaltyNumber = "pen1234",
            penaltyType = PenaltyTypeEnum.Additional,
            penaltyData = penaltyDataLPP,
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
                ),
                appealLevel = "01"
              )
          ).detail shouldBe Json.obj(
            "submittedBy" -> "agent",
            "identifierType" -> "NINO",
            "taxIdentifier" -> "AA123456A",
            "agentDetails" -> Json.obj(
              "agentReferenceNo" -> "XARN1234567",
              "isExcuseRelatedToAgent" -> true
            ),
            "penaltyNumber" -> "pen1234",
            "penaltyType" -> "Late Payment Penalty 2",
            "appealDateTime" -> "2023-01-05T00:00:00",
            "isMultipleAppeal" -> false,
            "appealInformation" -> Json.obj(
              "reasonForAppeal" -> "bereavement",
              "honestyDeclaration" -> true,
              "statementToExplainAppealReason" -> "Bereavement statement",
              "startDateOfEvent" -> "2023-01-01",
              "submittedAppealLate" -> false
            )
          )
        }
      }


      "auditing a Crime request" when {

        "penalty is an LSP (late appeal)" should {

          "serialise the Audit event correctly" in {

            implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForCrimeJourney

            ViewStatusAuditModel(
              penaltyNumber = "pen1234",
              penaltyType = PenaltyTypeEnum.Late_Submission,
              penaltyData = penaltyDataLSP,
              appealSubmission =
                AppealSubmission(
                  sourceSystem = "MDTP",
                  taxRegime = "ITSA",
                  customerReferenceNo = "123456789",
                  dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                  isLPP = false,
                  appealSubmittedBy = "customer",
                  agentDetails = None,
                  appealInformation = CrimeAppealInformation(
                    reasonableExcuse = ReasonableExcuse.Crime,
                    honestyDeclaration = true,
                    startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                    reportedIssueToPolice = CrimeReportedEnum.yes,
                    statement = Some("Crime statement"),
                    lateAppeal = true,
                    lateAppealReason = Some("Crime late appeal reason"),
                    isClientResponsibleForSubmission = Some(true),
                    isClientResponsibleForLateSubmission = Some(true)
                  ),
                  appealLevel = "01"
                )
            ).detail shouldBe Json.obj(
              "submittedBy" -> "customer",
              "identifierType" -> "NINO",
              "taxIdentifier" -> "AA123456A",
              "penaltyNumber" -> "pen1234",
              "penaltyType" -> "Late Submission Penalty",
              "appealDateTime" -> "2023-01-05T00:00:00",
              "isMultipleAppeal" -> false,
              "appealInformation" -> Json.obj(
                "reasonForAppeal" -> "crime",
                "honestyDeclaration" -> true,
                "wasCrimeReported" -> true,
                "statementToExplainAppealReason" -> "Crime statement",
                "startDateOfEvent" -> "2023-01-01",
                "submittedAppealLate" -> true,
                "lateAppealReason" -> "Crime late appeal reason"
              )
            )
          }
        }

        "penalty is an LPP1 (not late)" should {

          "serialise the Audit event correctly" in {

            implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForCrimeJourney

            ViewStatusAuditModel(
              penaltyNumber = "pen1234",
              penaltyType = PenaltyTypeEnum.Late_Payment,
              penaltyData = penaltyDataLPP,
              appealSubmission =
                AppealSubmission(
                  sourceSystem = "MDTP",
                  taxRegime = "ITSA",
                  customerReferenceNo = "123456789",
                  dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                  isLPP = false,
                  appealSubmittedBy = "customer",
                  agentDetails = None,
                  appealInformation = CrimeAppealInformation(
                    reasonableExcuse = ReasonableExcuse.Crime,
                    honestyDeclaration = true,
                    startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                    reportedIssueToPolice = CrimeReportedEnum.yes,
                    statement = Some("Crime statement"),
                    lateAppeal = false,
                    lateAppealReason = None,
                    isClientResponsibleForSubmission = Some(true),
                    isClientResponsibleForLateSubmission = Some(true)
                  ),
                  appealLevel = "01"
                )
            ).detail shouldBe Json.obj(
              "submittedBy" -> "customer",
              "identifierType" -> "NINO",
              "taxIdentifier" -> "AA123456A",
              "penaltyNumber" -> "pen1234",
              "penaltyType" -> "Late Payment Penalty 1",
              "appealDateTime" -> "2023-01-05T00:00:00",
              "isMultipleAppeal" -> false,
              "appealInformation" -> Json.obj(
                "reasonForAppeal" -> "crime",
                "honestyDeclaration" -> true,
                "wasCrimeReported" -> true,
                "statementToExplainAppealReason" -> "Crime statement",
                "startDateOfEvent" -> "2023-01-01",
                "submittedAppealLate" -> false
              )
            )
          }
        }

        "penalty is an LPP2 (agent - at fault - not reported to police)" should {

          "serialise the Audit event correctly" in {

            implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForCrimeJourney.copy(
              arn = Some("XARN1234567"),
              userAnswers = fakeRequestForCrimeJourney.userAnswers
                .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.agent)
                .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
            )(FakeRequest())

            ViewStatusAuditModel(
              penaltyNumber = "pen1234",
              penaltyType = PenaltyTypeEnum.Additional,
              penaltyData = penaltyDataLPP,
              appealSubmission =
                AppealSubmission(
                  sourceSystem = "MDTP",
                  taxRegime = "ITSA",
                  customerReferenceNo = "123456789",
                  dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                  isLPP = false,
                  appealSubmittedBy = "customer",
                  agentDetails = None,
                  appealInformation = CrimeAppealInformation(
                    reasonableExcuse = ReasonableExcuse.Crime,
                    honestyDeclaration = true,
                    startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                    reportedIssueToPolice = CrimeReportedEnum.no,
                    statement = Some("Crime statement"),
                    lateAppeal = false,
                    lateAppealReason = None,
                    isClientResponsibleForSubmission = Some(true),
                    isClientResponsibleForLateSubmission = Some(true)
                  ),
                  appealLevel = "01"
                )
            ).detail shouldBe Json.obj(
              "submittedBy" -> "agent",
              "identifierType" -> "NINO",
              "taxIdentifier" -> "AA123456A",
              "agentDetails" -> Json.obj(
                "agentReferenceNo" -> "XARN1234567",
                "isExcuseRelatedToAgent" -> true
              ),
              "penaltyNumber" -> "pen1234",
              "penaltyType" -> "Late Payment Penalty 2",
              "appealDateTime" -> "2023-01-05T00:00:00",
              "isMultipleAppeal" -> false,
              "appealInformation" -> Json.obj(
                "reasonForAppeal" -> "crime",
                "honestyDeclaration" -> true,
                "wasCrimeReported" -> false,
                "statementToExplainAppealReason" -> "Crime statement",
                "startDateOfEvent" -> "2023-01-01",
                "submittedAppealLate" -> false
              )
            )
          }
        }


        "auditing a Fire or Flood request" when {

          "penalty is an LSP (late appeal)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForFireAndFloodJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Submission,
                penaltyData = penaltyDataLSP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = FireOrFloodAppealInformation(
                      reasonableExcuse = ReasonableExcuse.FireOrFlood,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Fire statement"),
                      lateAppeal = true,
                      lateAppealReason = Some("Fire late appeal reason"),
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Submission Penalty",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "fireandflood",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Fire statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> true,
                  "lateAppealReason" -> "Fire late appeal reason"
                )
              )
            }
          }


          "penalty is an LPP1 (not late)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForFireAndFloodJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Payment,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = FireOrFloodAppealInformation(
                      reasonableExcuse = ReasonableExcuse.FireOrFlood,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Fire statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 1",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "fireandflood",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Fire statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> false
                )
              )
            }
          }

          "penalty is an LPP2 (agent - not at fault)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForFireAndFloodJourney.copy(
                arn = Some("XARN1234567"),
                userAnswers = fakeRequestForCrimeJourney.userAnswers
                  .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
                  .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)
              )(FakeRequest())

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Additional,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = FireOrFloodAppealInformation(
                      reasonableExcuse = ReasonableExcuse.FireOrFlood,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Fire statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "agent",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "agentDetails" -> Json.obj(
                  "agentReferenceNo" -> "XARN1234567",
                  "isExcuseRelatedToAgent" -> false
                ),
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 2",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "fireandflood",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Fire statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> false
                )
              )
            }
          }
        }

        "auditing a Loss of Staff request" when {

          "penalty is an LSP (late appeal)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForLossOfStaffJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Submission,
                penaltyData = penaltyDataLSP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = LossOfStaffAppealInformation(
                      reasonableExcuse = ReasonableExcuse.LossOfStaff,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Staff statement"),
                      lateAppeal = true,
                      lateAppealReason = Some("Staff late appeal reason"),
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Submission Penalty",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "lossOfEssentialStaff",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Staff statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> true,
                  "lateAppealReason" -> "Staff late appeal reason"
                )
              )
            }
          }

          "penalty is an LPP1 (not late)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForLossOfStaffJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Payment,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = LossOfStaffAppealInformation(
                      reasonableExcuse = ReasonableExcuse.LossOfStaff,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Staff statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 1",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "lossOfEssentialStaff",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Staff statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> false
                )
              )
            }
          }

          "penalty is an LPP2 (agent - not at fault)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForLossOfStaffJourney.copy(
                arn = Some("XARN1234567"),
                userAnswers = fakeRequestForCrimeJourney.userAnswers
                  .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
                  .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)
              )(FakeRequest())

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Additional,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = LossOfStaffAppealInformation(
                      reasonableExcuse = ReasonableExcuse.LossOfStaff,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Staff statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "agent",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "agentDetails" -> Json.obj(
                  "agentReferenceNo" -> "XARN1234567",
                  "isExcuseRelatedToAgent" -> false
                ),
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 2",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "lossOfEssentialStaff",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Staff statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> false
                )
              )
            }
          }
        }

        "auditing a Technical Issues request" when {

          "penalty is an LSP (late appeal)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForTechnicalIssuesJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Submission,
                penaltyData = penaltyDataLSP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = TechnicalIssuesAppealInformation(
                      reasonableExcuse = ReasonableExcuse.TechnicalIssues,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      endDateOfEvent = LocalDate.of(2023, 1, 4).atStartOfDay(),
                      statement = Some("Technical statement"),
                      lateAppeal = true,
                      lateAppealReason = Some("Technical late appeal reason"),
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Submission Penalty",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "technicalIssue",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Technical statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "endDateOfEvent" -> "2023-01-04",
                  "submittedAppealLate" -> true,
                  "lateAppealReason" -> "Technical late appeal reason"
                )
              )
            }
          }

          "penalty is an LPP1 (not late)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForTechnicalIssuesJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Payment,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = TechnicalIssuesAppealInformation(
                      reasonableExcuse = ReasonableExcuse.TechnicalIssues,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      endDateOfEvent = LocalDate.of(2023, 1, 4).atStartOfDay(),
                      statement = Some("Technical statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "01"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 1",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "technicalIssue",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Technical statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "endDateOfEvent" -> "2023-01-04",
                  "submittedAppealLate" -> false
                )
              )
            }
          }

          "penalty is an LPP2 (agent - not at fault)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForTechnicalIssuesJourney.copy(
                arn = Some("XARN1234567"),
                userAnswers = fakeRequestForCrimeJourney.userAnswers
                  .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
                  .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)
              )(FakeRequest())

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Additional,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = TechnicalIssuesAppealInformation(
                      reasonableExcuse = ReasonableExcuse.TechnicalIssues,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      endDateOfEvent = LocalDate.of(2023, 1, 4).atStartOfDay(),
                      statement = Some("Technical statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "agent",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "agentDetails" -> Json.obj(
                  "agentReferenceNo" -> "XARN1234567",
                  "isExcuseRelatedToAgent" -> false
                ),
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 2",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "technicalIssue",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Technical statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "endDateOfEvent" -> "2023-01-04",
                  "submittedAppealLate" -> false
                )
              )
            }
          }
        }

        "auditing a Health Issues request" when {

          "penalty is an LSP (late appeal)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForHealthIssuesJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Submission,
                penaltyData = penaltyDataLSP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = HealthAppealInformation(
                      reasonableExcuse = ReasonableExcuse.Health,
                      honestyDeclaration = true,
                      startDateOfEvent = Some(LocalDate.of(2023, 1, 1).atStartOfDay()),
                      endDateOfEvent = None,
                      hospitalStayInvolved = false,
                      eventOngoing = false,
                      statement = Some("Health statement"),
                      lateAppeal = true,
                      lateAppealReason = Some("Health late appeal reason"),
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Submission Penalty",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "health",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Health statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "hospitalStayInvolved" -> false,
                  "eventIsOngoing" -> false,
                  "submittedAppealLate" -> true,
                  "lateAppealReason" -> "Health late appeal reason"
                )
              )
            }
          }

          "penalty is an LPP1 (not late)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForHealthIssuesJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Payment,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = HealthAppealInformation(
                      reasonableExcuse = ReasonableExcuse.Health,
                      honestyDeclaration = true,
                      startDateOfEvent = None,
                      endDateOfEvent = None,
                      eventOngoing = false,
                      hospitalStayInvolved = false,
                      statement = Some("Health statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 1",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "health",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Health statement",
                  "hospitalStayInvolved" -> false,
                  "eventIsOngoing" -> false,
                  "submittedAppealLate" -> false
                )
              )
            }
          }

          "penalty is an LPP2 (agent - not at fault)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForHealthIssuesJourney.copy(
                arn = Some("XARN1234567"),
                userAnswers = fakeRequestForCrimeJourney.userAnswers
                  .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
                  .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)
              )(FakeRequest())

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Additional,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = HealthAppealInformation(
                      reasonableExcuse = ReasonableExcuse.Health,
                      honestyDeclaration = true,
                      startDateOfEvent = None,
                      endDateOfEvent = None,
                      statement = Some("Health statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      eventOngoing = false,
                      hospitalStayInvolved = false,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "agent",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "agentDetails" -> Json.obj(
                  "agentReferenceNo" -> "XARN1234567",
                  "isExcuseRelatedToAgent" -> false
                ),
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 2",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "health",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Health statement",
                  "submittedAppealLate" -> false,
                  "hospitalStayInvolved" -> false,
                  "eventIsOngoing" -> false,
                  "submittedAppealLate" -> false
                )
              )
            }
          }
        }

        "auditing an Unexpected Hospital Stay request" when {

          "penalty is an LSP (late appeal)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForHospitalStayJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Submission,
                penaltyData = penaltyDataLSP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = HealthAppealInformation(
                      reasonableExcuse = ReasonableExcuse.UnexpectedHospital,
                      honestyDeclaration = true,
                      startDateOfEvent = Some(LocalDate.of(2023, 1, 1).atStartOfDay()),
                      endDateOfEvent = Some(LocalDate.of(2023, 1, 4).atStartOfDay()),
                      hospitalStayInvolved = true,
                      eventOngoing = false,
                      statement = Some("UnexpectedHospital statement"),
                      lateAppeal = true,
                      lateAppealReason = Some("UnexpectedHospital late appeal reason"),
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Submission Penalty",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "unexpectedHospital",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "UnexpectedHospital statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "endDateOfEvent" -> "2023-01-04",
                  "hospitalStayInvolved" -> true,
                  "eventIsOngoing" -> false,
                  "submittedAppealLate" -> true,
                  "lateAppealReason" -> "UnexpectedHospital late appeal reason"
                )
              )
            }
          }

          "penalty is an LPP1 (not late)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForHospitalStayJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Payment,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = HealthAppealInformation(
                      reasonableExcuse = ReasonableExcuse.UnexpectedHospital,
                      honestyDeclaration = true,
                      startDateOfEvent = None,
                      endDateOfEvent = None,
                      eventOngoing = false,
                      hospitalStayInvolved = true,
                      statement = Some("UnexpectedHospital statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 1",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "unexpectedHospital",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "UnexpectedHospital statement",
                  "hospitalStayInvolved" -> true,
                  "eventIsOngoing" -> false,
                  "submittedAppealLate" -> false
                )
              )
            }
          }

          "penalty is an LPP2 (agent - not at fault)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForHospitalStayJourney.copy(
                arn = Some("XARN1234567"),
                userAnswers = fakeRequestForCrimeJourney.userAnswers
                  .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
                  .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)
              )(FakeRequest())

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Additional,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = HealthAppealInformation(
                      reasonableExcuse = ReasonableExcuse.UnexpectedHospital,
                      honestyDeclaration = true,
                      startDateOfEvent = None,
                      endDateOfEvent = Some(LocalDate.of(2023, 1, 4).atStartOfDay()),
                      statement = Some("UnexpectedHospital statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      eventOngoing = false,
                      hospitalStayInvolved = true,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true)
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "agent",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "agentDetails" -> Json.obj(
                  "agentReferenceNo" -> "XARN1234567",
                  "isExcuseRelatedToAgent" -> false
                ),
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 2",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "unexpectedHospital",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "UnexpectedHospital statement",
                  "endDateOfEvent" -> "2023-01-04",
                  "submittedAppealLate" -> false,
                  "hospitalStayInvolved" -> true,
                  "eventIsOngoing" -> false,
                  "submittedAppealLate" -> false
                )
              )
            }
          }
        }

        "auditing a Other request" when {

          "penalty is an LSP (late appeal)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForOtherJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Submission,
                penaltyData = penaltyDataLSP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = OtherAppealInformation(
                      reasonableExcuse = ReasonableExcuse.Other,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Other statement"),
                      lateAppeal = true,
                      lateAppealReason = Some("Other late appeal reason"),
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true),
                      supportingEvidence = Some(Evidence(2)),
                      uploadedFiles = Some(Seq(callbackModel, callbackModel2))
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Submission Penalty",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "other",
                  "honestyDeclaration" -> true,
                  "statementToExplainAppealReason" -> "Other statement",
                  "startDateOfEvent" -> "2023-01-01",
                  "submittedAppealLate" -> true,
                  "lateAppealReason" -> "Other late appeal reason",
                  "numberOfUploadedFiles" -> 2,
                  "uploadedFiles" -> Json.arr(
                    Json.obj(
                      "downloadUrl" -> "download.file/url",
                      "fileChecksum" -> "check1234",
                      "fileMimeType" -> "text/plain",
                      "fileName" -> "file1.txt",
                      "uploadTimestamp" -> "2023-01-01T01:01:00",
                      "upscanReference" -> "ref1"
                    ),
                    Json.obj(
                      "downloadUrl" -> "download.file2/url",
                      "fileChecksum" -> "check1234",
                      "fileMimeType" -> "text/plain",
                      "fileName" -> "file2.txt",
                      "uploadTimestamp" -> "2023-01-01T01:01:00",
                      "upscanReference" -> "ref2"
                    )
                  )
                )
              )
            }
          }

          "penalty is an LPP1 (not late)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForOtherJourney

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Late_Payment,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = OtherAppealInformation(
                      reasonableExcuse = ReasonableExcuse.Other,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Other statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true),
                      supportingEvidence = Some(Evidence(2)),
                      uploadedFiles = Some(Seq(callbackModel, callbackModel2))
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "customer",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 1",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "other",
                  "honestyDeclaration" -> true,
                  "startDateOfEvent" -> "2023-01-01",
                  "statementToExplainAppealReason" -> "Other statement",
                  "submittedAppealLate" -> false,
                  "numberOfUploadedFiles" -> 2,
                  "uploadedFiles" -> Json.arr(
                    Json.obj(
                      "downloadUrl" -> "download.file/url",
                      "fileChecksum" -> "check1234",
                      "fileMimeType" -> "text/plain",
                      "fileName" -> "file1.txt",
                      "uploadTimestamp" -> "2023-01-01T01:01:00",
                      "upscanReference" -> "ref1"
                    ),
                    Json.obj(
                      "downloadUrl" -> "download.file2/url",
                      "fileChecksum" -> "check1234",
                      "fileMimeType" -> "text/plain",
                      "fileName" -> "file2.txt",
                      "uploadTimestamp" -> "2023-01-01T01:01:00",
                      "upscanReference" -> "ref2"
                    )
                  )
                )
              )
            }
          }

          "penalty is an LPP2 (agent - not at fault)" should {

            "serialise the Audit event correctly" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = fakeRequestForOtherJourney.copy(
                arn = Some("XARN1234567"),
                userAnswers = fakeRequestForCrimeJourney.userAnswers
                  .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
                  .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.client)
              )(FakeRequest())

              ViewStatusAuditModel(
                penaltyNumber = "pen1234",
                penaltyType = PenaltyTypeEnum.Additional,
                penaltyData = penaltyDataLPP,
                appealSubmission =
                  AppealSubmission(
                    sourceSystem = "MDTP",
                    taxRegime = "ITSA",
                    customerReferenceNo = "123456789",
                    dateOfAppeal = LocalDate.of(2023, 1, 5).atStartOfDay(),
                    isLPP = false,
                    appealSubmittedBy = "customer",
                    agentDetails = None,
                    appealInformation = OtherAppealInformation(
                      reasonableExcuse = ReasonableExcuse.Other,
                      honestyDeclaration = true,
                      startDateOfEvent = LocalDate.of(2023, 1, 1).atStartOfDay(),
                      statement = Some("Other statement"),
                      lateAppeal = false,
                      lateAppealReason = None,
                      isClientResponsibleForSubmission = Some(true),
                      isClientResponsibleForLateSubmission = Some(true),
                      supportingEvidence = Some(Evidence(2)),
                      uploadedFiles = Some(Seq(callbackModel, callbackModel2))
                    ),
                    appealLevel = "02"
                  )
              ).detail shouldBe Json.obj(
                "submittedBy" -> "agent",
                "identifierType" -> "NINO",
                "taxIdentifier" -> "AA123456A",
                "agentDetails" -> Json.obj(
                  "agentReferenceNo" -> "XARN1234567",
                  "isExcuseRelatedToAgent" -> false
                ),
                "penaltyNumber" -> "pen1234",
                "penaltyType" -> "Late Payment Penalty 2",
                "appealDateTime" -> "2023-01-05T00:00:00",
                "isMultipleAppeal" -> false,
                "appealInformation" -> Json.obj(
                  "reasonForAppeal" -> "other",
                  "honestyDeclaration" -> true,
                  "startDateOfEvent" -> "2023-01-01",
                  "statementToExplainAppealReason" -> "Other statement",
                  "submittedAppealLate" -> false,
                  "numberOfUploadedFiles" -> 2,
                  "uploadedFiles" -> Json.arr(
                    Json.obj(
                      "downloadUrl" -> "download.file/url",
                      "fileChecksum" -> "check1234",
                      "fileMimeType" -> "text/plain",
                      "fileName" -> "file1.txt",
                      "uploadTimestamp" -> "2023-01-01T01:01:00",
                      "upscanReference" -> "ref1"
                    ),
                    Json.obj(
                      "downloadUrl" -> "download.file2/url",
                      "fileChecksum" -> "check1234",
                      "fileMimeType" -> "text/plain",
                      "fileName" -> "file2.txt",
                      "uploadTimestamp" -> "2023-01-01T01:01:00",
                      "upscanReference" -> "ref2"
                    )
                  )
                )
              )
            }
          }
        }
      }
    }
  }
}
