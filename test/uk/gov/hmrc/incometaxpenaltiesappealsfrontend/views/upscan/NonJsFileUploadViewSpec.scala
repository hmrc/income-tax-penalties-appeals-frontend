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
import fixtures.messages.SupportedFileTypeMessages
import fixtures.messages.upscan.NonJsFileUploadMessages
import fixtures.views.BaseSelectors
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.upscan.UploadDocumentForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsFileUploadView

class NonJsFileUploadViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with FileUploadFixtures {

  lazy val uploadFilePage: NonJsFileUploadView = app.injector.instanceOf[NonJsFileUploadView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswers)

  object Selectors extends BaseSelectors

  Seq(
    NonJsFileUploadMessages.English -> SupportedFileTypeMessages.English,
    NonJsFileUploadMessages.Welsh -> SupportedFileTypeMessages.Welsh
  ).foreach { case (messagesForLanguage, fileTypeMessages) =>

    s"When rendering the File Upload page in language '${messagesForLanguage.lang.name}'" should {

      implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
      implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

      behave like pageWithExpectedElementsAndMessages(
        Selectors.title -> messagesForLanguage.headingAndTitle,
        Selectors.h1 -> messagesForLanguage.headingAndTitle,
        Selectors.p(1) -> messagesForLanguage.p1,
        Selectors.p(2) -> messagesForLanguage.p2,
        Selectors.p(3) -> messagesForLanguage.p3(appConfig.upscanMaxNumberOfFiles),
        Selectors.p(4) -> messagesForLanguage.p4(appConfig.upscanMaxFileSizeMB),
        Selectors.detailsSummary -> fileTypeMessages.summaryHeading,
        concat(Selectors.details, Selectors.p(1)) -> fileTypeMessages.p1,
        concat(Selectors.details, Selectors.bullet(1)) -> fileTypeMessages.bullet1,
        concat(Selectors.details, Selectors.bullet(2)) -> fileTypeMessages.bullet2,
        concat(Selectors.details, Selectors.bullet(3)) -> fileTypeMessages.bullet3,
        concat(Selectors.details, Selectors.bullet(4)) -> fileTypeMessages.bullet4,
        concat(Selectors.details, Selectors.bullet(5)) -> fileTypeMessages.bullet5,
        Selectors.label(UploadDocumentForm.key) -> messagesForLanguage.label,
        Selectors.button -> messagesForLanguage.continue
      )
    }
  }
}
