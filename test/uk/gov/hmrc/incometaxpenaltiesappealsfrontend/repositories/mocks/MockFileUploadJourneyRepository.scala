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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.mocks

import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.FileUploadJourneyRepository
import uk.gov.hmrc.mongo.cache.CacheItem

import scala.concurrent.Future

trait MockFileUploadJourneyRepository extends MockitoSugar {

  val mockFileUploadJourneyRepository: FileUploadJourneyRepository = mock[FileUploadJourneyRepository]

  def mockUpsertFileUpload(journeyId: String, uploadJourney: UploadJourney)
                          (response: Future[CacheItem]): OngoingStubbing[Future[CacheItem]] =
    when(mockFileUploadJourneyRepository.upsertFileUpload(eqTo(journeyId), eqTo(uploadJourney))).thenReturn(response)

  def mockGetFile(journeyId: String, fileReference: String)
                 (response: Future[Option[UploadJourney]]): OngoingStubbing[Future[Option[UploadJourney]]] =
    when(mockFileUploadJourneyRepository.getFile(eqTo(journeyId), eqTo(fileReference))).thenReturn(response)

  def mockGetAllFiles(journeyId: String)
                     (response: Future[Seq[UploadJourney]]): OngoingStubbing[Future[Seq[UploadJourney]]] =
    when(mockFileUploadJourneyRepository.getAllFiles(eqTo(journeyId))).thenReturn(response)

  def mockRemoveFile(journeyId: String, fileReference: String)
                    (response: Future[Unit]): OngoingStubbing[Future[Unit]] =
    when(mockFileUploadJourneyRepository.removeFile(eqTo(journeyId), eqTo(fileReference))).thenReturn(response)

  def mockRemoveAllFiles(journeyId: String)
                        (response: Future[Unit]): OngoingStubbing[Future[Unit]] =
    when(mockFileUploadJourneyRepository.removeAllFiles(eqTo(journeyId))).thenReturn(response)
}
