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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import javax.inject.Inject

class PrintAppealHelper @Inject()(checkAnswersHelper: CheckAnswersHelper,
                                  timeMachine: TimeMachine) extends SummaryListRowHelper {

  def constructPrintSummaryRows(uploadedFiles: Seq[UploadJourney], nino: Option[String])
                               (implicit user: CurrentUserRequestWithAnswers[_], messages: Messages): Seq[SummaryListRow] =
    Seq(
      nino map ninoSummaryRow,
      Some(appealDateRow()),
      Some(penaltyPeriodRow()),
    ).flatten ++ checkAnswersHelper.constructSummaryListRows(uploadedFiles, showActionLinks = false)

  private def ninoSummaryRow(nino: String)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("viewAppealDetails.ni"),
      value = Html(nino),
      actions = None
    )

  private def appealDateRow()(implicit messages: Messages, user: CurrentUserRequestWithAnswers[_]): SummaryListRow = {
    val msgSuffix = if(user.is2ndStageAppeal) ".review" else ""
    summaryListRow(
      label = messages(s"viewAppealDetails.appealDate$msgSuffix"),
      value = Html(dateToString(timeMachine.getCurrentDate)),
      actions = None
    )
  }

  private def penaltyPeriodRow()(implicit user: CurrentUserRequestWithAnswers[_], messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("viewAppealDetails.penaltyAppealed"),
      value = Html {
        val messageKey = if(user.isLPP) "service.lpp.caption" else "service.lsp.caption"
        messages(messageKey, dateToString(user.periodStartDate), dateToString(user.periodEndDate))
      },
      actions = None
    )
}
