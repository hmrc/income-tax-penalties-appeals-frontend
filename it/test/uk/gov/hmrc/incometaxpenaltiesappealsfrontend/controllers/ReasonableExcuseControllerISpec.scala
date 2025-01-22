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

import fixtures.messages.ReasonableExcuseMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ReasonableExcusesForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys.reasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}


class ReasonableExcuseControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]


  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    userAnswersRepo.upsertUserAnswer(UserAnswers(testJourneyId))
    super.beforeEach()
  }

  "GET /reason-for-missing-deadline" should {
    testNavBar("/reason-for-missing-deadline")()

    "return an OK with a view pre-populated" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(
          UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, reasonableExcuse)
        ).futureValue
        val result = get("/reason-for-missing-deadline")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.select(s"#${ReasonableExcusesForm.form}").hasAttr("checked") shouldBe true
        document.select(s"#${ReasonableExcusesForm.form}-2").hasAttr("checked") shouldBe false
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)

        val result = get("/reason-for-missing-deadline", isAgent = true)

        result.status shouldBe OK
      }
    }
    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/reason-for-missing-deadline")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "What was the reason for missing the submission deadline? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
        document.getHintText.get(0).text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your ability to meet the deadline."
        document.getElementsByAttributeValue("for", "reasonableExcuse").text() shouldBe "Bereavement (someone died)"
        document.getElementsByAttributeValue("for", "reasonableExcuse-2").text() shouldBe "Cessation of income source"
        document.getElementsByAttributeValue("for", "reasonableExcuse-3").text() shouldBe "Crime"
        document.getElementsByAttributeValue("for", "reasonableExcuse-4").text() shouldBe "Fire or flood"
        document.getElementsByAttributeValue("for", "reasonableExcuse-5").text() shouldBe "Serious or life-threatening ill health"
        document.getElementsByAttributeValue("for", "reasonableExcuse-6").text() shouldBe "Software or technology issues"
        document.getElementsByAttributeValue("for", "reasonableExcuse-7").text() shouldBe "Unexpected hospital stay"
        document.getElementsByAttributeValue("for", "reasonableExcuse-9").text() shouldBe "The reason does not fit into any of the other categories"
        document.getHintText.get(1).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/reason-for-missing-deadline", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "What was the reason for missing the submission deadline? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "What was the reason for missing the submission deadline?"
        document.getHintText.get(0).text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your client's ability to meet the deadline."
        document.getElementsByAttributeValue("for", "reasonableExcuse").text() shouldBe "Bereavement (someone died)"
        document.getElementsByAttributeValue("for", "reasonableExcuse-2").text() shouldBe "Cessation of income source"
        document.getElementsByAttributeValue("for", "reasonableExcuse-3").text() shouldBe "Crime"
        document.getElementsByAttributeValue("for", "reasonableExcuse-4").text() shouldBe "Fire or flood"
        document.getElementsByAttributeValue("for", "reasonableExcuse-5").text() shouldBe "Serious or life-threatening ill health"
        document.getElementsByAttributeValue("for", "reasonableExcuse-6").text() shouldBe "Software or technology issues"
        document.getElementsByAttributeValue("for", "reasonableExcuse-7").text() shouldBe "Unexpected hospital stay"
        document.getElementsByAttributeValue("for", "reasonableExcuse-9").text() shouldBe "The reason does not fit into any of the other categories"
        document.getHintText.get(1).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }


  "POST /reason-for-missing-deadline" when {

    val userAnswersWithReason = UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, "bereavementReason")

    "a valid radio option has been selected" should {

      "save the value to UserAnswers AND redirect to the Honesty Declaration page" in {

        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
        val result = post("/reason-for-missing-deadline")(Map(ReasonableExcusesForm.key -> "crime"))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.HonestyDeclarationController.onPageLoad().url)

      }
    }

    "the selection for reasonable excuse is invalid" should {

      "render a bad request with the Form Error on the page with a link to the radios in error" in {

        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
        val result = post("/reason-for-missing-deadline")(Map(ReasonableExcusesForm.key -> ""))

        result.status shouldBe BAD_REQUEST
        result.header("Location") shouldBe Some(routes.ReasonableExcuseController.onPageLoad().url)

        val document = Jsoup.parse(result.body)

        document.title() should include(ReasonableExcuseMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe ReasonableExcuseMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe ReasonableExcuseMessages.English.errorRequired
        error1Link.attr("href") shouldBe s"#${ReasonableExcusesForm.form}"
      }
    }
  }

}
