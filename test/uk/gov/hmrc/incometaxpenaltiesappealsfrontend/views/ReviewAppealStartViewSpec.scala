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
import fixtures.messages.ReviewAppealStartMessages
import fixtures.views.BaseSelectors
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.ReviewAppealStartView

class ReviewAppealStartViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with SummaryListRowHelper with BaseFixtures {

  lazy val view: ReviewAppealStartView = app.injector.instanceOf[ReviewAppealStartView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

  object Selectors extends BaseSelectors

  Seq(ReviewAppealStartMessages.English, ReviewAppealStartMessages.Welsh).foreach { messagesForLanguage =>

    s"When rendering the View Appeal Details page in language '${messagesForLanguage.lang.name}'" when {

      implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      "appeal isLate == false" should {

        implicit val doc: Document = asDocument(view(isLate = false, routes.ReasonableExcuseController.onPageLoad()))

        behave like pageWithExpectedElementsAndMessages(
          Selectors.title -> messagesForLanguage.headingAndTitle,
          Selectors.h1 -> messagesForLanguage.headingAndTitle,
          Selectors.p(1) -> messagesForLanguage.p1,
          Selectors.p(2) -> messagesForLanguage.p2,
          Selectors.h2(1) -> messagesForLanguage.h2,
          Selectors.p(3) -> messagesForLanguage.p3,
          Selectors.p(4) -> messagesForLanguage.p4,
          Selectors.link(1) -> messagesForLanguage.continue
        )
      }

      "appeal isLate == true" should {

        implicit val doc: Document = asDocument(view(isLate = true, routes.ReasonableExcuseController.onPageLoad()))

        behave like pageWithExpectedElementsAndMessages(
          Selectors.title -> messagesForLanguage.headingAndTitle,
          Selectors.h1 -> messagesForLanguage.headingAndTitle,
          Selectors.p(1) -> messagesForLanguage.p1,
          Selectors.p(2) -> messagesForLanguage.p2,
          Selectors.h2(1) -> messagesForLanguage.h2,
          Selectors.p(3) -> messagesForLanguage.p3List,
          Selectors.bullet(1) -> messagesForLanguage.bullet1,
          Selectors.bullet(2) -> messagesForLanguage.bullet2,
          Selectors.link(1) -> messagesForLanguage.continue
        )
      }
    }
  }
}
