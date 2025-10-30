/*
 * Copyright 2025 HM Revenue & Customs
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

import fixtures.messages.WhatCausedYouToMissDeadlineMessages
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.WhatCausedYouToMissDeadlineForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, CheckMode, NormalMode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.WhatCausedYouToMissDeadlinePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString

class WhatCausedYouToMissDeadlineControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))
  
  override def beforeEach(): Unit = {
    deleteAll(userAnswersRepo)
    userAnswersRepo.upsertUserAnswer(emptyUserAnswersWithLSP).futureValue
    super.beforeEach()
  }

  List(NormalMode, CheckMode).foreach { mode =>
    val url = "/what-caused-you-to-miss-the-deadline" + {
      if (mode == CheckMode) "/check" else ""
    }

    s"GET $url" should {

      "return an OK with a view pre-populated" when {
        "the user is an authorised agent AND the page has already been answered" in {
          stubAuthRequests(true)
          userAnswersRepo.upsertUserAnswer(
            emptyUserAnswersWithLSP.setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.agent)
          ).futureValue

          val result = get(url, isAgent = true)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.select(s"#${WhatCausedYouToMissDeadlineForm.key}").hasAttr("checked") shouldBe true
          document.select(s"#${WhatCausedYouToMissDeadlineForm.key}-2").hasAttr("checked") shouldBe false
        }
      }

      "the page has the correct elements" when {
        "the user is an authorised agent" in {
          stubAuthRequests(true)

          val result = get(url, isAgent = true)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe WhatCausedYouToMissDeadlineMessages.English.serviceName
          document.title() should include(WhatCausedYouToMissDeadlineMessages.English.titleAndHeading)
          document.getElementById("captionSpan").text() shouldBe WhatCausedYouToMissDeadlineMessages.English.lspCaption(
            dateToString(lateSubmissionAppealData.startDate),
            dateToString(lateSubmissionAppealData.endDate)
          )
          document.getH1Elements.text() shouldBe WhatCausedYouToMissDeadlineMessages.English.titleAndHeading
          document.getSubmitButton.text() shouldBe WhatCausedYouToMissDeadlineMessages.English.continue

          document.select(s"#${WhatCausedYouToMissDeadlineForm.key}").hasAttr("checked") shouldBe false
          document.select(s"#${WhatCausedYouToMissDeadlineForm.key}-2").hasAttr("checked") shouldBe false
        }
      }
    }

    s"POST $url" when {
      "a valid radio option has been selected" should {

        if (mode == NormalMode) {
          "save the value to UserAnswers AND redirect to the ReasonableExcuse page" in {

            stubAuthRequests(true)

            val result = post(url)(Map(WhatCausedYouToMissDeadlineForm.key -> AgentClientEnum.agent))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.ReasonableExcuseController.onPageLoad(true, NormalMode).url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhatCausedYouToMissDeadlinePage)) shouldBe Some(AgentClientEnum.agent)
          }
        } else {
          "save the value to UserAnswers AND redirect to the CYA page" in {

            stubAuthRequests(true)

            val result = post(url)(Map(WhatCausedYouToMissDeadlineForm.key -> AgentClientEnum.agent))

            result.status shouldBe SEE_OTHER
            result.header("Location") shouldBe Some(routes.CheckYourAnswersController.onPageLoad(true).url)

            userAnswersRepo.getUserAnswer(testJourneyId).futureValue.flatMap(_.getAnswer(WhatCausedYouToMissDeadlinePage)) shouldBe Some(AgentClientEnum.agent)
          }
        }
      }

      "the text area content is invalid" should {

        "render a bad request with the Form Error on the page with a link to the field in error" in {

          stubAuthRequests(true)

          val result = post(url)(Map(WhatCausedYouToMissDeadlineForm.key -> ""))
          result.status shouldBe BAD_REQUEST

          val document = Jsoup.parse(result.body)

          document.title() should include(WhatCausedYouToMissDeadlineMessages.English.errorPrefix)
          document.select(".govuk-error-summary__title").text() shouldBe WhatCausedYouToMissDeadlineMessages.English.thereIsAProblem

          val error1Link = document.select(".govuk-error-summary__list li:nth-of-type(1) a")
          error1Link.text() shouldBe WhatCausedYouToMissDeadlineMessages.English.errorRequired
          error1Link.attr("href") shouldBe s"#${WhatCausedYouToMissDeadlineForm.key}"
        }
      }
    }
  }
}
