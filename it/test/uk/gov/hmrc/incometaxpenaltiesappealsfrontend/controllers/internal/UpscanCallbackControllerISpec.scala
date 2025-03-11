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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.internal

import fixtures.FileUploadFixtures
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.FailureDetails
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.FailureReasonEnum.INVALID_FILENAME
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadStatusEnum.FAILED
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.FileUploadJourneyRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing


class UpscanCallbackControllerISpec extends ComponentSpecHelper with FileUploadFixtures with LogCapturing {

  lazy val fileUploadRepository: FileUploadJourneyRepository = app.injector.instanceOf[FileUploadJourneyRepository]

  override def beforeEach(): Unit = {
    await(fileUploadRepository.removeAllFiles(testJourneyId))
    super.beforeEach()
  }

  "POST /internal/upscan-callback/:journeyId" when {

    "a valid UploadJourney payload is received" when {

      "a file for that callback reference exists within the File Upload journey" when {

        "the filename is invalid (includes special character not in regex)" should {

          "update the File Upload Journey as FAILED, persisting the values of the UploadFields. Returning a NO_CONTENT" in {

            await(fileUploadRepository.upsertFileUpload(testJourneyId, waitingFile))

            val invalidFileNameModel =
              callbackModel.copy(uploadDetails = Some(callbackModel.uploadDetails.get.copy(fileName = "file&.txt")))

            val result = post(s"/internal/upscan-callback/$testJourneyId")(invalidFileNameModel)

            result.status shouldBe NO_CONTENT
            await(fileUploadRepository.getFile(testJourneyId, callbackModel.reference)) shouldBe Some(
              invalidFileNameModel.copy(
                uploadFields = Some(uploadFields),
                fileStatus = FAILED,
                failureDetails = Some(FailureDetails(
                  failureReason = INVALID_FILENAME,
                  message = "Filename contains invalid characters, filename='file&.txt'"
                ))
              )
            )
          }
        }

        "the filename is valid" should {

          "update the File Upload Journey, persisting the values of the UploadFields. Returning a NO_CONTENT" in {

            await(fileUploadRepository.upsertFileUpload(testJourneyId, waitingFile))

            val result = post(s"/internal/upscan-callback/$testJourneyId")(callbackModel)

            result.status shouldBe NO_CONTENT
            await(fileUploadRepository.getFile(testJourneyId, callbackModel.reference)) shouldBe Some(callbackModel.copy(uploadFields = Some(uploadFields)))
          }
        }
      }

      "a file for that callback reference does NOT exist within the File Upload journey" should {

        "Returning a GONE response to Upscan and log a warning message" in {

          withCaptureOfLoggingFrom(logger) { logs =>
            val result = post(s"/internal/upscan-callback/$testJourneyId")(callbackModel)
            result.status shouldBe GONE

            logs.exists(_.getMessage.contains(s"[UpscanCallbackController][callbackFromUpscan] Callback from Upscan received for journeyId: $testJourneyId, fileReference: ${callbackModel.reference} that does not exist in the File Upload repository")) shouldBe true
          }
        }
      }
    }
  }
}
