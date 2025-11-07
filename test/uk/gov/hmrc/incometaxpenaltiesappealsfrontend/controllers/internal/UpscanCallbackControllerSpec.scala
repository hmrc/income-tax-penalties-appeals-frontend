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
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.FailureReasonEnum.INVALID_FILENAME
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadStatusEnum.FAILED
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{FailureDetails, UploadJourney}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.mocks.MockUpscanService
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.ExecutionContext

class UpscanCallbackControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite
  with LogCapturing with MockUpscanService with FileUploadFixtures {

  implicit lazy val ec: ExecutionContext = ExecutionContext.global
  lazy val controller: UpscanCallbackController = new UpscanCallbackController(mockUpscanService, stubMessagesControllerComponents())

  implicit class FileDecorator(file: UploadJourney) {
    def withFilename(filename: String): UploadJourney =
      file.copy(uploadDetails = file.uploadDetails.map(_.copy(fileName = filename)))
  }

  "UpscanCallbackController" when {

    "calling .validateFilename()" when {

      Seq(
        "file1.txt",
        "file_1.txt",
        "file-1.txt",
        "File1.txt",
        "F_I_L_E_1.txt",
        "file.name.one.txt"
      ).foreach { validFilenameExample =>

        s"the filename is valid (filename=$validFilenameExample)" should {

          "return the callback model unchanged" in {
            val file = callbackModel.withFilename(validFilenameExample)
            controller.validateFilenameAndIfPasswordProtected(file) shouldBe file
          }
        }
      }

      Seq(
        "file&1.txt",
        "file 1.txt",
        "file 2.txt", //&nbsp;
        "£file.txt",
        "@file.txt"
      ).foreach { invalidFilenameExample =>

        s"the filename is invalid (filename=$invalidFilenameExample)" should {

          "return the callback model unchanged" in {
            val file = callbackModel.withFilename(invalidFilenameExample)
            controller.validateFilenameAndIfPasswordProtected(file) shouldBe file.copy(
              fileStatus = FAILED,
              failureDetails = Some(FailureDetails(
                failureReason = INVALID_FILENAME,
                message = s"Filename contains invalid characters, filename='$invalidFilenameExample'"
              ))
            )
          }
        }
      }
    }
  }
}
