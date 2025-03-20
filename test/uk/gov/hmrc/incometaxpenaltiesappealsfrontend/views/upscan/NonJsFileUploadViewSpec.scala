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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.JointAppealPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.upscan.NonJsFileUploadView

class NonJsFileUploadViewSpec extends ViewBehaviours with GuiceOneAppPerSuite with FileUploadFixtures {

  lazy val uploadFilePage: NonJsFileUploadView = app.injector.instanceOf[NonJsFileUploadView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object Selectors extends BaseSelectors

  Seq(
    NonJsFileUploadMessages.English -> SupportedFileTypeMessages.English,
    NonJsFileUploadMessages.Welsh -> SupportedFileTypeMessages.Welsh
  ).foreach { case (messagesForLanguage, fileTypeMessages) =>

    s"When rendering the File Upload page in language '${messagesForLanguage.lang.name}'" when {

      "is a 1st Stage Appeal" when {

        "the penalty type is LSP" when {

          implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswersWithLSP)

          implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
          implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

          behave like pageWithExpectedElementsAndMessages(
            Selectors.title -> messagesForLanguage.headingAndTitle,
            Selectors.h1 -> messagesForLanguage.headingAndTitle,
            Selectors.p(1) -> messagesForLanguage.p1,
            Selectors.p(2) -> messagesForLanguage.p2LSP,
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

        "the penalty type is LPP (single penalty)" when {

          implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
            emptyUserAnswersWithLPP
          )

          implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
          implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

          behave like pageWithExpectedElementsAndMessages(
            Selectors.title -> messagesForLanguage.headingAndTitle,
            Selectors.h1 -> messagesForLanguage.headingAndTitle,
            Selectors.p(1) -> messagesForLanguage.p1,
            Selectors.p(2) -> messagesForLanguage.p2LPP,
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

        "the penalty type is LPP (multiple penalty - joint appeal)" when {

          implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
            emptyUserAnswersWithMultipleLPPs.setAnswer(JointAppealPage, true)
          )

          implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
          implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

          behave like pageWithExpectedElementsAndMessages(
            Selectors.title -> messagesForLanguage.headingAndTitle,
            Selectors.h1 -> messagesForLanguage.headingAndTitle,
            Selectors.p(1) -> messagesForLanguage.p1Joint,
            Selectors.p(2) -> messagesForLanguage.p2LPP,
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

      "is a 2nd Stage Appeal" when {

        "the penalty type is LSP" when {

          implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
            emptyUserAnswersWithLSP2ndStage
          )

          implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
          implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

          behave like pageWithExpectedElementsAndMessages(
            Selectors.title -> messagesForLanguage.headingAndTitleReview,
            Selectors.h1 -> messagesForLanguage.headingAndTitleReview,
            Selectors.p(1) -> messagesForLanguage.p1Review,
            Selectors.p(2) -> messagesForLanguage.p2LSPReview,
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

        "the penalty type is LPP (single penalty)" when {

          implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
            emptyUserAnswersWithLPP2ndStage
          )

          implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
          implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

          behave like pageWithExpectedElementsAndMessages(
            Selectors.title -> messagesForLanguage.headingAndTitleReview,
            Selectors.h1 -> messagesForLanguage.headingAndTitleReview,
            Selectors.p(1) -> messagesForLanguage.p1Review,
            Selectors.p(2) -> messagesForLanguage.p2LPPReview,
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

        "the penalty type is LPP (multiple penalties - joint appeal)" when {

          implicit lazy val request: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(
            emptyUserAnswersWithMultipleLPPs2ndStage.setAnswer(JointAppealPage, true)
          )

          implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
          implicit val doc = asDocument(uploadFilePage(UploadDocumentForm.form, uploadFields))

          behave like pageWithExpectedElementsAndMessages(
            Selectors.title -> messagesForLanguage.headingAndTitleReview,
            Selectors.h1 -> messagesForLanguage.headingAndTitleReview,
            Selectors.p(1) -> messagesForLanguage.p1JointReview,
            Selectors.p(2) -> messagesForLanguage.p2LPPReview,
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
  }
}
