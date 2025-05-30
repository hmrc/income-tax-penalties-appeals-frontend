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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.upscan.{routes => upscanRoutes}

case class UploadedFilesViewModel(fileReference: String,
                                  index: Int,
                                  filename: String,
                                  fileSize: Int)

object UploadedFilesViewModel extends SummaryListRowHelper {

  def apply(file: UploadJourney, index: Int): Option[UploadedFilesViewModel] = file match {
    case UploadJourney(reference, _, _, Some(fileDetails), _, _, _) =>
      Some(UploadedFilesViewModel(
        fileReference = reference,
        index = index,
        filename = fileDetails.fileName,
        fileSize = fileDetails.size
      ))
    case _ => None
  }

  def apply(files: Seq[UploadJourney]): Seq[UploadedFilesViewModel] =
    files.zipWithIndex.flatMap { case (file, index) => apply(file, index) }

  def toSummaryListRows(files: Seq[UploadedFilesViewModel], isAgent: Boolean, is2ndStageAppeal: Boolean)
                       (implicit messages: Messages): Seq[SummaryListRow] =
    files.map { file =>
      summaryListRow(
        label = messages("uploadCheckAnswers.nonJs.summaryKey", file.index + 1),
        value = HtmlFormat.escape(file.filename),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              href = upscanRoutes.UpscanRemoveFileController.onPageLoad(file.fileReference, file.index + 1, isAgent, is2ndStageAppeal).url,
              content = Text(messages("common.remove")),
              visuallyHiddenText = Some(messages("uploadCheckAnswers.nonJs.summaryKey", file.index + 1))
            )
          )
        ))
      )
    }
}
