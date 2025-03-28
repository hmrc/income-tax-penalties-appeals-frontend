/*
 * Copyright 2024 HM Revenue & Customs
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

import fixtures.messages.English
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.ws.WSResponse
import play.api.libs.json.Json
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}


class MultipleAppealsControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  class Setup(isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val otherAnswers: UserAnswers = emptyUserAnswersWithMultipleLPPs
      .setAnswer(ReasonableExcusePage, Other)

    userAnswersRepo.upsertUserAnswer(otherAnswers).futureValue
  }

  "GET /multiple-appeals" should {

    testNavBar("/multiple-appeals")()

    "return an OK with a view" when {
      "the user is an authorised individual" in new Setup() {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result: WSResponse = get("/multiple-appeals")

        result.status shouldBe OK
      }

      "the user is an authorised agent" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)
        val result: WSResponse = get("/multiple-appeals", isAgent = true)

        result.status shouldBe OK
      }
    }

    "the journey is for a 1st Stage Appeal" when {
      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup() {
          stubAuth(OK, successfulIndividualAuthResponse)
          val result: WSResponse = get("/multiple-appeals")

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "The appeal will cover both penalties - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lppCaption(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "The appeal will cover both penalties"
          document.getParagraphs.get(0).text() shouldBe "This allows you to enter appeal details once for penalties linked to the same charge. However, we will still review each penalty separately."
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup() {
          stubAuth(OK, successfulAgentAuthResponse)
          val result: WSResponse = get("/multiple-appeals", isAgent = true)

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "The appeal will cover both penalties - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lppCaption(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "The appeal will cover both penalties"
          document.getParagraphs.get(0).text() shouldBe "This allows you to enter appeal details once for penalties linked to the same charge. However, we will still review each penalty separately."

          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }

    "the journey is for a 2nd Stage Appeal" when {
      "the page has the correct elements" when {
        "the user is an authorised individual" in new Setup() {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

          val result: WSResponse = get("/multiple-appeals")

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "This review will cover both appeal decisions - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lppCaption(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "This review will cover both appeal decisions"
          document.getParagraphs.get(0).text() shouldBe "This allows you to upload evidence once for both reviews. However, we will consider each review separately."
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in new Setup() {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

          val result: WSResponse = get("/multiple-appeals", isAgent = true)

          val document: nodes.Document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "This review will cover both appeal decisions - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lppCaption(
            dateToString(latePaymentAppealData.startDate),
            dateToString(latePaymentAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "This review will cover both appeal decisions"
          document.getParagraphs.get(0).text() shouldBe "This allows you to upload evidence once for both reviews. However, we will consider each review separately."

          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }
  }

  s"POST /multiple-appeals" should {
    "redirect to the Reasonable Excuse page" in {
      stubAuth(OK, successfulIndividualAuthResponse)
      val result = post("/multiple-appeals")(Json.obj())
      result.status shouldBe SEE_OTHER
      result.header("Location") shouldBe Some(routes.ReasonableExcuseController.onPageLoad().url)
    }
  }

}
