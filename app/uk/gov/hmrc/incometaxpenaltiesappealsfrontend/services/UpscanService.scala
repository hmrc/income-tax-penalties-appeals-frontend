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

import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.upscan.UpscanInitiateConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.FileUploadJourneyRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ExceptionHandlingUtil, TimeMachine}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanService @Inject()(upscanConnector: UpscanInitiateConnector,
                              uploadRepo: FileUploadJourneyRepository,
                              appConfig: AppConfig,
                              timeMachine: TimeMachine)
                             (implicit ec: ExecutionContext) extends ExceptionHandlingUtil {

  def initiateNewFileUpload(journeyId: String)(implicit hc: HeaderCarrier): Future[UpscanInitiateResponse] = {
    withExceptionHandling(
      methodName = "initiateNewFileUpload",
      identifiers = Map("journeyId" -> journeyId),
      pagerDutyTriggerKey = Some(FAILED_INITIATE_CALL_UPSCAN)
    ) {
      upscanConnector.initiate(journeyId, UpscanInitiateRequest(journeyId, appConfig)).flatMap {
        case Right(response) =>
          uploadRepo.upsertFileUpload(journeyId, UploadJourney(
            reference = response.reference,
            fileStatus = UploadStatusEnum.WAITING,
            uploadFields = Some(response.uploadRequest),
            lastUpdated = timeMachine.getCurrentDateTime
          )).map { _ =>
            logger.info(s"[UpscanService][initiateNewFileUpload] Successfully initiated file upload for journeyId: $journeyId with fileReference: ${response.reference}")
            response
          }
        case Left(error) =>
          Future.failed(new Exception(s"Failed to initiate file upload for journeyId: $journeyId with error: $error"))
      }
    }
  }

  def upsertFileUpload(journeyId: String, uploadJourney: UploadJourney): Future[UploadJourney] =
    withExceptionHandling(
      methodName = "upsertFileUpload",
      identifiers = Map("journeyId" -> journeyId, "fileReference" -> uploadJourney.reference),
      pagerDutyTriggerKey = Some(FAILED_UPSERT_FILE_UPSCAN)
    ) {
      uploadRepo.upsertFileUpload(journeyId, uploadJourney).map { cacheItem =>
        logger.debug(s"[UpscanService][upsertFileUpload] Upserted file for journeyId: $journeyId, JSON:\n\n" + Json.toJson(cacheItem.data))
        uploadJourney
      }
    }

  def getFile(journeyId: String, fileReference: String): Future[Option[UploadJourney]] =
    withExceptionHandling("getFile", Map("journeyId" -> journeyId, "fileReference" -> fileReference)) {
      uploadRepo.getFile(journeyId, fileReference).map {
        case Some(file) =>
          logger.debug(s"[UpscanService][getFile] Found a file for journeyId: $journeyId and fileReference: $fileReference, JSON:\n\n" + Json.toJson(file))
          Some(file)
        case None =>
          logger.debug(s"[UpscanService][getFile] No file found for journeyId: $journeyId and fileReference: $fileReference")
          None
      }
    }

  def getAllFiles(journeyId: String): Future[Seq[UploadJourney]] =
    withExceptionHandling("getAllFiles", Map("journeyId" -> journeyId)) {
      uploadRepo.getAllFiles(journeyId).map { files =>
        logger.debug(s"[UpscanService][getAllFiles] Found ${files.size} files for journeyId: $journeyId\n" + Json.toJson(files))
        files
      }
    }

  def getAllReadyFiles(journeyId: String): Future[Seq[UploadJourney]] =
    getAllFiles(journeyId).map(_.filter(_.fileStatus == UploadStatusEnum.READY)).map { files =>
      logger.debug(s"[UpscanService][getAllReadyFiles] Found ${files.size} with status ${UploadStatusEnum.READY} files for journeyId: $journeyId\n" + Json.toJson(files))
      files
    }

  def countAllFiles(journeyId: String): Future[Int] =
    getAllFiles(journeyId).map(_.size).map { count =>
      logger.debug(s"[UpscanService][countAllFiles] Counted $count files for journeyId: $journeyId")
      count
    }

  def countAllReadyFiles(journeyId: String): Future[Int] =
    getAllReadyFiles(journeyId).map(_.size).map { count =>
      logger.debug(s"[UpscanService][countAllReadyFiles] Counted $count READY files for journeyId: $journeyId")
      count
    }

  def getStatusOfFileUpload(journeyId: String, fileReference: String): Future[Option[UploadStatus]] =
    withExceptionHandling(
      methodName = "getStatusOfFileUpload",
      identifiers = Map("journeyId" -> journeyId, "fileReference" -> fileReference),
      pagerDutyTriggerKey = Some(FILE_UPLOAD_STATUS_NOT_FOUND_UPSCAN)
    ) {
      getFile(journeyId, fileReference).map(_.map { upload =>
        upload.failureDetails.fold {
          logger.debug(s"[UpscanService][getStatusOfFileUpload] File upload status '${upload.fileStatus}' for journeyId: $journeyId and fileReference: $fileReference")
          UploadStatus(upload.fileStatus.toString)
        } { failure =>
          logger.debug(s"[UpscanService][getStatusOfFileUpload] File upload failed reason '${failure.failureReason}' for journeyId: $journeyId and fileReference: $fileReference with message: " + failure.message)
          UploadStatus(failure.failureReason.toString, Some(failure.message))
        }
      })
    }

  def getFormFieldsForFile(journeyId: String, fileReference: String): Future[Option[UploadFormFields]] =
    getFile(journeyId, fileReference).map(_.flatMap(_.uploadFields)).map { formFields =>
      logger.debug(s"[UpscanService][getFormFieldsForFile] Form fields for journeyId: $journeyId and fileReference: $fileReference\n\n" + formFields.map("\n" + _))
      formFields
    }

  def removeFile(journeyId: String, fileReference: String): Future[Boolean] =
    withExceptionHandling(
      methodName = "removeFile",
      identifiers = Map("journeyId" -> journeyId, "fileReference" -> fileReference),
      pagerDutyTriggerKey = Some(FILE_REMOVAL_FAILURE_UPSCAN)
    ) {
      uploadRepo.removeFile(journeyId, fileReference).map { _ =>
        logger.debug(s"[UpscanService][removeFile] Removed file for journeyId: $journeyId and fileReference: $fileReference")
        true
      }
    }.recover(_ => false)

  def removeAllFiles(journeyId: String): Future[Boolean] =
    withExceptionHandling(
      methodName = "removeAllFiles",
      identifiers = Map("journeyId" -> journeyId),
      pagerDutyTriggerKey = Some(FILE_REMOVAL_FAILURE_UPSCAN)
    ) {
      uploadRepo.removeAllFiles(journeyId).map { _ =>
        logger.debug(s"[UpscanService][removeAllFiles] Removed file for journeyId: $journeyId")
        true
      }
    }.recover(_ => false)

  def getUploadedFilename(journeyId: String, fileReference: String): Future[Option[String]] =
    withExceptionHandling(
      methodName = "getUploadedFilename",
      identifiers = Map("journeyId" -> journeyId, "fileReference" -> fileReference),
      pagerDutyTriggerKey = Some(FILE_NAME_RETRIEVAL_FAILURE_UPSCAN)
    ) {
      getFile(journeyId, fileReference).map(_.flatMap(_.uploadDetails.map(_.fileName))).map { fileName =>
        if (fileName.isDefined) logger.debug(s"[UpscanService][getFileNameForJourney] File name: ${fileName.get}, for journeyId: $journeyId and fileReference: $fileReference")
        fileName
      }
    }
}
