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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.checkAnswers

import fixtures.BaseFixtures
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import fixtures.messages.LateAppealMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{Bereavement, Crime}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{LateAppealPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

class LateAppealSummarySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lateAppealSummary: LateAppealSummary = app.injector.instanceOf[LateAppealSummary]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "LateAppealSummary" when {

    Seq(LateAppealMessages.English, LateAppealMessages.Welsh).foreach { messagesForLanguage =>

      s"being rendered in lang name '${messagesForLanguage.lang.name}'" when {

        implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

        "there's no answer" should {

          "return None" in {
            implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswersWithLSP)
            lateAppealSummary.row() shouldBe None
          }
        }

        "there's an answer" when {

          "reasonable excuse is 'bereavement' (show actions links == true)" when {

            "must output the expected row with the extended late appeal days value and a change link" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                emptyUserAnswersWithLSP
                  .setAnswer(ReasonableExcusePage, Bereavement)
                  .setAnswer(LateAppealPage, "foo")
              )

              lateAppealSummary.row() shouldBe Some(summaryListRow(
                label = messagesForLanguage.cyaKey(appConfig.bereavementLateDays),
                value = Html("foo"),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(messagesForLanguage.change),
                      href = controllers.routes.LateAppealController.onPageLoad(isAgent).url,
                      visuallyHiddenText = Some(messagesForLanguage.cyaHidden(appConfig.bereavementLateDays))
                    ).withId("changeLateAppeal")
                  )
                ))
              ))
            }
          }

          "reasonable excuse is anything else (show action links == false)" when {

            "must output the expected row with the standard late appeal days WITHOUT a change link" in {

              implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                emptyUserAnswersWithLSP
                  .setAnswer(ReasonableExcusePage, Crime)
                  .setAnswer(LateAppealPage, "foo")
              )

              lateAppealSummary.row(showActionLinks = false) shouldBe Some(summaryListRow(
                label = messagesForLanguage.cyaKey(appConfig.lateDays),
                value = Html("foo"),
                actions = None
              ))
            }
          }
        }
      }
    }
  }
}
