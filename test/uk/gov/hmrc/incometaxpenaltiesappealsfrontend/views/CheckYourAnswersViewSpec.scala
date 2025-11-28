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
import fixtures.messages.CheckYourAnswersMessages
import fixtures.views.BaseSelectors
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with SummaryListRowHelper with BaseFixtures {

  lazy val view: CheckYourAnswersView = app.injector.instanceOf[CheckYourAnswersView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object Selectors extends BaseSelectors

  Seq(CheckYourAnswersMessages.English, CheckYourAnswersMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the page in language '${messagesForLanguage.lang.name}'" when {

      "rendering for a 1st Stage Appeal" should {

        implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
        implicit val doc: Document = asDocument(view(Seq.empty[UploadJourney]))

        behave like pageWithExpectedElementsAndMessages(
          Selectors.title -> messagesForLanguage.headingAndTitle,
          Selectors.h1 -> messagesForLanguage.headingAndTitle,
          Selectors.h2(2) -> messagesForLanguage.declarationH2,
          Selectors.p(1) -> messagesForLanguage.declarationP1,
          Selectors.p(2) -> messagesForLanguage.declarationP2
        )
      }

      "rendering for a 2nd Stage Appeal (Review)" should {

        implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
          emptyUserAnswers.setAnswerForKey(IncomeTaxSessionKeys.penaltyData, penaltyDataLPP.copy(is2ndStageAppeal = true))
        )

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
        implicit val doc: Document = asDocument(view(Seq.empty[UploadJourney]))

        behave like pageWithExpectedElementsAndMessages(
          Selectors.title -> messagesForLanguage.headingAndTitle,
          Selectors.h1 -> messagesForLanguage.headingAndTitle,
          Selectors.h2(2) -> messagesForLanguage.declarationH2,
          Selectors.p(1) -> messagesForLanguage.declarationP1Review,
          Selectors.p(2) -> messagesForLanguage.declarationP2
        )
      }
    }
  }
}
