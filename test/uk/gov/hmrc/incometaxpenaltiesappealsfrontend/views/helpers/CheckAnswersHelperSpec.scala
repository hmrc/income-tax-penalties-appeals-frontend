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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Health
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, NormalMode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.checkAnswers._

import java.time.LocalDate

class CheckAnswersHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with FileUploadFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val checkAnswersHelper: CheckAnswersHelper = app.injector.instanceOf[CheckAnswersHelper]
  lazy val lateAppealSummary: LateAppealSummary = app.injector.instanceOf[LateAppealSummary]

  "CheckAnswersHelper" should {

    Seq(En, Cy).foreach { lang =>

      s"rendering in language of '${lang.name}'" when {

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(lang.code)))

        val uploads = Seq(callbackModel, callbackModel2)

        val userAnswers = emptyUserAnswersWithLSP
          .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
          .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
          .setAnswer(ReasonableExcusePage, Health)
          .setAnswer(LateAppealPage, "I was late")
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 2, 1))
          .setAnswer(WhenDidEventEndPage, LocalDate.of(2025, 2, 2))

        implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(userAnswers)

        "show action links == true" should {

          "construct from UserAnswers with a change link" in {

            checkAnswersHelper.constructSummaryListRows(uploads) shouldBe Seq(
              WhoPlannedToSubmitSummary.row(),
              WhatCausedYouToMissDeadlineSummary.row(),
              JointAppealSummary.row(),
              ReasonableExcuseSummary.row(),
              WhenDidEventHappenSummary.row(),
              WhenDidEventEndSummary.row(),
              CrimeReportedSummary.row(),
              MissedDeadlineReasonSummary.row(),
              lateAppealSummary.row(),
              ExtraEvidenceSummary.row(),
              UploadedDocumentsSummary.row(uploads)
            ).flatten
          }
        }

        "show action links == false" should {

          "construct from UserAnswers WITHOUT a change link" in {

            checkAnswersHelper.constructSummaryListRows(uploads, showActionLinks = false) shouldBe Seq(
              WhoPlannedToSubmitSummary.row(showActionLinks = false),
              WhatCausedYouToMissDeadlineSummary.row(showActionLinks = false),
              JointAppealSummary.row(showActionLinks = false),
              ReasonableExcuseSummary.row(showActionLinks = false),
              WhenDidEventHappenSummary.row(showActionLinks = false),
              WhenDidEventEndSummary.row(showActionLinks = false),
              CrimeReportedSummary.row(showActionLinks = false),
              MissedDeadlineReasonSummary.row(showActionLinks = false),
              lateAppealSummary.row(showActionLinks = false),
              ExtraEvidenceSummary.row(showActionLinks = false),
              UploadedDocumentsSummary.row(uploads, showActionLinks = false)
            ).flatten
          }
        }
      }
    }
  }
}
