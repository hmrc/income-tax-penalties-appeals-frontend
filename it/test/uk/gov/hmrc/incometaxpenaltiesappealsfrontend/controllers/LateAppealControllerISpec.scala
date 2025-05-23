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

import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import fixtures.messages.LateAppealMessages
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.LateAppealForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{JointAppealPage, LateAppealPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class LateAppealControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  val reasonsList: List[(ReasonableExcuse, Int)] = List(
    (Bereavement, appConfig.bereavementLateDays),
    (Cessation, appConfig.lateDays),
    (Crime, appConfig.lateDays),
    (FireOrFlood, appConfig.lateDays),
    (Health, appConfig.lateDays),
    (TechnicalIssues, appConfig.lateDays),
    (UnexpectedHospital, appConfig.lateDays),
    (Other, appConfig.lateDays)
  )

  override def beforeEach(): Unit = {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    super.beforeEach()
  }

  for (reason <- reasonsList) {

    val userAnswersWithReasonLSP =
      emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, reason._1)

    val userAnswersWithReasonLPP =
      emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, reason._1)

    val userAnswersWithReasonMultipleLPP =
      emptyUserAnswersWithMultipleLPPs
        .setAnswer(ReasonableExcusePage, reason._1)
        .setAnswer(JointAppealPage, true)

    val userAnswersWithReasonLSPSecondStage =
      emptyUserAnswersWithLSP2ndStage.setAnswer(ReasonableExcusePage, reason._1)

    val userAnswersWithSingleLPPsSecondStage =
      emptyUserAnswersWithMultipleLPPs2ndStage
        .setAnswer(ReasonableExcusePage, reason._1)
        .setAnswer(JointAppealPage, false)

    val userAnswersWithMultipleLPPsSecondStage =
      emptyUserAnswersWithMultipleLPPs2ndStage
        .setAnswer(ReasonableExcusePage, reason._1)
        .setAnswer(JointAppealPage, true)

    s"GET /making-a-late-appeal with ${reason._1}" should {

      testNavBar(url = "/making-a-late-appeal") {
        userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue
      }

      "return an OK with a view pre-populated" when {
        "the user is an authorised individual AND the page has already been answered" in {
          stubAuthRequests(false)
          userAnswersRepo.upsertUserAnswer(
            userAnswersWithReasonLSP.setAnswer(LateAppealPage, "Some reason")
          ).futureValue

          val result = get("/making-a-late-appeal")
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.select(s"#${LateAppealForm.key}").text() shouldBe "Some reason"
        }

        "the user is an authorised agent AND the page has already been answered" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(
            userAnswersWithReasonLSP.setAnswer(LateAppealPage, "Some reason")
          ).futureValue

          val result = get("/agent-making-a-late-appeal", isAgent = true)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.select(s"#${LateAppealForm.key}").text() shouldBe "Some reason"
        }
      }

      "the journey is for a 1st Stage Appeal" when {
        "the penalty type is LSP" when {
          "the page has the correct elements" when {
            "the user is an authorised individual" in {
              stubAuthRequests(false)
              userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

              val result = get("/making-a-late-appeal")

              val document = Jsoup.parse(result.body)

              document.getServiceName.text() shouldBe "Manage your Self Assessment"
              document.title() shouldBe s"This penalty point was issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
              document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lspCaption(
                dateToString(lateSubmissionAppealData.startDate),
                dateToString(lateSubmissionAppealData.endDate)
              )
              document.getH1Elements.text() shouldBe s"This penalty point was issued more than ${reason._2} days ago"
              document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
              document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
              document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
              document.getSubmitButton.text() shouldBe "Continue"
            }

            "the user is an authorised agent" in {
              stubAuthRequests(true)
              userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSP).futureValue

              val result = get("/agent-making-a-late-appeal", isAgent = true)

              val document = Jsoup.parse(result.body)

              document.getServiceName.text() shouldBe "Manage your Self Assessment"
              document.title() shouldBe s"This penalty point was issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
              document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lspCaption(
                dateToString(lateSubmissionAppealData.startDate),
                dateToString(lateSubmissionAppealData.endDate)
              )
              document.getH1Elements.text() shouldBe s"This penalty point was issued more than ${reason._2} days ago"
              document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
              document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
              document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
              document.getSubmitButton.text() shouldBe "Continue"
            }
          }
        }

        "the penalty type is LPP - Single Appeal" when {
          "the page has the correct elements" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLPP).futureValue

            val result = get("/making-a-late-appeal")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe s"This penalty was issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe s"This penalty was issued more than ${reason._2} days ago"
            document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
            document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
            document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
            document.getSubmitButton.text() shouldBe "Continue"

          }
        }

        "the penalty type is LPP - Multiple Appeals" when {
          "the page has the correct elements" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithReasonMultipleLPP).futureValue

            val result = get("/making-a-late-appeal")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe s"The penalties were issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe s"The penalties were issued more than ${reason._2} days ago"
            document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to appeal within ${reason._2} days of the date on the penalty notice."
            document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you could not appeal within ${reason._2} days"
            document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
            document.getSubmitButton.text() shouldBe "Continue"

          }
        }
      }

      "the journey is for a 2nd Stage Appeal" when {
        "the penalty type is LSP" when {
          "the page has the correct elements" when {
            "the user is an authorised individual" in {
              stubAuthRequests(false)
              userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSPSecondStage).futureValue

              val result = get("/making-a-late-review-request")

              val document = Jsoup.parse(result.body)

              document.getServiceName.text() shouldBe "Manage your Self Assessment"
              document.title() shouldBe s"The appeal decision was issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
              document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lspCaption(
                dateToString(lateSubmissionAppealData.startDate),
                dateToString(lateSubmissionAppealData.endDate)
              )
              document.getH1Elements.text() shouldBe s"The appeal decision was issued more than ${reason._2} days ago"
              document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to ask for a review within ${reason._2} days of the date of the decision."
              document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you have not asked for a review within ${reason._2} days"
              document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
              document.getSubmitButton.text() shouldBe "Continue"
            }

            "the user is an authorised agent" in {
              stubAuthRequests(true)
              userAnswersRepo.upsertUserAnswer(userAnswersWithReasonLSPSecondStage).futureValue

              val result = get("/agent-making-a-late-review-request", isAgent = true)

              val document = Jsoup.parse(result.body)

              document.getServiceName.text() shouldBe "Manage your Self Assessment"
              document.title() shouldBe s"The appeal decision was issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
              document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lspCaption(
                dateToString(lateSubmissionAppealData.startDate),
                dateToString(lateSubmissionAppealData.endDate)
              )
              document.getH1Elements.text() shouldBe s"The appeal decision was issued more than ${reason._2} days ago"
              document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to ask for a review within ${reason._2} days of the date of the decision."
              document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you have not asked for a review within ${reason._2} days"
              document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
              document.getSubmitButton.text() shouldBe "Continue"
            }
          }
        }

        "the penalty type is LPP - Single Appeal" when {
          "the page has the correct elements" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithSingleLPPsSecondStage).futureValue

            val result = get("/making-a-late-review-request")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe s"The appeal decision was issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe s"The appeal decision was issued more than ${reason._2} days ago"
            document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to ask for a review within ${reason._2} days of the date of the decision."
            document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you have not asked for a review within ${reason._2} days"
            document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
            document.getSubmitButton.text() shouldBe "Continue"

          }
        }

        "the penalty type is LPP - Multiple Appeals" when {
          "the page has the correct elements" in {
            stubAuthRequests(false)
            userAnswersRepo.upsertUserAnswer(userAnswersWithMultipleLPPsSecondStage).futureValue

            val result = get("/making-a-late-review-request")

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe s"The appeal decisions were issued more than ${reason._2} days ago - Manage your Self Assessment - GOV.UK"
            document.getElementById("captionSpan").text() shouldBe LateAppealMessages.English.lppCaption(
              dateToString(latePaymentAppealData.startDate),
              dateToString(latePaymentAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe s"The appeal decisions were issued more than ${reason._2} days ago"
            document.getElementById("infoDaysParagraph").text() shouldBe s"You usually need to ask for a review within ${reason._2} days of the date of the decision."
            document.getElementsByAttributeValue("for", s"${LateAppealForm.key}").text() shouldBe s"Tell us why you have not asked for a review within ${reason._2} days"
            document.getElementById(s"${LateAppealForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
            document.getSubmitButton.text() shouldBe "Continue"

          }
        }
      }
    }
  }

  "POST /making-a-late-appeal" when {

    val userAnswersWithReason = emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Bereavement)

    "the text area content is valid" should {

      "save the value to UserAnswers AND redirect to the CheckAnswers page" in {

        stubAuthRequests(false)
        userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

        val result = post("/making-a-late-appeal")(Map(LateAppealForm.key -> "Some reason"))

        result.status shouldBe SEE_OTHER
        result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad(isAgent).url)

        userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(LateAppealPage)) shouldBe Some("Some reason")
      }
    }

    "the text area content is invalid" should {

      "render a bad request with the Form Error on the page with a link to the field in error" in {

        stubAuthRequests(false)
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
