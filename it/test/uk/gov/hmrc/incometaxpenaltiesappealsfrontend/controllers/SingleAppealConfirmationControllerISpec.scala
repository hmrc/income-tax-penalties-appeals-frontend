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

import fixtures.messages.LateAppealMessages
import org.jsoup.select.Elements
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.JointAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, SingleAppealConfirmationPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._
import play.api.libs.json.Json

class SingleAppealConfirmationControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  class Setup(isLate: Boolean = false) {

    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue

    val otherAnswers: UserAnswers = emptyUserAnswersWithMultipleLPPs
      .setAnswer(ReasonableExcusePage, Other)

    userAnswersRepo.upsertUserAnswer(otherAnswers).futureValue
  }

  "GET /single-appeal" should {

    testNavBar(url = "/single-appeal")(
      userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs.setAnswer(ReasonableExcusePage, Other)).futureValue
    )

    "return an OK with a view" when {

      "the user is an authorised agent AND page NOT already answered" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)

        val result: WSResponse = get("/single-appeal", isAgent = true)
        result.status shouldBe OK
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in new Setup() {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result: WSResponse = get("/single-appeal")

        val document: nodes.Document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "The appeal will cover a single penalty - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lppCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "The appeal will cover a single penalty"
        document.getElementById("whichPenalty").text() shouldBe "You have chosen to appeal the £101.01 first late payment penalty."
        document.getSubmitButton.text() shouldBe "Accept and continue"
      }

      "the user is an authorised agent" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)
        val result: WSResponse = get("/single-appeal", isAgent = true)

        val document: nodes.Document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "The appeal will cover a single penalty - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lppCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "The appeal will cover a single penalty"
        document.getElementById("whichPenalty").text() shouldBe "You have chosen to appeal the £101.01 first late payment penalty."
        document.getSubmitButton.text() shouldBe "Accept and continue"

      }
    }
  }
    s"POST /single-appeal" should {

      "redirect to the WhenDidEventHappen page and add the Declaration flag to UserAnswers" in {

        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

        val result = post("/single-appeal")(Json.obj())

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.WhenDidEventHappenController.onPageLoad().url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(SingleAppealConfirmationPage)) shouldBe Some(true)
      }
    }
  }
