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

import fixtures.BaseFixtures
import fixtures.messages.WhoPlannedToSubmitMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhoPlannedToSubmitForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.AgentClientEnum
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.WhoPlannedToSubmitPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class WhoPlannedToSubmitControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper with BaseFixtures {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    userAnswersRepo.upsertUserAnswer(emptyUerAnswersWithLSP).futureValue
    super.beforeEach()
  }

  s"GET /who-planned-to-submit" should {

    testNavBar(url = "/who-planned-to-submit")()

    "return an OK with a view pre-populated" when {
      "the user is an authorised agent AND the page has already been answered" in {
        stubAuth(OK, successfulAgentAuthResponse)
        userAnswersRepo.upsertUserAnswer(
          emptyUerAnswersWithLSP.setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
        ).futureValue

        val result = get("/who-planned-to-submit", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.select(s"#${WhoPlannedToSubmitForm.key}").hasAttr("checked") shouldBe true
        document.select(s"#${WhoPlannedToSubmitForm.key}-2").hasAttr("checked") shouldBe false
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)

        val result = get("/who-planned-to-submit", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe WhoPlannedToSubmitMessages.English.serviceName
        document.title() should include(WhoPlannedToSubmitMessages.English.titleAndHeading)
        document.getElementById("captionSpan").text() shouldBe WhoPlannedToSubmitMessages.English.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        )
        document.getH1Elements.text() shouldBe WhoPlannedToSubmitMessages.English.titleAndHeading
        document.getSubmitButton.text() shouldBe WhoPlannedToSubmitMessages.English.continue

        document.select(s"#${WhoPlannedToSubmitForm.key}").hasAttr("checked") shouldBe false
        document.select(s"#${WhoPlannedToSubmitForm.key}-2").hasAttr("checked") shouldBe false
      }
    }
  }

  "POST /who-planned-to-submit" when {

    "a valid radio option has been selected" should {

      "save the value to UserAnswers AND redirect to the ReasonableExcuse page" in {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/who-planned-to-submit")(Map(WhoPlannedToSubmitForm.key -> AgentClientEnum.agent))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.WhatCausedYouToMissDeadlineController.onPageLoad().url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhoPlannedToSubmitPage)) shouldBe Some(AgentClientEnum.agent)
      }
    }

    "the text area content is invalid" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/who-planned-to-submit")(Map(WhoPlannedToSubmitForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(WhoPlannedToSubmitMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe WhoPlannedToSubmitMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe WhoPlannedToSubmitMessages.English.errorRequired
        error1Link.attr("href") shouldBe s"#${WhoPlannedToSubmitForm.key}"
      }
    }
  }
}
