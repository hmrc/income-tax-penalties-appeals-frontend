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
import fixtures.messages.WhenDidEventEndMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.{TechnicalIssues, UnexpectedHospital}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CurrentUserRequestWithAnswers, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{ReasonableExcusePage, WhenDidEventEndPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

import java.time.LocalDate

class WhenDidEventEndSummarySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "WhenDidEventEndSummary" when {

    ReasonableExcuse.allReasonableExcuses.foreach { reason =>

      s"for reasonableExcuse '$reason'" when {

        Seq(WhenDidEventEndMessages.English, WhenDidEventEndMessages.Welsh).foreach { messagesForLanguage =>

          s"being rendered in lang name '${messagesForLanguage.lang.name}'" when {

            implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

            "there's no answer" should {

              "return None" in {
                implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUerAnswersWithLSP)
                WhenDidEventEndSummary.row() shouldBe None
              }
            }

            "there's an answer" when {

              if (Seq(TechnicalIssues, UnexpectedHospital).contains(reason)) {

                "show actions links == true" should {

                  "must output the expected row with a change link" in {

                    implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                      emptyUerAnswersWithLSP
                        .setAnswer(ReasonableExcusePage, reason)
                        .setAnswer(WhenDidEventEndPage, LocalDate.of(2025, 1, 1))
                    )

                    WhenDidEventEndSummary.row() shouldBe Some(summaryListRow(
                      label = messagesForLanguage.cyaKey(reason),
                      value = Html(dateToString(LocalDate.of(2025, 1, 1))),
                      actions = Some(Actions(
                        items = Seq(
                          ActionItem(
                            content = Text(messagesForLanguage.change),
                            href = controllers.routes.WhenDidEventEndController.onPageLoad().url,
                            visuallyHiddenText = Some(messagesForLanguage.cyaHidden(reason))
                          ).withId("changeWhenDidEventEnd")
                        )
                      ))
                    ))
                  }
                }

                "show actions links == false" should {

                  "must output the expected row WITHOUT a change link" in {

                    implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                      emptyUerAnswersWithLSP
                        .setAnswer(ReasonableExcusePage, reason)
                        .setAnswer(WhenDidEventEndPage, LocalDate.of(2025, 1, 1))
                    )

                    WhenDidEventEndSummary.row(showActionLinks = false) shouldBe Some(summaryListRow(
                      label = messagesForLanguage.cyaKey(reason),
                      value = Html(dateToString(LocalDate.of(2025, 1, 1))),
                      actions = None
                    ))
                  }
                }
              } else {

                "return None" in {
                  implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                    emptyUerAnswersWithLSP
                      .setAnswer(ReasonableExcusePage, reason)
                      .setAnswer(WhenDidEventEndPage, LocalDate.of(2025, 1, 1))
                  )
                  WhenDidEventEndSummary.row() shouldBe None
                }
              }
            }
          }
        }
      }
    }
  }
}
