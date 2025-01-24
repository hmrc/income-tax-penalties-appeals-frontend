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
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class CheckYourAnswersControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val bereavementReasonMessage: String = "When did the person die?"
  val cessationReasonMessage: String = "TBC cessationReason"
  val crimeReasonMessage: String = "When did the crime happen?"
  val fireOrFloodReasonReasonMessage: String = "When did the fire or flood happen?"
  val healthReasonMessage: String = "TBC healthReason"
  val technicalReasonMessage: String = "When did the software or technology issues begin?"
  val unexpectedHospitalReasonMessage: String = "TBC unexpectedHospitalReason"
  val otherReasonMessage: String = "TBC otherReason"

  val bereavementReasonValue = "Bereavement (someone died)"
  val cessationReasonValue = "Cessation of income source"
  val crimeReasonValue = "Crime"
  val fireOrFloodReasonValue = "Fire or flood"
  val healthReasonValue = "Serious or life-threatening ill health"
  val technicalReasonValue = "Software or technology issues"
  val unexpectedHospitalReasonValue = "Unexpected hospital stay"
  val otherReasonValue = "The reason does not fit into any of the other categories"

  val reasonsList: List[(String, String, String)] = List(
    ("bereavementReason", bereavementReasonValue, bereavementReasonMessage),
    ("cessationReason", cessationReasonValue, cessationReasonMessage),
    ("crimeReason", crimeReasonValue, crimeReasonMessage),
    ("fireOrFloodReason", fireOrFloodReasonValue, fireOrFloodReasonReasonMessage),
    ("healthReason", healthReasonValue, healthReasonMessage),
    ("technicalReason", technicalReasonValue, technicalReasonMessage),
    ("unexpectedHospitalReason", unexpectedHospitalReasonValue, unexpectedHospitalReasonMessage),
    ("otherReason", otherReasonValue, otherReasonMessage)
  )

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
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
          if(reason._1 == "technicalReason"){
          document.select("#reasonableExcuseDateEnd > dt").text() shouldBe "When did the software or technology issues end?"
          document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__value").text() shouldBe "20 October 2027"
          document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__actions").text() shouldBe "Change When did the software or technology issues end?"
          }
          if(reason._1 == "crimeReason"){
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
          if(reason._1 == "technicalReason"){
            document.select("#reasonableExcuseDateEnd > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__value").text() shouldBe "20 October 2027"
            document.select("#reasonableExcuseDateEnd > dd.govuk-summary-list__actions").text() shouldBe "Change When did the software or technology issues end?"
          }
          if(reason._1 == "crimeReason"){
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

}
