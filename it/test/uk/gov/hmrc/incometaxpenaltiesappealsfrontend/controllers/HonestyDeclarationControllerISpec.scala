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

import fixtures.messages.English
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{HonestyDeclarationPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class HonestyDeclarationControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub with NavBarTesterHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo = app.injector.instanceOf[UserAnswersRepository]

  val dueDate = dateToString(lateSubmissionAppealData.dueDate).replace("\u00A0", " ")

  val bereavementMessage: String = s"because I was affected by someone’s death, I was unable to send the submission due on $dueDate"
  val cessationMessage: String = s"TBC cessation - I was unable to send the submission due on $dueDate"
  val crimeMessage: String = s"because I was affected by a crime, I was unable to send the submission due on $dueDate"
  val fireOrFloodReasonMessage: String = s"because of a fire or flood, I was unable to send the submission due on $dueDate"
  val healthMessage: String = s"TBC health - I was unable to send the submission due on $dueDate"
  val technicalIssueMessage: String = s"because of software or technology issues, I was unable to send the submission due on $dueDate"
  val unexpectedHospitalMessage: String = s"TBC unexpectedHospital - I was unable to send the submission due on $dueDate"
  val otherMessage: String = s"TBC other - I was unable to send the submission due on $dueDate"

  val reasonsList: List[(ReasonableExcuse, String)]= List(
    (Bereavement, bereavementMessage),
    (Cessation, cessationMessage),
    (Crime, crimeMessage),
    (FireOrFlood, fireOrFloodReasonMessage),
    (Health, healthMessage),
    (TechnicalIssues, technicalIssueMessage),
    (UnexpectedHospital, unexpectedHospitalMessage),
    (Other, otherMessage)
  )

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  for(reason <- reasonsList) {

    val userAnswersWithReason =
      emptyUerAnswersWithLSP.setAnswer(ReasonableExcusePage, reason._1)

    s"GET /honesty-declaration with ${reason._1}" should {

      testNavBar(url = "/honesty-declaration") {
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
      }

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/honesty-declaration")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/honesty-declaration", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/honesty-declaration")

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Honesty declaration - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "Honesty declaration"
          document.getElementById("honestyDeclarationConfirm").text() shouldBe "I confirm that:"
          document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
          document.getElementById("honestyDeclaration").text() shouldBe "I will provide honest and accurate information in this appeal"
          document.getSubmitButton.text() shouldBe "Accept and continue"
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

          val result = get("/honesty-declaration", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
          document.title() shouldBe "Honesty declaration - Appeal a Self Assessment penalty - GOV.UK"
          document.getElementById("captionSpan").text() shouldBe English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe "Honesty declaration"
          document.getElementById("honestyDeclarationConfirm").text() shouldBe "I confirm that:"
          document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
          document.getElementById("honestyDeclaration").text() shouldBe "I will provide honest and accurate information in this appeal"
          document.getSubmitButton.text() shouldBe "Accept and continue"
        }
      }
    }
  }

  s"POST /honesty-declaration" should {

    "redirect to the WhenDidEventHappen page and add the Declaration flag to UserAnswers" in {

      stubAuth(OK, successfulIndividualAuthResponse)
      userAnswersRepo.upsertUserAnswer(emptyUerAnswersWithLSP).futureValue

      val result = post("/honesty-declaration")(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header("Location") shouldBe Some(routes.WhenDidEventHappenController.onPageLoad().url)

      userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(HonestyDeclarationPage)) shouldBe Some(true)
    }
  }
}
