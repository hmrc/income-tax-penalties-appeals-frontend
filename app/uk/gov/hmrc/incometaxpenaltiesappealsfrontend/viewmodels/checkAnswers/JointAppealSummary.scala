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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.checkAnswers

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.JointAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

object JointAppealSummary extends SummaryListRowHelper {

  def row(showActionLinks: Boolean = true)(implicit user: CurrentUserRequestWithAnswers[_], messages: Messages): Option[SummaryListRow] =
    Option.when(user.hasMultipleLPPs)(JointAppealPage.value.map { appealBoth =>

      val msgSuffix = if(user.is2ndStageAppeal) ".review" else ""

      summaryListRow(
        label = messages(s"checkYourAnswers.jointAppeal.key$msgSuffix"),
        value = Html(messages(s"common.${if(appealBoth) "yes" else "no"}")),
        actions = Option.when(showActionLinks)(Actions(
          items = Seq(
            ActionItem(
              content = Text(messages("common.change")),
              href = controllers.routes.JointAppealController.onPageLoad().url,
              visuallyHiddenText = Some(messages(s"checkYourAnswers.jointAppeal.change.hidden$msgSuffix"))
            ).withId("changeJointAppeal")
          )
        ))
      )
    }).flatten
}
