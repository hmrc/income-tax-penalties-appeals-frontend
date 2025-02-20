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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class ViewAppealDetailsControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

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

  val reasonsList: List[(ReasonableExcuse, String, String)] = List(
    (Bereavement, bereavementValue, bereavementMessage),
    (Cessation, cessationValue, cessationMessage),
    (Crime, crimeValue, crimeMessage),
    (FireOrFlood, fireOrFloodValue, fireOrFloodReasonMessage),
    (Health, healthValue, healthMessage),
    (TechnicalIssues, technicalIssueValue, technicalIssueMessage),
    (UnexpectedHospital, unexpectedHospitalValue, unexpectedHospitalMessage),
    (Other, otherValue, otherMessage)
  )

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  for (reason <- reasonsList) {

    val userAnswersWithReason = emptyUerAnswersWithLSP.setAnswer(ReasonableExcusePage, reason._1)

    s"GET /appeal-details with ${reason._1}" should {

      testNavBar(url = "/appeal-details")(
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
      )

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/appeal-details")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/appeal-details", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/appeal-details")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Appeal details - Appeal a Self Assessment penalty - GOV.UK"
          document.getH1Elements.text() shouldBe "Appeal details"
          document.select("#nationalInsurance > dt").text() shouldBe "National Insurance number"
          document.select("#nationalInsurance > dd").text() shouldBe "QQ123456B"
          document.select("#penaltyAppealed > dt").text() shouldBe "Penalty appealed"
          document.select("#penaltyAppealed > dd").text() shouldBe "Late payment penalty: 2027 to 2028 tax year"
          document.select("#appealDate > dt").text() shouldBe "Appeal date"
          document.select("#appealDate > dd").text() shouldBe "17 March 2029"
          document.select("#multiplePenalties > dt").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.select("#multiplePenalties > dd").text() shouldBe "No"
          document.select("#penaltyReason > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select("#penaltyReason > dd").text() shouldBe reason._2
          document.select("#startDate > dt").text() shouldBe reason._3
          document.select("#startDate > dd").text() shouldBe "20 January 2029"
          if(reason._1 == TechnicalIssues){
            document.select("#endDate > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#endDate > dd").text() shouldBe "20 February 2029"
          }
          if(reason._1 == Crime){
            document.select("#reportedCrime > dt").text() shouldBe "Has this crime been reported to the police?"
            document.select("#reportedCrime > dd").text() shouldBe "Yes"
          }
          document.select("#printWarn > p").text() shouldBe "Print or download this page if you want to keep it. You will not be able to return to these appeal details later."
          document.getElementById("print-button").text shouldBe "Print this page"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/appeal-details", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Appeal details - Appeal a Self Assessment penalty - GOV.UK"
          document.getH1Elements.text() shouldBe "Appeal details"
          document.select("#nationalInsurance > dt").text() shouldBe "National Insurance number"
          document.select("#nationalInsurance > dd").text() shouldBe "QQ123456B"
          document.select("#penaltyAppealed > dt").text() shouldBe "Penalty appealed"
          document.select("#penaltyAppealed > dd").text() shouldBe "Late payment penalty: 2027 to 2028 tax year"
          document.select("#appealDate > dt").text() shouldBe "Appeal date"
          document.select("#appealDate > dd").text() shouldBe "17 March 2029"
          document.select("#whoPlanned > dt").text() shouldBe "Before the deadline, who planned to send the submission?"
          document.select("#whoPlanned > dd").text() shouldBe "I did"
          document.select("#multiplePenalties > dt").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
          document.select("#multiplePenalties > dd").text() shouldBe "No"
          document.select("#penaltyReason > dt").text() shouldBe "Reason for missing the submission deadline"
          document.select("#penaltyReason > dd").text() shouldBe reason._2
          document.select("#startDate > dt").text() shouldBe reason._3
          document.select("#startDate > dd").text() shouldBe "20 January 2029"
          if(reason._1 == TechnicalIssues){
            document.select("#endDate > dt").text() shouldBe "When did the software or technology issues end?"
            document.select("#endDate > dd").text() shouldBe "20 February 2029"
          }
          if(reason._1 == Crime){
            document.select("#reportedCrime > dt").text() shouldBe "Has this crime been reported to the police?"
            document.select("#reportedCrime > dd").text() shouldBe "Yes"
          }
          document.select("#printWarn > p").text() shouldBe "Print or download this page if you want to keep it. You will not be able to return to these appeal details later."
          document.getElementById("print-button").text shouldBe "Print this page"
        }
      }
    }
  }

}
