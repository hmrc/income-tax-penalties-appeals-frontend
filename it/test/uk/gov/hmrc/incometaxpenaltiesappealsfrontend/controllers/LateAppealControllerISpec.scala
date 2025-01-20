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
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.LateAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class LateAppealControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val reasonsList: List[(String, String)] = List(
    ("bereavementReason", "45"),
    ("cessationReason", "30"),
    ("crimeReason", "30"),
    ("fireOrFloodReason", "30"),
    ("healthReason", "30"),
    ("technicalReason", "30"),
    ("unexpectedHospitalReason", "30"),
    ("otherReason", "30")
  )

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  for (reason <- reasonsList) {

    val userAnswersWithReason =
      UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, reason._1)

    s"GET /making-a-late-appeal with ${reason._1}" should {

      testNavBar(url = "/making-a-late-appeal") {
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
      }

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/making-a-late-appeal")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/making-a-late-appeal", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/making-a-late-appeal")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"This penalty point was issued more than ${reason._2} days ago - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe s"This penalty point was issued more than ${reason._2} days ago"
          document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
          document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
          document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/making-a-late-appeal", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe s"This penalty point was issued more than ${reason._2} days ago - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe s"This penalty point was issued more than ${reason._2} days ago"
          document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
          document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
          document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
          document.getSubmitButton.text() shouldBe "Continue"
        }
      }
    }
  }

  "POST /making-a-late-appeal" when {

    val userAnswersWithReason = UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, "bereavementReason")

    "the text area content is valid" should {

      "redirect to the CheckAnswers page" in {

        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

        val result = post("/making-a-late-appeal")(Map(LateAppealForm.key -> "Some reason"))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad().url)
      }
    }

    "the text area content is invalid" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in {

        stubAuth(OK, successfulIndividualAuthResponse)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

        val result = post("/making-a-late-appeal")(Map(LateAppealForm.key -> ""))
        result.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(result.body)

        document.title() should include(LateAppealMessages.English.errorPrefix)
        document.select(".govuk-error-summary__title").text() shouldBe LateAppealMessages.English.thereIsAProblem

        val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
        error1Link.text() shouldBe LateAppealMessages.English.errorRequired
        error1Link.attr("href") shouldBe s"#${LateAppealForm.key}"
      }
    }
  }
}
