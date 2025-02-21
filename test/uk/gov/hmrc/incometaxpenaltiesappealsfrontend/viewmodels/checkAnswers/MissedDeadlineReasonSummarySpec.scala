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
import fixtures.messages.MissedDeadlineReasonMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{MissedDeadlineReasonPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

class MissedDeadlineReasonSummarySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "MissedDeadlineReasonSummary" when {

    Seq(MissedDeadlineReasonMessages.English, MissedDeadlineReasonMessages.Welsh).foreach { messagesForLanguage =>

      s"being rendered in lang name '${messagesForLanguage.lang.name}'" when {

        implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

        "there's no answer" should {

          "return None" in {
            implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUerAnswersWithLSP)
            MissedDeadlineReasonSummary.row() shouldBe None
          }
        }

        "there's an answer" when {

          Seq(true, false).foreach { isLPP =>

            s"penalty type isLPP='$isLPP'" when {

              implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                userAnswers = (if (isLPP) emptyUerAnswersWithLPP else emptyUerAnswersWithLSP)
                  .setAnswer(ReasonableExcusePage, Other)
                  .setAnswer(MissedDeadlineReasonPage, "foo"),
                penaltyData = if(isLPP) penaltyDataLPP else penaltyDataLSP
              )

              "show actions links == true" when {

                "must output the expected row with change link" in {

                  MissedDeadlineReasonSummary.row() shouldBe Some(summaryListRow(
                    label = messagesForLanguage.cyaKey(isLPP),
                    value = Html("foo"),
                    actions = Some(Actions(
                      items = Seq(
                        ActionItem(
                          content = Text(messagesForLanguage.change),
                          href = controllers.routes.MissedDeadlineReasonController.onPageLoad().url,
                          visuallyHiddenText = Some(messagesForLanguage.cyaHidden(isLPP))
                        ).withId("changeMissedDeadlineReason")
                      )
                    ))
                  ))
                }
              }

              "show action links == false" when {

                "must output the expected row WITHOUT a change link" in {

                  MissedDeadlineReasonSummary.row(showActionLinks = false) shouldBe Some(summaryListRow(
                    label = messagesForLanguage.cyaKey(isLPP),
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
  }
}
