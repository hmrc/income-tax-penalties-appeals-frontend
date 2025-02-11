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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, CurrentUserRequestWithAnswers}
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

      s"rendering in language of '${lang.name}'" should {

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(lang.code)))

        "construct from UserAnswers" in {

          val uploads = Seq(callbackModel, callbackModel2)

          val userAnswers = emptyUserAnswers
            .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
            .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)
            .setAnswer(ReasonableExcusePage, "health")
            .setAnswer(LateAppealPage, "I was late")
            .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 2, 1))
            .setAnswer(WhenDidEventEndPage, LocalDate.of(2025, 2, 2))

          implicit val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(userAnswers)

          checkAnswersHelper.constructSummaryListRows(uploads) shouldBe Seq(
            WhoPlannedToSubmitSummary.row(),
            WhatCausedYouToMissDeadlineSummary.row(),
            ReasonableExcuseSummary.row(),
            WhenDidEventHappenSummary.row(),
            WhenDidEventEndSummary.row(),
            CrimeReportedSummary.row(),
            lateAppealSummary.row(),
            UploadedDocumentsSummary.row(uploads)
          ).flatten
        }
      }
    }
  }
}
