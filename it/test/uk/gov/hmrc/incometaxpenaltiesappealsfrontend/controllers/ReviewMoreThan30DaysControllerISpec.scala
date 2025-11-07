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

import fixtures.messages.ReviewMoreThan30DaysMessages
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ReviewMoreThan30DaysForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.*
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{JointAppealPage, ReviewMoreThan30DaysPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

class ReviewMoreThan30DaysControllerISpec extends ControllerISpecHelper {

  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))

  class Setup(jointAppeal: Boolean = false) {
    
    deleteAll(userAnswersRepo)
    
    val reviewAnswers: UserAnswers =
    if(jointAppeal) {
      emptyUserAnswersWithLPP2ndStage
        .setAnswer(ReviewMoreThan30DaysPage, ReviewMoreThan30DaysEnum.yes).setAnswer(JointAppealPage, jointAppeal)
    } else {
      emptyUserAnswersWithLSP2ndStage
        .setAnswer(ReviewMoreThan30DaysPage, ReviewMoreThan30DaysEnum.yes)
    }
    userAnswersRepo.upsertUserAnswer(reviewAnswers).futureValue

  }


  def getUrl(isAgent: Boolean, mode: Mode): String = {
    val pathStart = if (isAgent) "/agent-" else "/"
    val pathMiddle = "review-more-than-30-days"
    mode match {
      case CheckMode => s"${pathStart}${pathMiddle}/check"
      case NormalMode => s"${pathStart}${pathMiddle}"
    }
  }

  Seq(NormalMode, CheckMode).foreach { mode =>
    Seq(true, false).foreach { isAgent =>
      val url = getUrl(isAgent = isAgent, mode)

      s"GET $url" should {

        "return an OK with a view pre-populated" when {
          s"agent is $isAgent AND the page has already been answered" in new Setup() {
            stubAuthRequests(isAgent)

            val result = get(url, isAgent = isAgent)

            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.select(s"#${ReviewMoreThan30DaysForm.key}").hasAttr("checked") shouldBe true
            document.select(s"#${ReviewMoreThan30DaysForm.key}-2").hasAttr("checked") shouldBe false
            document.select(s"#${ReviewMoreThan30DaysForm.key}-3").hasAttr("checked") shouldBe false
          }
        }

        "the page has the correct elements" when {
          s"agent is $isAgent and single appeal" in new Setup() {
            stubAuthRequests(isAgent)

            val result = get(url, isAgent = isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe ReviewMoreThan30DaysMessages.English.serviceName
            document.title() should include(ReviewMoreThan30DaysMessages.English.headingAndTitle)
            document.getElementById("captionSpan").text() shouldBe ReviewMoreThan30DaysMessages.English.lspCaption(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe ReviewMoreThan30DaysMessages.English.headingAndTitle
            document.getHintText.text() shouldBe ReviewMoreThan30DaysMessages.English.hintText
            document.getSubmitButton.text() shouldBe ReviewMoreThan30DaysMessages.English.continue

            document.select(s"#${ReviewMoreThan30DaysForm.key}").hasAttr("checked") shouldBe true
            document.select(s"#${ReviewMoreThan30DaysForm.key}-2").hasAttr("checked") shouldBe false
            document.select(s"#${ReviewMoreThan30DaysForm.key}-3").hasAttr("checked") shouldBe false
          }

          s"agent is $isAgent and multiple appeal" in new Setup(jointAppeal = true) {
            stubAuthRequests(isAgent)

            val result = get(url, isAgent = isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe ReviewMoreThan30DaysMessages.English.serviceName
            document.title() should include(ReviewMoreThan30DaysMessages.English.headingAndTitleMultiple)
            document.getElementById("captionSpan").text() shouldBe ReviewMoreThan30DaysMessages.English.lppCaptionMultiple(
              dateToString(lateSubmissionAppealData.startDate),
              dateToString(lateSubmissionAppealData.endDate)
            )
            document.getH1Elements.text() shouldBe ReviewMoreThan30DaysMessages.English.headingAndTitleMultiple
            document.getHintText.text() shouldBe ReviewMoreThan30DaysMessages.English.hintText
            document.getSubmitButton.text() shouldBe ReviewMoreThan30DaysMessages.English.continue

            document.select(s"#${ReviewMoreThan30DaysForm.key}").hasAttr("checked") shouldBe true
            document.select(s"#${ReviewMoreThan30DaysForm.key}-2").hasAttr("checked") shouldBe false
            document.select(s"#${ReviewMoreThan30DaysForm.key}-3").hasAttr("checked") shouldBe false
          }
        }
      }

      s"POST $url" when {
        s"a valid radio option has been selected and agent is $isAgent" should {

            "save the value to UserAnswers AND redirect to the Late appeal page" in new Setup() {

              stubAuthRequests(isAgent)

              val result = post(url)(Map(ReviewMoreThan30DaysForm.key -> ReviewMoreThan30DaysEnum.yes))

              result.status shouldBe SEE_OTHER
              result.header("Location") shouldBe Some(routes.LateAppealController.onPageLoad(isAgent = isAgent, is2ndStageAppeal = true, mode = mode).url)

              userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(ReviewMoreThan30DaysPage)) shouldBe Some(ReviewMoreThan30DaysEnum.yes)
            }
        }

        s"the radio option is invalid and agent is $isAgent" should {

          "render a bad request with the Form Error on the page with a link to the field in error" in new Setup() {

            stubAuthRequests(isAgent)

            val result = post(url)(Map(ReviewMoreThan30DaysForm.key -> ""))
            result.status shouldBe BAD_REQUEST

            val document = Jsoup.parse(result.body)

            document.title() should include(ReviewMoreThan30DaysMessages.English.errorPrefix)
            document.select(".govuk-error-summary__title").text() shouldBe ReviewMoreThan30DaysMessages.English.thereIsAProblem

            val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
            error1Link.text() shouldBe ReviewMoreThan30DaysMessages.English.errorRequired
            error1Link.attr("href") shouldBe s"#${ReviewMoreThan30DaysForm.key}"
          }
        }
      }
    }
  }
}
