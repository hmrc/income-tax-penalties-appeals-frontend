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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.ComponentSpecHelper

class FileUploadJourneyRepositoryISpec extends ComponentSpecHelper with FileUploadFixtures {

  lazy val repository: FileUploadJourneyRepository = injector.instanceOf[FileUploadJourneyRepository]

  override def beforeEach(): Unit = {
    deleteAll(repository.mongo)
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
      await(repository.mongo.collection.countDocuments().toFuture()) shouldBe 2
      await(repository.removeAllFiles(testJourneyId))
      await(repository.mongo.collection.countDocuments().toFuture()) shouldBe 1
    }
  }
}