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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.JsonUtils

import java.time.LocalDateTime

case class UploadJourney(reference: String,
                         fileStatus: UploadStatusEnum.Value,
                         downloadUrl: Option[String] = None,
                         uploadDetails: Option[UploadDetails] = None,
                         failureDetails: Option[FailureDetails] = None,
                         lastUpdated: LocalDateTime = LocalDateTime.now(),
                         uploadFields: Option[UploadFormFields] = None)

object UploadJourney extends JsonUtils {
  val writesSubmission: OWrites[UploadJourney] = (o: UploadJourney) => {
    jsonObjNoNulls(
      ("reference" -> o.reference),
      ("fileStatus" -> o.fileStatus),
      ("downloadUrl" -> o.downloadUrl),
      ("uploadDetails" -> o.uploadDetails),
      ("failureDetails" -> o.failureDetails),
      ("lastUpdated" -> o.lastUpdated),
      ("uploadFields" -> o.uploadFields.map(_.fields))
    )
  }

  val writes: Writes[UploadJourney] = Json.writes[UploadJourney]

  val reads: Reads[UploadJourney] = (json: JsValue) => {
    for {
      reference <- (json \ "reference").validate[String]
      fileStatus <- (json \ "fileStatus").validateOpt[UploadStatusEnum.Value]
      downloadUrl <- (json \ "downloadUrl").validateOpt[String]
      uploadDetails <- (json \ "uploadDetails").validateOpt[UploadDetails]
      failureDetails <- (json \ "failureDetails").validateOpt[FailureDetails]
      lastUpdated <- (json \ "lastUpdated").validateOpt[LocalDateTime]
      uploadFields <- (json \ "uploadFields").validateOpt[UploadFormFields]
    } yield {
      UploadJourney(
        reference,
        fileStatus.getOrElse(UploadStatusEnum.WAITING),
        downloadUrl,
        uploadDetails,
        failureDetails,
        lastUpdated.fold(LocalDateTime.now)(identity),
        uploadFields
      )
    }
  }

  implicit val format: Format[UploadJourney] = Format(reads, writes)

  val auditWrites: Writes[UploadJourney] = Writes { model =>
    Json.obj(
      Seq[Option[(String, JsValueWrapper)]](
        Some("upscanReference" -> model.reference),
        model.uploadDetails.map(_.uploadTimestamp).map("uploadTimestamp" -> _),
        model.uploadDetails.map(_.fileName).map("fileName" -> _),
        model.uploadDetails.map(_.fileMimeType).map("fileMimeType" -> _),
        model.uploadDetails.map(_.checksum).map("fileChecksum" -> _),
        model.downloadUrl.map("downloadUrl" -> _)
      ).flatten: _*
    )
  }
}
