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

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.mongo.cache.{CacheIdType, CacheItem, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileUploadJourneyRepository @Inject()(mongoComponent: MongoComponent,
                                            timestampSupport: TimestampSupport,
                                            appConfig: AppConfig)(implicit ec: ExecutionContext)
  extends MongoCacheRepository[String](
    mongoComponent = mongoComponent,
    collectionName = "file-upload-journeys",
    replaceIndexes = true,
    ttl = appConfig.mongoTTL,
    timestampSupport = timestampSupport,
    cacheIdType = CacheIdType.SimpleCacheId
  )(ec) {

  def upsertFileUpload(journeyId: String, uploadJourney: UploadJourney): Future[CacheItem] =
    put(journeyId)(DataKey(uploadJourney.reference), uploadJourney)

  def getAllFiles(journeyId: String): Future[Seq[UploadJourney]] =
    findById(journeyId).map {
      _.fold[Seq[UploadJourney]](Seq.empty)(_.data.values.map(_.as[UploadJourney]).toSeq)
    }

  def getFile(journeyId: String, fileReference: String): Future[Option[UploadJourney]] =
    get[UploadJourney](journeyId)(DataKey(fileReference))

  def removeFile(journeyId: String, fileReference: String): Future[Unit] =
    delete(journeyId)(DataKey(fileReference))

  def removeAllFiles(journeyId: String): Future[Unit] =
    deleteEntity(journeyId)
}
