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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.LateAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

import javax.inject.Inject

class LateAppealSummary @Inject()(implicit val appConfig: AppConfig) extends SummaryListRowHelper {

  def row(showActionLinks: Boolean = true)(implicit user: CurrentUserRequestWithAnswers[_], messages: Messages): Option[SummaryListRow] =
    LateAppealPage.value.map { lateAppeal =>
      summaryListRow(
        label = messages("checkYourAnswers.lateAppeal.key", user.lateAppealDays()),
        value = HtmlFormat.escape(lateAppeal),
        actions = Option.when(showActionLinks)(Actions(
          items = Seq(
            ActionItem(
              content = Text(messages("common.change")),
              href = controllers.routes.LateAppealController.onPageLoad().url,
              visuallyHiddenText = Some(messages("checkYourAnswers.lateAppeal.change.hidden", user.lateAppealDays()))
            ).withId("changeLateAppeal")
          )
        ))
      )
    }
}
