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

import fixtures.FileUploadFixtures
import fixtures.messages.UploadedDocumentsSummaryMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.SummaryListRowHelper

class UploadedDocumentsSummarySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with FileUploadFixtures with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit lazy val user: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUerAnswersWithLSP.setAnswer(ReasonableExcusePage, Other))

  "UploadedDocumentsSummary" when {

    Seq(UploadedDocumentsSummaryMessages.English, UploadedDocumentsSummaryMessages.Welsh).foreach { messagesForLanguage =>

      s"being rendered in lang name '${messagesForLanguage.lang.name}'" when {

        implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

        "there's no uploaded files" should {

          "return None" in {
            UploadedDocumentsSummary.row(Seq()) shouldBe None
          }
        }

        "there's one uploaded file which is ready (unready ignored) (show action links == true)" when {

          "must output the expected row with ready filenames listed and a change link" in {

            UploadedDocumentsSummary.row(Seq(
              waitingFile,
              callbackModel,
              callbackModelFailed
            )) shouldBe Some(summaryListRow(
              label = messagesForLanguage.cyaKey,
              value = HtmlFormat.fill(Seq(
                Html("file1.txt")
              )),
              actions = Some(Actions(
                items = Seq(
                  ActionItem(
                    content = Text(messagesForLanguage.change),
                    href = controllers.upscan.routes.UpscanCheckAnswersController.onPageLoad().url,
                    visuallyHiddenText = Some(messagesForLanguage.cyaHidden)
                  ).withId("changeUploadedFiles")
                )
              ))
            ))
          }
        }

        "there's multiple uploaded files which are ready (unready ignored) (show action links == false)" when {

          "must output the expected row with ready filenames listed WITHOUT a change link" in {

            UploadedDocumentsSummary.row(
              Seq(
                waitingFile,
                callbackModel,
                callbackModel2,
                callbackModel2.copy(uploadDetails = callbackModel2.uploadDetails.map(_.copy(fileName = "file3.txt"))),
                callbackModelFailed
              ),
              showActionLinks = false
            ) shouldBe Some(summaryListRow(
              label = messagesForLanguage.cyaKey,
              value = HtmlFormat.fill(Seq(
                Html("file1.txt<br>"),
                Html("file2.txt<br>"),
                Html("file3.txt")
              )),
              actions = None
            ))
          }
        }
      }
    }
  }
}
