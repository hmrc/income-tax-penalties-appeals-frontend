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

import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.UseStubForBackend
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{HonestyDeclarationPage, ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.{AuthStub, PenaltiesStub}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, IncomeTaxSessionKeys, NavBarTesterHelper, ViewSpecHelper}

import java.time.LocalDate

class CheckYourAnswersControllerISpec extends ComponentSpecHelper with ViewSpecHelper
  with AuthStub with NavBarTesterHelper with PenaltiesStub {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val errorHandler = app.injector.instanceOf[ErrorHandler]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val bereavementMessage: String = "When did the person die?"
  val cessationMessage: String = "TBC cessation"
  val crimeMessage: String = "When did the crime happen?"
  val fireOrFloodReasonMessage: String = "When did the fire or flood happen?"
  val healthMessage: String = "TBC health"
  val technicalIssueMessage: String = "When did the software or technology issues begin?"
  val unexpectedHospitalMessage: String = "TBC unexpectedHospital"
  val otherMessage: String = "TBC other"

  val bereavementValue = "Bereavement (someone died)"
  val cessationValue = "Cessation of income source"
  val crimeValue = "Crime"
  val fireOrFloodValue = "Fire or flood"
  val healthValue = "Serious or life-threatening ill health"
  val technicalIssueValue = "Software or technology issues"
  val unexpectedHospitalValue = "Unexpected hospital stay"
  val otherValue = "The reason does not fit into any of the other categories"

  val reasonsList: List[(String, String, String)] = List(
    ("bereavement", bereavementValue, bereavementMessage),
    ("cessation", cessationValue, cessationMessage),
    ("crime", crimeValue, crimeMessage),
    ("fireOrFlood", fireOrFloodValue, fireOrFloodReasonMessage),
    ("health", healthValue, healthMessage),
    ("technicalIssues", technicalIssueValue, technicalIssueMessage),
    ("unexpectedHospital", unexpectedHospitalValue, unexpectedHospitalMessage),
    ("other", otherValue, otherMessage)
  )

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    disable(UseStubForBackend)
    super.beforeEach()
  }

  for (reason <- reasonsList) {

    val userAnswersWithReason = UserAnswers(testJourneyId).setAnswer(ReasonableExcusePage, reason._1)

    s"GET /check-your-answers with ${reason._1}" should {

      testNavBar(url = "/check-your-answers")(
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
      )

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/check-your-answers")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/check-your-answers", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/check-your-answers")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Check your answers - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe "Check your answers"
          document.getElementById("appealDetails").text() shouldBe "Appeal details"
          document.select("#reasonableExcuse > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select("#reasonableExcuse > dd.govuk-summary-list__value").text() shouldBe reason._2
          document.select("#reasonableExcuse > dd.govuk-summary-list__actions > a").text() shouldBe "Change Reason for missing the submission deadline"
          document.select("#reasonableExcuseDateStart > dt").text() shouldBe reason._3
          document.select("#reasonableExcuseDateStart > dd.govuk-summary-list__value").text() shouldBe "04 October 2027"
          document.select("#reasonableExcuseDateStart > dd.govuk-summary-list__actions > a").text() shouldBe s"Change ${reason._3}"
          if(reason._1 == "technicalIssues"){
            document.select("#reasonableExcuseDateEnd > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__value").text() shouldBe "20 October 2027"
            document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__actions").text() shouldBe "Change When did the software or technology issues end?"
          }
          if(reason._1 == "crime"){
            document.select("#reportedCrime > dt").text() shouldBe "Has this crime been reported to the police?"
            document.select("#reportedCrime > dd.govuk-summary-list__value").text() shouldBe "Yes"
            document.select("#reportedCrime > dd.govuk-summary-list__actions").text() shouldBe "Change Has this crime been reported to the police?"
          }
          document.getElementById("declaration").text() shouldBe "Declaration"
          document.select("#declarationWarn").text() shouldBe "! Warning By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. A false declaration can result in prosecution."
          document.getSubmitButton.text() shouldBe "Accept and send"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/check-your-answers", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Check your answers - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe "Late submission penalty point: 6 July 2027 to 5 October 2027"
          document.getH1Elements.text() shouldBe "Check your answers"
          document.getElementById("appealDetails").text() shouldBe "Appeal details"
          document.select("#reasonableExcuse > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select("#reasonableExcuse > dd.govuk-summary-list__value").text() shouldBe reason._2
          document.select("#reasonableExcuse > dd.govuk-summary-list__actions > a").text() shouldBe "Change Reason for missing the submission deadline"
          document.select("#reasonableExcuseDateStart > dt").text() shouldBe reason._3
          document.select("#reasonableExcuseDateStart > dd.govuk-summary-list__value").text() shouldBe "04 October 2027"
          document.select("#reasonableExcuseDateStart > dd.govuk-summary-list__actions > a").text() shouldBe s"Change ${reason._3}"
          if(reason._1 == "technicalIssues"){
            document.select("#reasonableExcuseDateEnd > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__value").text() shouldBe "20 October 2027"
            document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__actions").text() shouldBe "Change When did the software or technology issues end?"
          }
          if(reason._1 == "crime"){
            document.select("#reportedCrime > dt").text() shouldBe "Has this crime been reported to the police?"
            document.select("#reportedCrime > dd.govuk-summary-list__value").text() shouldBe "Yes"
            document.select("#reportedCrime > dd.govuk-summary-list__actions").text() shouldBe "Change Has this crime been reported to the police?"
          }
          document.getElementById("declaration").text() shouldBe "Declaration"
          document.select("#declarationWarn").text() shouldBe "! Warning By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. A false declaration can result in prosecution."
          document.getSubmitButton.text() shouldBe "Accept and send"
        }
      }
    }
  }

  "POST /check-your-answers" when {
    "the user is an authorised individual" when {
      "the Appeals Submission model can be constructed successfully" when {

        lazy val userAnswers =
          UserAnswers(testJourneyId)
            .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "1")
            .setAnswer(ReasonableExcusePage, "fireOrFlood")
            .setAnswer(HonestyDeclarationPage, true)
            .setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 1, 1))

        "a successful response is returned from the downstream service" should {
          "redirect to the confirmation page" in {
            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

            successfulAppealSubmission(isLPP = false, "1")

            val result = post("/check-your-answers")(Json.obj())

            result.status shouldBe SEE_OTHER
            result.header(LOCATION) shouldBe Some(routes.ConfirmationController.onPageLoad().url)
          }
        }

        "an Error response is returned from the downstream service" should {
          "render an ISE" in {
            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

            failedAppealSubmission(isLPP = false, "1")

            val result = post("/check-your-answers")(Json.obj())

            result.status shouldBe INTERNAL_SERVER_ERROR
            result.body should include("Sorry, there is a problem with the service")
          }
        }
      }

      "the Appeals Submission model can NOT be constructed successfully" when {

        lazy val userAnswers =
          UserAnswers(testJourneyId)
            .setAnswerForKey[String](IncomeTaxSessionKeys.penaltyNumber, "1")
            .setAnswer(HonestyDeclarationPage, true)
            .setAnswer(WhenDidEventHappenPage, LocalDate.of(2024, 1, 1))

        "render an ISE" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswers).futureValue

          val result = post("/check-your-answers")(Json.obj())

          result.status shouldBe INTERNAL_SERVER_ERROR
          result.body should include("Sorry, there is a problem with the service")
        }
      }
    }
  }
}
