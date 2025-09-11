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
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.is2ndStageAppeal
import fixtures.messages.upscan.NonJsRemoveFileMessages
import fixtures.views.BaseSelectors
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadDocumentForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.Mode.{CheckMode, NormalMode}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.viewmodels.UploadedFilesViewModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsRemoveFileView

class NonJsRemoveFileViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with FileUploadFixtures {

  lazy val uploadCheckAnswers: NonJsRemoveFileView = app.injector.instanceOf[NonJsRemoveFileView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

  object Selectors extends BaseSelectors

  Seq(NormalMode, CheckMode).foreach { mode =>
    Seq(NonJsRemoveFileMessages.English, NonJsRemoveFileMessages.Welsh).foreach { messagesForLanguage =>

      implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"When rendering the Remove File page in $mode with language '${messagesForLanguage.lang.name}'" should {

        val fileIndex = 1

        implicit val doc = asDocument(uploadCheckAnswers(
          UploadDocumentForm.form,
          UploadedFilesViewModel(callbackModel, fileIndex).get,
          controllers.upscan.routes.UpscanRemoveFileController.onSubmit(callbackModel.reference, fileIndex, isAgent, is2ndStageAppeal, mode)
        ))

        behave like pageWithExpectedElementsAndMessages(
          Selectors.title -> messagesForLanguage.headingAndTitle(fileIndex),
          Selectors.legend -> messagesForLanguage.headingAndTitle(fileIndex),
          Selectors.hint -> messagesForLanguage.filenameHint(callbackModel.uploadDetails.get.fileName),
          Selectors.radio(1) -> messagesForLanguage.yes,
          Selectors.radio(2) -> messagesForLanguage.no,
          Selectors.button -> messagesForLanguage.continue
        )
      }
    }
  }
}
