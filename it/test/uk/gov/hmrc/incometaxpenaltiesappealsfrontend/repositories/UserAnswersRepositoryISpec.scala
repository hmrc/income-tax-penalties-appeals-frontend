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

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.ComponentSpecHelper

class UserAnswersRepositoryISpec extends ComponentSpecHelper {

  lazy val repository: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]

  class Setup {
    deleteAll(repository)
  }

  val userAnswers: UserAnswers = UserAnswers(journeyId = "journey123", data = Json.obj("key1" -> "value1", "key2" -> "value2"))
  val userAnswers2: UserAnswers = userAnswers.copy(journeyId = "journey456")
  val userAnswers3: UserAnswers = userAnswers.copy(journeyId = "journey789")

  "upsertUserAnswer" should {
    "insert userAnswer payload when there is no duplicate keys" in new Setup {
      val result: Boolean = await(repository.upsertUserAnswer(userAnswers))
      result shouldBe true

      val recordsInMongoAfterInsertion: Seq[UserAnswers] = await(repository.collection.find().toFuture())
      recordsInMongoAfterInsertion.size shouldBe 1
      recordsInMongoAfterInsertion.head.journeyId shouldBe userAnswers.journeyId
      recordsInMongoAfterInsertion.head.data shouldBe userAnswers.data
    }

    "update userAnswer payload when there IS duplicate key" in new Setup {
      val duplicateUserAnswer: UserAnswers = userAnswers.copy(data = Json.obj(
        "key12" -> "value12",
        "key23" -> "value23"
      ))
      await(repository.upsertUserAnswer(userAnswers))
      await(repository.upsertUserAnswer(duplicateUserAnswer))
      val recordsInMongoAfterUpdate: Seq[UserAnswers] = await(repository.collection.find().toFuture())
      recordsInMongoAfterUpdate.size shouldBe 1
      recordsInMongoAfterUpdate.head.journeyId shouldBe duplicateUserAnswer.journeyId
      recordsInMongoAfterUpdate.head.data shouldBe duplicateUserAnswer.data
    }
  }

  "getUserAnswer" should {
    s"return a $UserAnswers when there is a pre-existing record under the journeyId" in new Setup {
      await(repository.upsertUserAnswer(userAnswers))
      val recordsInMongoAfterInsertion: Seq[UserAnswers] = await(repository.collection.find().toFuture())
      recordsInMongoAfterInsertion.size shouldBe 1
      recordsInMongoAfterInsertion.head.journeyId shouldBe userAnswers.journeyId
      recordsInMongoAfterInsertion.head.data shouldBe userAnswers.data


      val getResult: Option[UserAnswers] = await(repository.getUserAnswer("journey123"))
      getResult.isDefined shouldBe true
      getResult.get.journeyId shouldBe userAnswers.journeyId
      getResult.get.data shouldBe userAnswers.data
    }

    s"return $None when there is NO pre-existing record under the journeyId" in new Setup {
      val getResult: Option[UserAnswers] = await(repository.getUserAnswer("journey123"))
      getResult.isEmpty shouldBe true
    }
  }

  "removeUserAnswers" should {
    "remove all the user answers for the specified journey ID" in new Setup {
      await(repository.upsertUserAnswer(userAnswers))
      await(repository.upsertUserAnswer(userAnswers2))
      await(repository.collection.find().toFuture()).size shouldBe 2
      await(repository.removeUserAnswers("journey123"))
      await(repository.getUserAnswer("journey456")).isDefined shouldBe true
    }
  }
}
