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

import fixtures.messages.JointAppealMessages
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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{JointAppealPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils._

class JointAppealControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

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

  "GET /joint-appeal" should {

    testNavBar(url = "/joint-appeal")(
      userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithMultipleLPPs.setAnswer(ReasonableExcusePage, Other)).futureValue
    )

    "return an OK with a view" when {
      "the user is an authorised individual AND the page has already been answered" in new Setup() {
        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(otherAnswers.setAnswer(JointAppealPage, true)).futureValue

        val result: WSResponse = get("/joint-appeal")
        result.status shouldBe OK

        val document: nodes.Document = Jsoup.parse(result.body)
        document.select(s"#${JointAppealForm.key}").hasAttr("checked") shouldBe true
        document.select(s"#${JointAppealForm.key}-2").hasAttr("checked") shouldBe false
      }

      "the user is an authorised agent AND page NOT already answered" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)

        val result: WSResponse = get("/joint-appeal", isAgent = true)
        result.status shouldBe OK

        val document: nodes.Document = Jsoup.parse(result.body)
        document.select(s"#${JointAppealForm.key}").hasAttr("checked") shouldBe false
        document.select(s"#${JointAppealForm.key}-2").hasAttr("checked") shouldBe false
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in new Setup() {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result: WSResponse = get("/joint-appeal")

        val document: nodes.Document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "There are 2 penalties for this overdue tax charge - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
        document.getElementById("paragraph1").text() shouldBe "These are:"
        document.select("#penaltiesList > li:nth-child(1)").text() shouldBe "£101.01 first late payment penalty"
        document.select("#penaltiesList > li:nth-child(2)").text() shouldBe "£101.02 second late payment penalty"
        document.getElementById("paragraph2").text() shouldBe "You can appeal both penalties at the same time if the reason why you did not make the tax payment is the same for each penalty."
        document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
        document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in new Setup() {
        stubAuth(OK, successfulAgentAuthResponse)
        val result: WSResponse = get("/joint-appeal", isAgent = true)

        val document: nodes.Document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "There are 2 penalties for this overdue tax charge - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe JointAppealMessages.English.lppCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe "There are 2 penalties for this overdue tax charge"
        document.getElementById("paragraph1").text() shouldBe "These are:"
        document.select("#penaltiesList > li:nth-child(1)").text() shouldBe "£101.01 first late payment penalty"
        document.select("#penaltiesList > li:nth-child(2)").text() shouldBe "£101.02 second late payment penalty"
        document.getElementById("paragraph2").text() shouldBe "You can appeal both penalties at the same time if the reason why your client did not make the tax payment is the same for each penalty."
        document.getElementsByAttributeValue("for", s"${JointAppealForm.key}").text() shouldBe JointAppealMessages.English.yes
        document.getElementsByAttributeValue("for", s"${JointAppealForm.key}-2").text() shouldBe JointAppealMessages.English.no
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

  "POST /joint-appeal" when {

    "the radio option posted is valid" should {

      "save the value to UserAnswers AND redirect to the MultipleAppeals page if the answer is 'Yes'" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result: WSResponse = post("/joint-appeal")(Map(JointAppealForm.key -> true))

        result.status shouldBe SEE_OTHER
        //TODO: redirect to the MultipleAppeals page
        result.header("Location") shouldBe Some(controllers.routes.ReasonableExcuseController.onPageLoad().url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(JointAppealPage)) shouldBe Some(true)
      }

      "save the value to UserAnswers AND redirect to the SingleAppeal page if the answer is 'No'" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)
        val result: WSResponse = post("/joint-appeal")(Map(JointAppealForm.key -> false))

        result.status shouldBe SEE_OTHER
        //TODO: redirect to the SingleAppeal page
        result.header("Location") shouldBe Some(controllers.routes.ReasonableExcuseController.onPageLoad().url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(JointAppealPage)) shouldBe Some(false)
      }
    }
  }

    "the radio option is invalid" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result: WSResponse = post("/joint-appeal")(Map(JointAppealForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document: nodes.Document = Jsoup.parse(result.body)
        document.title() should include(JointAppealMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe JointAppealMessages.English.thereIsAProblem

        val error1Link: Elements = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe JointAppealMessages.English.errorRequired
        error1Link.attr("href") shouldBe s"#${JointAppealForm.key}"
      }
    }
  }
