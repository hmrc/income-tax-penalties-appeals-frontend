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

import fixtures.messages.CrimeReportedMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.CrimeReportedForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CrimeReportedEnum
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{CrimeReportedPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class CrimeReportedControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val crimeReasonAnswers: UserAnswers = UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, "crimeReason")

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    userAnswersRepo.upsertUserAnswer(crimeReasonAnswers).futureValue
    super.beforeEach()
  }

  "GET /has-this-crime-been-reported" should {

    testNavBar(url = "/has-this-crime-been-reported")()

    "return an OK with a view" when {
      "the user is an authorised individual AND the page has already been answered" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(crimeReasonAnswers.setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)).futureValue

        val result = get("/has-this-crime-been-reported")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.select(s"#${CrimeReportedForm.key}").hasAttr("checked") shouldBe true
        document.select(s"#${CrimeReportedForm.key}-2").hasAttr("checked") shouldBe false
        document.select(s"#${CrimeReportedForm.key}-3").hasAttr("checked") shouldBe false
      }

      "the user is an authorised agent AND page NOT already answered" in {
        stubAuth(OK, successfulAgentAuthResponse)

        val result = get("/has-this-crime-been-reported", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.select(s"#${CrimeReportedForm.key}").hasAttr("checked") shouldBe false
        document.select(s"#${CrimeReportedForm.key}-2").hasAttr("checked") shouldBe false
        document.select(s"#${CrimeReportedForm.key}-3").hasAttr("checked") shouldBe false
      }
    }

    "the page has the correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/has-this-crime-been-reported")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Has this crime been reported to the police? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Has this crime been reported to the police?"
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}").text() shouldBe CrimeReportedMessages.English.yes
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}-2").text() shouldBe CrimeReportedMessages.English.no
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}-3").text() shouldBe CrimeReportedMessages.English.unkownOption
        document.getSubmitButton.text() shouldBe "Continue"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/has-this-crime-been-reported", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
        document.title() shouldBe "Has this crime been reported to the police? - Appeal a Self Assessment penalty - GOV.UK"
        document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
        document.getH1Elements.text() shouldBe "Has this crime been reported to the police?"
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}").text() shouldBe CrimeReportedMessages.English.yes
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}-2").text() shouldBe CrimeReportedMessages.English.no
        document.getElementsByAttributeValue("for", s"${CrimeReportedForm.key}-3").text() shouldBe CrimeReportedMessages.English.unkownOption
        document.getSubmitButton.text() shouldBe "Continue"

      }
    }
  }

  "POST /has-this-crime-been-reported" when {

    "the radio option posted is valid" should {

      "save the value to UserAnswers AND redirect to the LateAppeal page" in {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/has-this-crime-been-reported")(Map(CrimeReportedForm.key -> CrimeReportedEnum.yes))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad().url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(CrimeReportedPage)) shouldBe Some(CrimeReportedEnum.yes)
      }
    }

    "the radio option is invalid" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in {

        stubAuth(OK, successfulIndividualAuthResponse)

        val result = post("/has-this-crime-been-reported")(Map(CrimeReportedForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)
        document.title() should include(CrimeReportedMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe CrimeReportedMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe CrimeReportedMessages.English.errorRequired
        error1Link.attr("href") shouldBe s"#${CrimeReportedForm.key}"
      }
    }
  }

}
