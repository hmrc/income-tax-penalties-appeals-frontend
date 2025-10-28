/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileUploadJourneyRepository @Inject()(val mongo: MongoFileUploadJourneyConnection)(implicit ec: ExecutionContext) {

  def upsertFileUpload(journeyId: String, uploadJourney: UploadJourney): Future[CacheItem] =
    mongo.put(journeyId)(DataKey(uploadJourney.reference), uploadJourney)

  def getAllFiles(journeyId: String): Future[Seq[UploadJourney]] =
    mongo.findById(journeyId).map {
      _.fold[Seq[UploadJourney]](Seq.empty)(_.data.values.map(_.as[UploadJourney]).toSeq)
    }

  def getFile(journeyId: String, fileReference: String): Future[Option[UploadJourney]] =
    mongo.get[UploadJourney](journeyId)(DataKey(fileReference))

  def removeFile(journeyId: String, fileReference: String): Future[Unit] =
    mongo.delete(journeyId)(DataKey(fileReference))

  def removeAllFiles(journeyId: String): Future[Unit] =
    mongo.deleteEntity(journeyId)
}
