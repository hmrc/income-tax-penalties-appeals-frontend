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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, SummaryListRow}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan.UploadJourney
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

case class UploadedFilesViewModel(fileReference: String,
                                  filename: String,
                                  fileSize: Int)

object UploadedFilesViewModel extends SummaryListRowHelper {

  def apply(files: Seq[UploadJourney]): Seq[UploadedFilesViewModel] =
    files.collect {
      case UploadJourney(reference, _, _, Some(fileDetails), _, _, _) =>
        UploadedFilesViewModel(
          fileReference = reference,
          filename = fileDetails.fileName,
          fileSize = fileDetails.size
        )
    }

  def toSummaryListRows(files: Seq[UploadedFilesViewModel])
                       (implicit messages: Messages): Seq[SummaryListRow] =
    files.zipWithIndex.map { case (file, i) =>
      summaryListRow(
        label = messages("uploadCheckAnswers.nonJs.summaryKey", i + 1),
        value = HtmlFormat.escape(file.filename),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              href = "#", //TODO: Add remove link in future story
              content = Text(messages("common.remove")),
              visuallyHiddenText = Some(messages("uploadCheckAnswers.nonJs.summaryKey", i + 1))
            )
          )
        ))
      )
    }
}
