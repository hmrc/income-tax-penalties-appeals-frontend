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
import fixtures.messages.CrimeReportedMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{CrimeReportedEnum, CurrentUserRequestWithAnswers}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{CrimeReportedPage, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

class CrimeReportedSummarySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "CrimeReportedSummary" when {

    Seq(
      "bereavement",
      "crime",
      "fireOrFlood",
      "technicalIssues",
      "cessation",
      "health",
      "unexpectedHospital"
    ).foreach { reason =>

      s"for reasonableExcuse '$reason'" when {

        Seq(CrimeReportedMessages.English, CrimeReportedMessages.Welsh).foreach { messagesForLanguage =>

          s"being rendered in lang name '${messagesForLanguage.lang.name}'" when {

            implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

            if (reason != "crime") {

              "return None" in {
                implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers.setAnswer(ReasonableExcusePage, reason))
                CrimeReportedSummary.row() shouldBe None
              }

            } else {

              "when there's no answer" should {

                "return None" in {
                  implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers.setAnswer(ReasonableExcusePage, reason))
                  CrimeReportedSummary.row() shouldBe None
                }
              }

              "when there's an answer" should {

                "must output the expected row" in {

                  implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
                    emptyUserAnswers
                      .setAnswer(ReasonableExcusePage, reason)
                      .setAnswer(CrimeReportedPage, CrimeReportedEnum.yes)
                  )

                  CrimeReportedSummary.row() shouldBe Some(summaryListRow(
                    label = messagesForLanguage.cyaKey,
                    value = Html(messagesForLanguage.yes),
                    actions = Some(Actions(
                      items = Seq(
                        ActionItem(
                          content = Text(messagesForLanguage.change),
                          href = controllers.routes.CrimeReportedController.onPageLoad().url,
                          visuallyHiddenText = Some(messagesForLanguage.cyaHidden)
                        ).withId("changeCrimeReported")
                      )
                    ))
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
