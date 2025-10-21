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
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.NormalMode
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString


class MultipleAppealsControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  class Setup {

    deleteAll(userAnswersRepo)

    val otherAnswers: UserAnswers = emptyUserAnswersWithMultipleLPPs
      .setAnswer(ReasonableExcusePage, Other)

    userAnswersRepo.upsertUserAnswer(otherAnswers).futureValue
  }
  
  List(true, false).foreach { isAgent =>
    
    val url = if(isAgent) "/agent-appeal-cover-for-both-penalties" else "/appeal-cover-for-both-penalties"

    s"GET $url" should {

      if(!isAgent) {
        testNavBar(url)()
      }

      "return an OK with a view" when {
        "the user is an authorised" in new Setup {
          stubAuthRequests(isAgent)
          val result: WSResponse = get(url)

          result.status shouldBe OK
        }
      }

      "the journey is for a 1st Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised" in new Setup {
            stubAuthRequests(isAgent)
            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "The appeal will cover both penalties - Manage your Self Assessment - GOV.UK"
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
          "the user is an authorised" in new Setup {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "This review will cover both appeal decisions - Manage your Self Assessment - GOV.UK"
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

    s"POST $url" should {
      "redirect to the Reasonable Excuse page" in {
        stubAuthRequests(isAgent)
        val result = post(url)(Json.obj())
        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.ReasonableExcuseController.onPageLoad(isAgent, NormalMode).url)
      }
    }
  }
}
