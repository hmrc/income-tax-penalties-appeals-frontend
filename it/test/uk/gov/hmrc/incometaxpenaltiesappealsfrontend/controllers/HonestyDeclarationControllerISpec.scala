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

import fixtures.messages.{English, HonestyDeclarationMessages}
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

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val reasonsList: List[(ReasonableExcuse, String, String)]= List(
  (Bereavement, HonestyDeclarationMessages.English.bereavementMessageLSP, HonestyDeclarationMessages.English.bereavementMessageLPP),
  (Cessation, HonestyDeclarationMessages.English.cessationMessageLSP, HonestyDeclarationMessages.English.cessationMessageLPP),
  (Crime, HonestyDeclarationMessages.English.crimeMessageLSP, HonestyDeclarationMessages.English.crimeMessageLPP),
  (FireOrFlood, HonestyDeclarationMessages.English.fireOrFloodReasonMessageLSP, HonestyDeclarationMessages.English.fireOrFloodReasonMessageLPP),
  (Health, HonestyDeclarationMessages.English.healthMessageLSP, HonestyDeclarationMessages.English.healthMessageLPP),
  (TechnicalIssues, HonestyDeclarationMessages.English.technicalIssueMessageLSP, HonestyDeclarationMessages.English.technicalIssueMessageLPP),
  (UnexpectedHospital, HonestyDeclarationMessages.English.unexpectedHospitalMessageLSP, HonestyDeclarationMessages.English.unexpectedHospitalMessageLPP),
  (Other, HonestyDeclarationMessages.English.otherMessageLSP, HonestyDeclarationMessages.English.otherMessageLPP)
  )

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  for(reason <- reasonsList) {

    val userAnswersWithReasonLSP =
      emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, reason._1)

    val userAnswersWithReasonLPP =
      emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, reason._1)

    val userAnswersWithReason2ndStage =
      emptyUserAnswersWithLSP2ndStage.setAnswer(ReasonableExcusePage, reason._1)

    s"GET /honesty-declaration with ${reason._1}" should {

      testNavBar(url = "/honesty-declaration") {
        userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue
      }

      "return an OK with a view" when {
        "the user is an authorised individual" in {
          stubAuth(OK, successfulIndividualAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

          val result = get("/honesty-declaration")

          result.status shouldBe OK
        }

        "the user is an authorised agent" in {
          stubAuth(OK, successfulAgentAuthResponse)
          userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

          val result = get("/honesty-declaration", isAgent = true)

          result.status shouldBe OK
        }
      }

      "the journey is for a 1st Stage Appeal" when {
        "the page has the correct LSP elements" when {
          "the user is an authorised individual" in {
            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

            val result = get("/honesty-declaration")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            else if(reason._1 == UnexpectedHospital){document.getElementById("honestyDeclarationHospital").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHospital}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent" in {
            stubAuth(OK, successfulAgentAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

            val result = get("/honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._2
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            else if(reason._1 == UnexpectedHospital){document.getElementById("honestyDeclarationHospital").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHospital}
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }
        }

        "the page has the correct LPP elements" when {
          "the user is an authorised individual" in {
            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLPP).futureValue

            val result = get("/honesty-declaration")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclarationReason").text() shouldBe reason._3
            if(reason._1 == Health){document.getElementById("honestyDeclarationHealth").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHealth}
            else if(reason._1 == UnexpectedHospital){document.getElementById("honestyDeclarationHospital").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationHospital}
            document.getElementById("honestyDeclarationLPP").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationLPP
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfo
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }
        }

      }

      "the journey is for a 2nd Stage Appeal" when {
        "the page has the correct elements" when {
          "the user is an authorised individual" in {
            stubAuth(OK, successfulIndividualAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason2ndStage).futureValue

            val result = get("/honesty-declaration")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfoReview
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }

          "the user is an authorised agent" in {
            stubAuth(OK, successfulAgentAuthResponse)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReason2ndStage).futureValue

            val result = get("/honesty-declaration", isAgent = true)

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe HonestyDeclarationMessages.English.serviceName
            document.title() shouldBe HonestyDeclarationMessages.English.titleWithSuffix(HonestyDeclarationMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe HonestyDeclarationMessages.English.headingAndTitle
            document.getElementById("honestyDeclarationConfirm").text() shouldBe HonestyDeclarationMessages.English.confirmParagraph
            document.getElementById("honestyDeclaration").text() shouldBe HonestyDeclarationMessages.English.honestyDeclarationInfoReview
            document.getSubmitButton.text() shouldBe HonestyDeclarationMessages.English.acceptAndContinue
          }
        }
      }
    }
  }

  s"POST /honesty-declaration" should {

    "redirect to the WhenDidEventHappen page and add the Declaration flag to UserAnswers" in {

      stubAuth(OK, successfulIndividualAuthResponse)
      userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue

      val result = post("/honesty-declaration")(Json.obj())

      result.status shouldBe SEE_OTHER
      result.header("Location") shouldBe Some(routes.WhenDidEventHappenController.onPageLoad().url)

      userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(HonestyDeclarationPage)) shouldBe Some(true)
    }
  }
}
