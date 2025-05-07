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
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.{UploadDetails, UploadJourney}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

import scala.annotation.tailrec

object UploadedDocumentsSummary extends SummaryListRowHelper {

  def row(uploadedFiles: Seq[UploadJourney], showActionLinks: Boolean = true)(implicit user: CurrentUserRequestWithAnswers[_], messages: Messages): Option[SummaryListRow] =
    Option.when(uploadedFiles.exists(_.uploadDetails.isDefined) && ReasonableExcusePage.value.contains(Other)) {
      val msgSuffix = if (user.is2ndStageAppeal) ".review" else ""
      summaryListRow(
        label = messages(s"checkYourAnswers.uploadedDocuments.key$msgSuffix"),
        value = HtmlFormat.fill(filenames(uploadedFiles.flatMap(_.uploadDetails))),
        actions = Option.when(showActionLinks)(Actions(
          items = Seq(
            ActionItem(
              content = Text(messages("common.change")),
              href = controllers.upscan.routes.UpscanCheckAnswersController.onPageLoad().url,
              visuallyHiddenText = Some(messages(s"checkYourAnswers.uploadedDocuments.change.hidden$msgSuffix"))
            ).withId("changeUploadedFiles")
          )
        ))
      )
    }

  /**
   * Tail recursively builds a list of filenames; stack overflow safe.
   * This is required so that the last element doesn't include a line break.
   */
  @tailrec
  private def filenames(uploadDetails: Seq[UploadDetails], accumulator: Seq[Html] = Seq()): Seq[Html] =
    uploadDetails match {
      case file +: Nil => accumulator :+ Html(s"${HtmlFormat.escape(file.fileName)}")
      case file +: tail => filenames(tail, accumulator :+ Html(s"${HtmlFormat.escape(file.fileName)}<br>"))
      case _ => accumulator
    }
}
