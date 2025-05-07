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
import fixtures.messages.WhenDidEventHappenMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventHappenPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

import java.time.LocalDate

class WhenDidEventHappenSummarySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "WhenDidEventHappenSummary" when {

    ReasonableExcuse.allReasonableExcuses.foreach { reason =>

      s"for reasonableExcuse '$reason'" when {

        Seq(WhenDidEventHappenMessages.English, WhenDidEventHappenMessages.Welsh).foreach { messagesForLanguage =>

          s"being rendered in lang name '${messagesForLanguage.lang.name}'" when {

            implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

            "there's no answer" should {

              "return None" in {
                implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswersWithLSP)
                WhenDidEventHappenSummary.row() shouldBe None
              }
            }

            "there's an answer" when {

              "show actions link == true" should {

                "must output the expected row a change link" in {

                  implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                    emptyUserAnswersWithLSP
                      .setAnswer(ReasonableExcusePage, reason)
                      .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 1, 1))
                  )

                  WhenDidEventHappenSummary.row() shouldEqual Some(summaryListRow(
                    label = messagesForLanguage.cyaKey(reason),
                    value = Html(dateToString(LocalDate.of(2025, 1, 1))),
                    actions = Some(Actions(
                      items = Seq(
                        ActionItem(
                          content = Text(messagesForLanguage.change),
                          href = controllers.routes.WhenDidEventHappenController.onPageLoad().url,
                          visuallyHiddenText = Some(messagesForLanguage.cyaHidden(reason))
                        ).withId("changeWhenDidEventHappen")
                      )
                    ))
                  ))
                }
              }

              "show actions link == false" should {

                "must output the expected row WITHOUT a change link" in {

                  implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                    emptyUserAnswersWithLSP
                      .setAnswer(ReasonableExcusePage, reason)
                      .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 1, 1))
                  )

                  WhenDidEventHappenSummary.row(showActionLinks = false) shouldBe Some(summaryListRow(
                    label = messagesForLanguage.cyaKey(reason),
                    value = Html(dateToString(LocalDate.of(2025, 1, 1))),
                    actions = None
                  ))
                }
              }
            }
          }
        }
      }
    }
  }
}
