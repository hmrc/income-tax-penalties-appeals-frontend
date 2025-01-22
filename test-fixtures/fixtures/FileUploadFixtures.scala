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

package fixtures

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{FailureDetails, FailureReasonEnum, UploadDetails, UploadJourney, UploadStatusEnum}

import java.time.LocalDateTime

trait FileUploadFixtures {

  val fileRef1 = "ref1"
  val fileRef2 = "ref2"
  val testVirusMessage = "File has a virus"

  val waitingFile: UploadJourney = UploadJourney(
    reference = fileRef1,
    fileStatus = UploadStatusEnum.WAITING,
    downloadUrl = None,
    uploadDetails = None,
    uploadFields = None
  )

  val callbackModel: UploadJourney = UploadJourney(
    reference = fileRef1,
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2023, 1, 1, 1, 1),
      checksum = "check1234",
      size = 2
    )),
    uploadFields = Some(Map(
      "key" -> "abcxyz",
      "algo" -> "md5"
    ))
  )

  val callbackModel2: UploadJourney = callbackModel.copy(
    reference = fileRef2,
    downloadUrl = Some("download.file2/url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file2.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2023, 1, 1, 1, 1),
      checksum = "check1234",
      size = 3
    ))
  )

  val callbackModelFailed: UploadJourney = callbackModel.copy(
    fileStatus = UploadStatusEnum.FAILED,
    downloadUrl = None,
    uploadDetails = None,
    failureDetails = Some(
      FailureDetails(
        failureReason = FailureReasonEnum.QUARANTINE,
        message = testVirusMessage
      )
    )
  )

}
