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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories

import fixtures.FileUploadFixtures
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{FailureReasonEnum, UploadStatus, UploadStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.ComponentSpecHelper

class FileUploadJourneyRepositoryISpec extends ComponentSpecHelper with FileUploadFixtures {

  lazy val repository: FileUploadJourneyRepository = injector.instanceOf[FileUploadJourneyRepository]

  override def beforeEach(): Unit = {
    await(deleteAll(repository))
    super.beforeEach()
  }

  "calling .upsertFileUpload()" should {
    "insert an entry when a document does not exist" in {
      await(repository.upsertFileUpload(testJourneyId, waitingFile))
      await(repository.getFile(testJourneyId, fileRef1)) shouldBe Some(waitingFile)
    }

    "update an entry when a document does already exist" in {
      await(repository.upsertFileUpload(testJourneyId, waitingFile))
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.getFile(testJourneyId, fileRef1)) shouldBe Some(callbackModel)
    }
  }


  ".calling .getFormFieldsForFile()" should {

    s"return $None when the uploadDetails does not exist" in {
      await(repository.getFormFieldsForFile(testJourneyId, fileRef1)) shouldBe None
    }

    s"return $Some upload fields when the uploadDetails exists" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.getFormFieldsForFile(testJourneyId, fileRef1)) shouldBe callbackModel.uploadFields
    }
  }


  "calling .getStatusOfFileUpload()" should {

    s"return $None when the document is not in Mongo" in {
      await(repository.getStatusOfFileUpload(testJourneyId, "")) shouldBe None
    }

    s"return $Some when the document is in Mongo" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.getStatusOfFileUpload(testJourneyId, fileRef1)) shouldBe
        Some(UploadStatus(UploadStatusEnum.READY.toString))
    }

    s"return $Some when the document is in Mongo (failed upload)" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModelFailed))
      await(repository.getStatusOfFileUpload(testJourneyId, fileRef1)) shouldBe
        Some(UploadStatus(FailureReasonEnum.QUARANTINE.toString, Some(testVirusMessage)))
    }
  }


  "calling .getAllFiles()" should {

    s"return empty sequence when the document is not in Mongo" in {
      await(repository.getAllFiles(testJourneyId)) shouldBe Seq()
    }

    s"return a sequence of files when files exist in Mongo" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2))
      await(repository.getAllFiles(testJourneyId)) shouldBe Seq(callbackModel, callbackModel2)
    }
  }


  "calling .getNumberOfReadyFiles()" should {

    "return 0 when there is no uploads for the journey" in {
      await(repository.getNumberOfReadyFiles(testJourneyId)) shouldBe 0
    }

    "return 0 when there is no uploads that are READY for the journey" in {
      await(repository.upsertFileUpload(testJourneyId, waitingFile))
      await(repository.getNumberOfReadyFiles(testJourneyId)) shouldBe 0
    }

    "return a count of all READY uploads when there are some" in {
      await(repository.upsertFileUpload(testJourneyId, waitingFile))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2.copy(reference = "ref3")))
      await(repository.upsertFileUpload(testJourneyId, waitingFile.copy(reference = "ref4")))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 4
      await(repository.getNumberOfReadyFiles(testJourneyId)) shouldBe 2
    }
  }


  "calling .removeFileForJourney()" should {

    "remove the file in the journey if it exists" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 2
      await(repository.removeFile(testJourneyId, fileRef1))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 1
      await(repository.getAllFiles(testJourneyId)).headOption shouldBe Some(callbackModel2)
    }

    "do not remove the file in the journey if the file specified doesn't exist" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 2
      await(repository.removeFile(testJourneyId, "ref1234"))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 2
    }

    "do not remove the file in the journey if the journey specified doesn't exist" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 2
      await(repository.removeFile("1235", fileRef1))
      await(repository.getTotalNumberOfFiles(testJourneyId)) shouldBe 2
    }
  }


  "calling .getFile()" should {

    "return a upload journey model when the file reference exists" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.getFile(testJourneyId, fileRef1)) shouldBe Some(callbackModel)
    }

    "return nothing when the file reference does not exist" in {
      await(repository.getFile(testJourneyId, "ref1234")) shouldBe None
    }
  }


  "calling .removeAllFiles()" should {

    "remove all files for the specified journey ID" in {
      await(repository.upsertFileUpload(testJourneyId, callbackModel))
      await(repository.upsertFileUpload(testJourneyId, callbackModel2))
      await(repository.upsertFileUpload("12345", callbackModel))
      await(repository.collection.countDocuments().toFuture()) shouldBe 2
      await(repository.removeAllFiles(testJourneyId))
      await(repository.collection.countDocuments().toFuture()) shouldBe 1
    }
  }
}