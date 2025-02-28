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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers

import fixtures.FileUploadFixtures
import fixtures.messages.PrintAppealMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Health
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, CurrentUserRequestWithAnswers}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.checkAnswers._

import java.time.LocalDate

class PrintAppealHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with FileUploadFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val checkAnswersHelper: CheckAnswersHelper = app.injector.instanceOf[CheckAnswersHelper]
  lazy val printAppealHelper: PrintAppealHelper = app.injector.instanceOf[PrintAppealHelper]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  "PrintAppealHelper" should {

    Seq(PrintAppealMessages.English, PrintAppealMessages.Welsh).foreach { messagesForLanguage =>

      s"rendering in language of '${messagesForLanguage.lang.name}'" when {

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

        val uploads = Seq(callbackModel, callbackModel2)

        val userAnswers = emptyUserAnswersWithLSP
          .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
          .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
          .setAnswer(ReasonableExcusePage, Health)
          .setAnswer(LateAppealPage, "I was late")
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 2, 1))
          .setAnswer(WhenDidEventEndPage, LocalDate.of(2025, 2, 2))

        implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(userAnswers)

        "a NINO exists" should {

          "return expecting print rows with a NINO row" in {

            printAppealHelper.constructPrintSummaryRows(uploads, Some(testNino)) shouldBe Seq(
              summaryListRow(
                label = messagesForLanguage.nino,
                value = Html(testNino)
              ),
              summaryListRow(
                label = messagesForLanguage.appealDate,
                value = Html(dateToString(timeMachine.getCurrentDate))
              ),
              summaryListRow(
                label = messagesForLanguage.penaltyAppealed,
                value = Html(messagesForLanguage.lspCaption(
                  dateToString(lateSubmissionAppealData.startDate),
                  dateToString(lateSubmissionAppealData.endDate),
                  removeNBSP = false
                ))
              )
            ) ++ checkAnswersHelper.constructSummaryListRows(uploads, showActionLinks = false)
          }
        }

        "a NINO does NOT exist" should {

          "return expected print rows WITHOUT a NINO" in {

            printAppealHelper.constructPrintSummaryRows(uploads, None) shouldBe Seq(
              summaryListRow(
                label = messagesForLanguage.appealDate,
                value = Html(dateToString(timeMachine.getCurrentDate))
              ),
              summaryListRow(
                label = messagesForLanguage.penaltyAppealed,
                value = Html(messagesForLanguage.lspCaption(
                  dateToString(lateSubmissionAppealData.startDate),
                  dateToString(lateSubmissionAppealData.endDate),
                  removeNBSP = false
                ))
              )
            ) ++ checkAnswersHelper.constructSummaryListRows(uploads, showActionLinks = false)
          }
        }
      }
    }
  }
}
