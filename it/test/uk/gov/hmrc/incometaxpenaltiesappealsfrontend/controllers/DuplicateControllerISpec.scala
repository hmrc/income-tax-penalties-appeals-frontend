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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Bereavement
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository

class DuplicateControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Bereavement)).futureValue
    super.beforeEach()
  }

  "GET /duplicate-appeal " should {

    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        val result = get("/duplicate-appeal")

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuthRequests(true)
        val result = get("/agent-duplicate-appeal", isAgent = true)

        result.status shouldBe OK
      }
    }
    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

        val result = get("/duplicate-appeal")

        val document = Jsoup.parse(result.body)

        document.getElementById("duplicatedAppealParagraph").text() shouldBe "You can't submit more than one appeal for the same penalty at the same time."
        document.getElementById("duplicateAppeal-link").text() shouldBe "Back to Self Assessment penalties and appeals"
      }
    }
    //nimo

    "the user is an authorised agent" in {
      stubAuthRequests(true)
      userAnswersRepo.upsertUserAnswer(emptyUserAnswers).futureValue

      val result = get("/agent-duplicate-appeal", isAgent = true)

      val document = Jsoup.parse(result.body)

      document.getElementById("duplicatedAppealParagraph").text() shouldBe "You can't submit more than one appeal for the same penalty at the same time."
      document.getElementById("duplicateAppeal-link").text() shouldBe "Back to Self Assessment penalties and appeals"
    }
  }
  }