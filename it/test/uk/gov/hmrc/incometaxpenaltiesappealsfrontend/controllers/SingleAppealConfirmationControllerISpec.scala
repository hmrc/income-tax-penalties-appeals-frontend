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

import fixtures.messages.SingleAppealConfirmationMessages
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class SingleAppealConfirmationControllerISpec extends ControllerISpecHelper {

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  class Setup {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue
  }
  
  List(true, false).foreach { isAgent =>
    
    val url = if(isAgent) "/agent-appeal-single-penalty" else "/appeal-single-penalty"

    s"GET $url" should {

      if(!isAgent) {
        testNavBar(url = url)(
          userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs).futureValue
        )
      }

      "return an OK with a view" when {

        "the user is an authorised" in new Setup() {
          stubAuthRequests(isAgent)

          val result: WSResponse = get(url)
          result.status shouldBe OK
        }
      }

      "the journey is for a 1st Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised" in new Setup() {
            stubAuthRequests(isAgent)
            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe SingleAppealConfirmationMessages.English.serviceName
            document.title() shouldBe SingleAppealConfirmationMessages.English.titleWithSuffix(SingleAppealConfirmationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe SingleAppealConfirmationMessages.English.lppCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe SingleAppealConfirmationMessages.English.headingAndTitle
            document.getElementById("whichPenalty").text() shouldBe SingleAppealConfirmationMessages.English.p1_LPP1(multiplePenaltiesModel.firstPenaltyAmount)
            document.getElementById("p2").text() shouldBe SingleAppealConfirmationMessages.English.p2
            document.getSubmitButton.text() shouldBe SingleAppealConfirmationMessages.English.continue
          }
        }
      }

      "the journey is for a 2nd Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised" in new Setup() {
            stubAuthRequests(isAgent)
            userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs2ndStage).futureValue

            val result: WSResponse = get(url)

            val document: nodes.Document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe SingleAppealConfirmationMessages.English.serviceName
            document.title() shouldBe SingleAppealConfirmationMessages.English.titleWithSuffix(SingleAppealConfirmationMessages.English.headingAndTitleReview)
            document.getElementById("captionSpan").text() shouldBe SingleAppealConfirmationMessages.English.lppCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe SingleAppealConfirmationMessages.English.headingAndTitleReview
            document.getElementById("whichPenalty").text() shouldBe SingleAppealConfirmationMessages.English.p1_LPP1Review(multiplePenaltiesModel.firstPenaltyAmount)
            document.getElementById("p2").text() shouldBe SingleAppealConfirmationMessages.English.p2Review
            document.getSubmitButton.text() shouldBe SingleAppealConfirmationMessages.English.continue
          }
        }
      }
    }

    s"POST $url" should {

      "redirect to the Reasonable Excuse page" in {

        stubAuthRequests(isAgent)

        val result = post(url)(Json.obj())

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.ReasonableExcuseController.onPageLoad(isAgent).url)
      }
    }
  }
}
