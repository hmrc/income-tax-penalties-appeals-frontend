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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views

import fixtures.BaseFixtures
import fixtures.messages.PrintAppealMessages
import fixtures.views.BaseSelectors
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.ViewAppealDetailsView

class ViewAppealDetailsViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with SummaryListRowHelper with BaseFixtures {

  lazy val view: ViewAppealDetailsView = app.injector.instanceOf[ViewAppealDetailsView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

  object Selectors extends BaseSelectors

  Seq(PrintAppealMessages.English, PrintAppealMessages.Welsh).foreach { messagesForLanguage =>

    s"When rendering the View Appeal Details page in language '${messagesForLanguage.lang.name}'" should {

      implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
      implicit val doc = asDocument(view(Seq(
        summaryListRow(messagesForLanguage.nino, Html(testNino)),
        summaryListRow(messagesForLanguage.penaltyAppealed, Html(messagesForLanguage.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        ))),
        summaryListRow(messagesForLanguage.appealDate, Html(dateToString(timeMachine.getCurrentDate, withNBSP = false))),
      )))

      behave like pageWithExpectedElementsAndMessages(
        Selectors.title -> messagesForLanguage.headingAndTitle,
        Selectors.h1 -> messagesForLanguage.headingAndTitle,
        Selectors.summaryRowKey(1) -> messagesForLanguage.nino,
        Selectors.summaryRowValue(1) -> testNino,
        Selectors.summaryRowKey(2) -> messagesForLanguage.penaltyAppealed,
        Selectors.summaryRowValue(2) -> messagesForLanguage.lspCaption(
          dateToString(lateSubmissionAppealData.startDate),
          dateToString(lateSubmissionAppealData.endDate)
        ),
        Selectors.summaryRowKey(3) -> messagesForLanguage.appealDate,
        Selectors.summaryRowValue(3) -> dateToString(timeMachine.getCurrentDate, withNBSP = false),
        Selectors.p(1) -> concat(messagesForLanguage.warn1, messagesForLanguage.warn2),
        Selectors.button -> messagesForLanguage.printThisPage
      )
    }
  }
}
