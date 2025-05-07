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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.upscan

import fixtures.FileUploadFixtures
import fixtures.messages.upscan.NonJsUploadCheckAnswersMessages
import fixtures.views.BaseSelectors
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadDocumentForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.UploadedFilesViewModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsUploadCheckAnswersView

class NonJsUploadCheckAnswersViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with FileUploadFixtures {

  lazy val uploadCheckAnswers: NonJsUploadCheckAnswersView = app.injector.instanceOf[NonJsUploadCheckAnswersView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

  object Selectors extends BaseSelectors

  Seq(NonJsUploadCheckAnswersMessages.English, NonJsUploadCheckAnswersMessages.Welsh).foreach { messagesForLanguage =>

    implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

    s"When rendering the File Upload Check Answers page in language '${messagesForLanguage.lang.name}'" when {

      s"the number of files which has been added is < ${appConfig.upscanMaxNumberOfFiles}" when {

        implicit val doc = asDocument(uploadCheckAnswers(
          UploadDocumentForm.form,
          UploadedFilesViewModel(Seq(callbackModel)),
          controllers.upscan.routes.UpscanCheckAnswersController.onSubmit()
        ))

        behave like pageWithExpectedElementsAndMessages(
          Selectors.title -> messagesForLanguage.headingAndTitleSingular,
          Selectors.h1 -> messagesForLanguage.headingAndTitleSingular,
          Selectors.summaryRowKey(1) -> messagesForLanguage.summaryRowKey(1),
          Selectors.summaryRowValue(1) -> callbackModel.uploadDetails.get.fileName,
          Selectors.summaryRowAction(1, 1) -> (messagesForLanguage.remove + " " + messagesForLanguage.summaryRowKey(1)),
          Selectors.legend -> messagesForLanguage.uploadAnotherFileLegend,
          Selectors.radio(1) -> messagesForLanguage.yes,
          Selectors.radio(2) -> messagesForLanguage.no,
          Selectors.button -> messagesForLanguage.continue
        )

        "have a remove link that redirects to the Remove File controller" in {
          doc.select(Selectors.summaryRowAction(1, 1)).attr("href") shouldBe controllers.upscan.routes.UpscanRemoveFileController.onSubmit(callbackModel.reference, 1).url
        }
      }

      s"the number of files which has been added is == ${appConfig.upscanMaxNumberOfFiles}" when {

        val files = (1 to appConfig.upscanMaxNumberOfFiles).map { i =>
          callbackModel.copy(uploadDetails = callbackModel.uploadDetails.map(_.copy(fileName = s"file$i.txt")))
        }

        implicit val doc = asDocument(uploadCheckAnswers(
          UploadDocumentForm.form,
          UploadedFilesViewModel(files),
          controllers.upscan.routes.UpscanCheckAnswersController.onSubmit()
        ))

        behave like pageWithExpectedElementsAndMessages(
          (1 to appConfig.upscanMaxNumberOfFiles).flatMap(i =>
            Seq(
              Selectors.summaryRowKey(i) -> messagesForLanguage.summaryRowKey(i),
              Selectors.summaryRowValue(i) -> s"file$i.txt",
              Selectors.summaryRowAction(i, 1) -> (messagesForLanguage.remove + " " + messagesForLanguage.summaryRowKey(i))
            )
          ) ++ Seq(
            Selectors.title -> messagesForLanguage.headingAndTitlePlural(appConfig.upscanMaxNumberOfFiles),
            Selectors.h1 -> messagesForLanguage.headingAndTitlePlural(appConfig.upscanMaxNumberOfFiles),
            Selectors.button -> messagesForLanguage.continue
          ): _*
        )

        behave like pageWithoutElementsRendered(
          Selectors.legend,
          Selectors.radio(1),
          Selectors.radio(2)
        )

        "have a remove link that redirects to the Remove File controller" in {
          doc.select(Selectors.summaryRowAction(1, 1)).attr("href") shouldBe controllers.upscan.routes.UpscanRemoveFileController.onSubmit(callbackModel.reference, 1).url
        }
      }
    }
  }
}
