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

import fixtures.messages.MissedDeadlineReasonMessages
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.MissedDeadlineReasonForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CheckMode, Mode, NormalMode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{JointAppealPage, MissedDeadlineReasonPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class MissedDeadlineReasonControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    super.beforeEach()
  }

  val userAnswersWithReasonLSP: UserAnswers =
    emptyUserAnswersWithLSP.setAnswer(ReasonableExcusePage, Other)

  val userAnswersWithReasonLPP: UserAnswers =
    emptyUserAnswersWithLPP.setAnswer(ReasonableExcusePage, Other)

  val userAnswersWithReasonWithMultipleLPPs: UserAnswers =
    emptyUserAnswersWithMultipleLPPs.setAnswer(ReasonableExcusePage, Other)
      .setAnswer(JointAppealPage, true)
  //  second stage appeals
  val userAnswersWithReasonLSP2ndStage: UserAnswers =
    emptyUserAnswersWithLSP2ndStage.setAnswer(ReasonableExcusePage, Other)

  val userAnswersWithReasonLPP2ndStage: UserAnswers =
    emptyUserAnswersWithLPP2ndStage.setAnswer(ReasonableExcusePage, Other)

  val userAnswersWithReasonWithMultipleLPPs2ndStage: UserAnswers =
    emptyUserAnswersWithMultipleLPPs2ndStage.setAnswer(ReasonableExcusePage, Other)
      .setAnswer(JointAppealPage, true)


  def userAnswers(isLPP: Boolean, is2ndStageAppeal: Boolean, isJointAppeal: Boolean): UserAnswers = {
    if (is2ndStageAppeal) {
      if (isLPP) {
        if (isJointAppeal) {
          userAnswersWithReasonWithMultipleLPPs2ndStage
        } else {
          userAnswersWithReasonLPP2ndStage
        }
      } else userAnswersWithReasonLSP2ndStage

    } else if (isLPP) {
      if (isJointAppeal) {
        userAnswersWithReasonWithMultipleLPPs
      } else {
        userAnswersWithReasonLPP
      }
    } else userAnswersWithReasonLSP
  }

  def getUrl(isLpp: Boolean, isAgent: Boolean, is2ndStageAppeal: Boolean, mode: Mode): String = {
    val url = isLpp match {
      case true if is2ndStageAppeal && isAgent => "/agent-why-are-you-asking-for-review-lpp"
      case true if is2ndStageAppeal && !isAgent => "/why-are-you-asking-for-review-lpp"

      case true if !is2ndStageAppeal && isAgent => "/agent-why-was-the-payment-late"
      case true if !is2ndStageAppeal && !isAgent => "/why-was-the-payment-late"

      case false if is2ndStageAppeal && isAgent => "/agent-why-are-you-asking-for-review-lsp"
      case false if is2ndStageAppeal && !isAgent => "/why-are-you-asking-for-review-lsp"

      case false if !is2ndStageAppeal && isAgent => "/agent-why-was-the-submission-late"
      case false if !is2ndStageAppeal && !isAgent => "/why-was-the-submission-late"

      case _ => "url not found"
    }
    mode match {
      case NormalMode => url
      case CheckMode => s"$url/check"
    }
  }

  def caption(isLPP: Boolean): String = if (isLPP) {
    MissedDeadlineReasonMessages.English.lppCaption(
      dateToString(latePaymentAppealData.startDate),
      dateToString(latePaymentAppealData.endDate)
    )
  } else {
    MissedDeadlineReasonMessages.English.lspCaption(
      dateToString(lateSubmissionAppealData.startDate),
      dateToString(lateSubmissionAppealData.endDate)
    )
  }

  def captionJointAppeal(isLPP: Boolean): String = if (isLPP) {
    MissedDeadlineReasonMessages.English.lppCaptionMultiple(
      dateToString(latePaymentAppealData.startDate),
      dateToString(latePaymentAppealData.endDate)
    )
  } else {
    MissedDeadlineReasonMessages.English.lspCaptionMultiple(
      dateToString(lateSubmissionAppealData.startDate),
      dateToString(lateSubmissionAppealData.endDate)
    )
  }

  //  LPP penalty type (First stage appeal/Second stage appeal  -  Single appeal/Joint appeal)
  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { is2ndStageAppeal =>
      Seq(true, false).foreach { isJointAppeal =>
        Seq(NormalMode, CheckMode).foreach { mode =>
          val urlLPP = getUrl(isLpp = true, isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode)

          s"GET $urlLPP - penalty type is LPP with isAgent = $isAgent is2ndStageAppeal = $is2ndStageAppeal isJointAppeal = $isJointAppeal" should {
            if (!isAgent) {
              testNavBar(url = urlLPP) {
                userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)).futureValue
              }
            }

            "return an OK with a view pre-populated" when {
              s"the user is isAgent = $isAgent AND the page has already been answered is2ndStageAppeal = $is2ndStageAppeal isJointAppeal = $isJointAppeal" in {
                stubAuthRequests(isAgent)
                userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal).setAnswer(MissedDeadlineReasonPage, "Some reason")).futureValue

                val result = get(urlLPP)
                result.status shouldBe OK

                val document = Jsoup.parse(result.body)
                document.select(s"#${MissedDeadlineReasonForm.key}").text() shouldBe "Some reason"
              }
            }

            s"the page has the correct elements for is2ndStageAppeal = $is2ndStageAppeal, isJointAppeal = $isJointAppeal, url = $urlLPP" when {
              s"the user isAgent = $isAgent" in {
                stubAuthRequests(isAgent)
                userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)).futureValue

                val result = get(urlLPP)

                val document = Jsoup.parse(result.body)
                val whichCaption = if (isJointAppeal) captionJointAppeal(true) else caption(true)

                document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
                document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)} - Manage your Self Assessment - GOV.UK"

                document.getElementById("captionSpan").text() shouldBe whichCaption

                document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)
                document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintText(isLPP = true, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)
                document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
                document.getSubmitButton.text() shouldBe "Continue"
              }
            }
          }
        }
      }
    }
  }

  //  LSP penalty type (First stage appeal/Second stage appeal)
  Seq(true, false).foreach { isAgent =>
    Seq(true, false).foreach { is2ndStageAppeal =>
      Seq(NormalMode, CheckMode).foreach { mode =>

        val urlLSP = getUrl(isLpp = false, isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode)

        s"GET $urlLSP - penalty type is LSP with isAgent = $isAgent is2ndStageAppeal = $is2ndStageAppeal mode = $mode" should {
          if (!isAgent) {
            testNavBar(url = urlLSP) {
              userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = false, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false)).futureValue
            }
          }

          "return an OK with a view pre-populated" when {
            s"the user is isAgent = $isAgent AND the page has already been answered is2ndStageAppeal = $is2ndStageAppeal" in {
              stubAuthRequests(isAgent)

              userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = false, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false).setAnswer(MissedDeadlineReasonPage, "Some reason")).futureValue

              val result = get(urlLSP)
              result.status shouldBe OK

              val document = Jsoup.parse(result.body)
              document.select(s"#${MissedDeadlineReasonForm.key}").text() shouldBe "Some reason"
            }
          }

          s"the page has the correct elements for is2ndStageAppeal = $is2ndStageAppeal, url = $urlLSP" when {
            s"the user isAgent = $isAgent" in {
              stubAuthRequests(isAgent)
              userAnswersRepo.upsertUserAnswer(userAnswers(isLPP = false, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false)).futureValue

              val result = get(urlLSP)

              val document = Jsoup.parse(result.body)

              document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
              document.title() shouldBe s"${MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = false, is2ndStageAppeal = is2ndStageAppeal)} - Manage your Self Assessment - GOV.UK"
              document.getElementById("captionSpan").text() shouldBe caption(false)

              document.getElementsByAttributeValue("for", s"${MissedDeadlineReasonForm.key}").text() shouldBe MissedDeadlineReasonMessages.English.headingAndTitle(isLPP = false, is2ndStageAppeal = is2ndStageAppeal)
              document.getElementById("missedDeadlineReason-hint").text() shouldBe MissedDeadlineReasonMessages.English.hintText(isLPP = false, is2ndStageAppeal = is2ndStageAppeal)
              document.getElementById(s"${MissedDeadlineReasonForm.key}-info").text() shouldBe "You can enter up to 5000 characters"
              document.getSubmitButton.text() shouldBe "Continue"
            }
          }
        }
      }
    }
  }

  Seq(true, false).foreach { isLPP =>
    Seq(true, false).foreach { isAgent =>
      Seq(true, false).foreach { is2ndStageAppeal =>
        Seq(NormalMode, CheckMode).foreach { mode =>


          s"POST ${getUrl(isLpp = isLPP, isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode)} with isLPP = $isLPP, isAgent = $isAgent, is2ndStageAppeal = $is2ndStageAppeal  mode = $mode" when {
            val userAnswersWithReason = userAnswers(isLPP = isLPP, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = false).setAnswer(ReasonableExcusePage, Other)
            "the text area content is valid" should {

              "save the value to UserAnswers AND redirect to the correct page" in {

                stubAuthRequests(isAgent)
                userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

                val result = post(getUrl(isLpp = isLPP, isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode))(Map(MissedDeadlineReasonForm.key -> "Some reason"))

                result.status shouldBe SEE_OTHER
                if(mode == NormalMode){
                  result.header("Location") shouldBe Some(routes.ExtraEvidenceController.onPageLoad(isAgent, is2ndStageAppeal, NormalMode).url)
                } else {
                  result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad(isAgent).url)
                }
                userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(MissedDeadlineReasonPage)) shouldBe Some("Some reason")
              }
            }

            "the text area content is invalid" should {

              Seq(true, false).foreach { isJointAppeal =>
                s"render a bad request in isLPP = $isLPP, is2ndStage = $is2ndStageAppeal, isJointAppeal = $isJointAppeal  mode = $mode with the Form Error on the page with a link to the field in error" in {

                  val userAnswersWithReason = userAnswers(isLPP = isLPP, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal).setAnswer(ReasonableExcusePage, Other)
                  stubAuthRequests(isAgent)
                  userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue

                  val result = post(getUrl(isLpp = isLPP, isAgent = isAgent, is2ndStageAppeal = is2ndStageAppeal, mode = mode))(Map(MissedDeadlineReasonForm.key -> ""))
                  result.status shouldBe BAD_REQUEST

                  val document = Jsoup.parse(result.body)

                  document.title() should include(MissedDeadlineReasonMessages.English.errorPrefix)
                  document.select(".govuk-error-summary__title").text() shouldBe MissedDeadlineReasonMessages.English.thereIsAProblem

                  val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
                  error1Link.text() shouldBe MissedDeadlineReasonMessages.English.errorRequired(isLPP = isLPP, is2ndStageAppeal = is2ndStageAppeal, isJointAppeal = isJointAppeal)
                  error1Link.attr("href") shouldBe s"#${MissedDeadlineReasonForm.key}"
                }
              }
            }
          }
        }
      }
    }
  }
}

