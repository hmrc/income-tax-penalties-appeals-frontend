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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services

import fixtures.FileUploadFixtures
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.httpParsers.BadRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.MockUpscanInitiateConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{FailureReasonEnum, UploadStatus, UploadStatusEnum, UpscanInitiateRequest}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.mocks.MockFileUploadJourneyRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class UpscanServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite
  with MockFileUploadJourneyRepository
  with MockUpscanInitiateConnector
  with FileUploadFixtures
  with LogCapturing {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val ec: ExecutionContext = ExecutionContext.global

  lazy val testDateTime: LocalDateTime = LocalDateTime.now()

  lazy val timeMachine: TimeMachine = new TimeMachine(appConfig) {
    override def getCurrentDateTime: LocalDateTime = testDateTime
  }

  val testService = new UpscanService(
    mockUpscanInitiateConnector,
    mockFileUploadJourneyRepository,
    appConfig,
    timeMachine
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "calling .initiateNewFileUpload()" when {

    val initiateRequest = UpscanInitiateRequest(testJourneyId, appConfig, isAgent)

    "a successful response is returned from the UpscanConnector" when {

      "the response is successfully written to Mongo" should {

        "return the response" in {
          mockInitiate(testJourneyId, initiateRequest)(Future.successful(Right(initiateResponse)))
          mockUpsertFileUpload(testJourneyId, waitingFile.copy(lastUpdated = testDateTime))(Future.successful(cacheItem(waitingFile)))

          await(testService.initiateNewFileUpload(testJourneyId, isAgent)) shouldBe initiateResponse
        }
      }

      "the response fails to be written to Mongo" should {

        "throw and exception with useful logging, including a PagerDuty trigger" in {
          mockInitiate(testJourneyId, initiateRequest)(Future.successful(Right(initiateResponse)))
          mockUpsertFileUpload(testJourneyId, waitingFile.copy(lastUpdated = testDateTime))(Future.failed(new Exception("bang")))

          withCaptureOfLoggingFrom(logger) { logs =>
            intercept[Exception](await(testService.initiateNewFileUpload(testJourneyId, isAgent)))
            logs.exists(_.getMessage.contains(s"[$FAILED_INITIATE_CALL_UPSCAN][UpscanService][initiateNewFileUpload] An exception of type Exception occurred for journeyId: $testJourneyId")) shouldBe true
          }
        }
      }
    }

    "an error response is returned from the UpscanInitiateConnector" when {

      "the response is a Left" should {

        "throw and exception with useful logging, including a PagerDuty trigger" in {
          mockInitiate(testJourneyId, initiateRequest)(Future.successful(Left(BadRequest)))
          withCaptureOfLoggingFrom(logger) { logs =>
            intercept[Exception](await(testService.initiateNewFileUpload(testJourneyId, isAgent)))
            logs.exists(_.getMessage.contains(s"[$FAILED_INITIATE_CALL_UPSCAN][UpscanService][initiateNewFileUpload] An exception of type Exception occurred for journeyId: $testJourneyId")) shouldBe true
          }
        }
      }

      "the response is a failed future" should {

        "throw and exception with useful logging, including a PagerDuty trigger" in {
          mockInitiate(testJourneyId, initiateRequest)(Future.failed(new Exception("bang")))
          withCaptureOfLoggingFrom(logger) { logs =>
            intercept[Exception](await(testService.initiateNewFileUpload(testJourneyId, isAgent)))
            logs.exists(_.getMessage.contains(s"[$FAILED_INITIATE_CALL_UPSCAN][UpscanService][initiateNewFileUpload] An exception of type Exception occurred for journeyId: $testJourneyId")) shouldBe true
          }
        }
      }
    }
  }

  "calling .upsertFileUpload()" when {

    "a file is successfully written to Mongo" should {

      "return the file that was stored as the response" in {
        mockUpsertFileUpload(testJourneyId, callbackModel)(Future.successful(cacheItem(callbackModel)))
        await(testService.upsertFileUpload(testJourneyId, callbackModel)) shouldBe callbackModel
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging AND trigger PagerDuty logMessage" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockUpsertFileUpload(testJourneyId, callbackModel)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.upsertFileUpload(testJourneyId, callbackModel))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][upsertFileUpload] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
          logs.exists(_.getMessage.contains(s"[$FAILED_UPSERT_FILE_UPSCAN]")) shouldBe true
        }
      }
    }
  }

  "calling .getFile()" when {

    "a file is returned from Mongo" should {

      "return the file" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(Some(callbackModel)))
        await(testService.getFile(testJourneyId, fileRef1)) shouldBe Some(callbackModel)
      }
    }

    "NO file is found in Mongo" should {

      "return None" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(None))
        await(testService.getFile(testJourneyId, fileRef1)) shouldBe None
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetFile(testJourneyId, fileRef1)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.getFile(testJourneyId, fileRef1))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getFile] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
        }
      }
    }
  }

  "calling .getAllFiles()" when {

    "files are returned from Mongo" should {

      "return a sequence of files" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq(
          callbackModel,
          callbackModel2
        )))
        await(testService.getAllFiles(testJourneyId)) shouldBe Seq(
          callbackModel,
          callbackModel2
        )
      }
    }

    "NO files are returned from Mongo" should {

      "return Seq.empty" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq()))
        await(testService.getAllFiles(testJourneyId)) shouldBe Seq.empty
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetAllFiles(testJourneyId)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.getAllFiles(testJourneyId))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getAllFiles] An exception of type RuntimeException occurred for journeyId: $testJourneyId")) shouldBe true
        }
      }
    }
  }

  "calling .getAllReadyFiles()" when {

    "files are returned from Mongo, one is READY" should {

      "return a sequence of just the ready files" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq(
          waitingFile,
          callbackModel,
          callbackModel2.copy(fileStatus = UploadStatusEnum.FAILED)
        )))
        await(testService.getAllReadyFiles(testJourneyId)) shouldBe Seq(callbackModel)
      }
    }

    "NO files are returned from Mongo" should {

      "return Seq.empty" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq()))
        await(testService.getAllReadyFiles(testJourneyId)) shouldBe Seq.empty
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetAllFiles(testJourneyId)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.getAllReadyFiles(testJourneyId))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getAllFiles] An exception of type RuntimeException occurred for journeyId: $testJourneyId")) shouldBe true
        }
      }
    }
  }

  "calling .countAllFiles()" when {

    "files are returned from Mongo" should {

      "return a count of all the files for the journey" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq(
          waitingFile,
          callbackModel,
          callbackModel2.copy(fileStatus = UploadStatusEnum.FAILED)
        )))
        await(testService.countAllFiles(testJourneyId)) shouldBe 3
      }
    }

    "NO files are returned from Mongo" should {

      "return count of 0" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq()))
        await(testService.countAllFiles(testJourneyId)) shouldBe 0
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetAllFiles(testJourneyId)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.countAllFiles(testJourneyId))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getAllFiles] An exception of type RuntimeException occurred for journeyId: $testJourneyId")) shouldBe true
        }
      }
    }
  }

  "calling .countAllReadyFiles()" when {

    "files are returned from Mongo" should {

      "return a count of only READY files for the journey" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq(
          waitingFile,
          callbackModel,
          callbackModel2.copy(fileStatus = UploadStatusEnum.FAILED)
        )))
        await(testService.countAllReadyFiles(testJourneyId)) shouldBe 1
      }
    }

    "NO files are returned from Mongo" should {

      "return count of 0" in {
        mockGetAllFiles(testJourneyId)(Future.successful(Seq()))
        await(testService.countAllReadyFiles(testJourneyId)) shouldBe 0
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetAllFiles(testJourneyId)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.countAllReadyFiles(testJourneyId))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getAllFiles] An exception of type RuntimeException occurred for journeyId: $testJourneyId")) shouldBe true
        }
      }
    }
  }

  "calling .getStatusOfFileUpload()" when {

    "file is returned from Mongo" when {

      "the file has failed upload (rejected by Upscan)" should {

        "return the failure status with failure message" in {
          mockGetFile(testJourneyId, fileRef1)(Future.successful(Some(callbackModelFailed)))
          await(testService.getStatusOfFileUpload(testJourneyId, fileRef1)) shouldBe
            Some(UploadStatus(FailureReasonEnum.QUARANTINE.toString, Some(testVirusMessage)))
        }
      }

      "the file has NOT been rejected by Upscan" should {

        "return the file status" in {
          mockGetFile(testJourneyId, fileRef1)(Future.successful(Some(callbackModel)))
          await(testService.getStatusOfFileUpload(testJourneyId, fileRef1)) shouldBe
            Some(UploadStatus(callbackModel.fileStatus.toString))
        }
      }
    }

    "when no file is returned" should {

      "return None" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(None))
        await(testService.getStatusOfFileUpload(testJourneyId, fileRef1)) shouldBe None
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging AND log PagerDuty trigger" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetFile(testJourneyId, fileRef1)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.getStatusOfFileUpload(testJourneyId, fileRef1))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getFile] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
          logs.exists(_.getMessage.contains(s"[$FILE_UPLOAD_STATUS_NOT_FOUND_UPSCAN]")) shouldBe true
        }
      }
    }
  }

  "calling .getFormFieldsForFile()" when {

    "file is returned from Mongo" when {

      "return the upload fields" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(Some(callbackModelFailed)))
        await(testService.getFormFieldsForFile(testJourneyId, fileRef1)) shouldBe callbackModel.uploadFields
      }
    }

    "when no file is returned" should {

      "return None" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(None))
        await(testService.getFormFieldsForFile(testJourneyId, fileRef1)) shouldBe None
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetFile(testJourneyId, fileRef1)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.getFormFieldsForFile(testJourneyId, fileRef1))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getFile] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
        }
      }
    }
  }

  "calling .removeFile()" when {

    "removal is successful" should {

      "return true" in {
        mockRemoveFile(testJourneyId, fileRef1)(Future.successful(()))
        await(testService.removeFile(testJourneyId, fileRef1)) shouldBe true
      }
    }

    "when the Future fails with a throwable" should {

      "handle the exception and include some useful logging including PagerDuty trigger, returning false" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockRemoveFile(testJourneyId, fileRef1)(Future.failed(new RuntimeException("bang")))

          await(testService.removeFile(testJourneyId, fileRef1)) shouldBe false

          logs.exists(_.getMessage.contains(s"[UpscanService][removeFile] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
          logs.exists(_.getMessage.contains(s"[$FILE_REMOVAL_FAILURE_UPSCAN]")) shouldBe true
        }
      }
    }
  }

  "calling .removeAllFile()" when {

    "removal is successful" should {

      "return true" in {
        mockRemoveAllFiles(testJourneyId)(Future.successful(()))
        await(testService.removeAllFiles(testJourneyId)) shouldBe true
      }
    }

    "when the Future fails with a throwable" should {

      "handle the exception and include some useful logging with PagerDuty trigger, returning false" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockRemoveAllFiles(testJourneyId)(Future.failed(new RuntimeException("bang")))

          await(testService.removeFile(testJourneyId, fileRef1)) shouldBe false

          logs.exists(_.getMessage.contains(s"[UpscanService][removeFile] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
          logs.exists(_.getMessage.contains(s"[$FILE_REMOVAL_FAILURE_UPSCAN]")) shouldBe true
        }
      }
    }
  }

  "calling .getUploadedFilename()" when {

    "file is returned from Mongo" when {

      "return the uploaded files filename" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(Some(callbackModel)))
        await(testService.getUploadedFilename(testJourneyId, fileRef1)) shouldBe callbackModel.uploadDetails.map(_.fileName)
      }
    }

    "when no file is returned" should {

      "return None" in {
        mockGetFile(testJourneyId, fileRef1)(Future.successful(None))
        await(testService.getUploadedFilename(testJourneyId, fileRef1)) shouldBe None
      }
    }

    "when the Future fails with a throwable" should {

      "throw the exception but include some useful logging with PagerDuty trigger" in {
        withCaptureOfLoggingFrom(logger) { logs =>
          mockGetFile(testJourneyId, fileRef1)(Future.failed(new RuntimeException("bang")))

          intercept[RuntimeException](await(testService.getUploadedFilename(testJourneyId, fileRef1))).getMessage shouldBe "bang"

          logs.exists(_.getMessage.contains(s"[UpscanService][getFile] An exception of type RuntimeException occurred for journeyId: $testJourneyId, fileReference: $fileRef1")) shouldBe true
          logs.exists(_.getMessage.contains(s"[$FILE_NAME_RETRIEVAL_FAILURE_UPSCAN]")) shouldBe true
        }
      }
    }
  }
}
