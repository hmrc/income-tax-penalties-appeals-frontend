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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import fixtures.FileUploadFixtures
import fixtures.messages._
import fixtures.views.BaseSelectors
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.OK
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.AgentClientEnum
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.{FileUploadJourneyRepository, UserAnswersRepository}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.DateFormatter.dateToString
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import java.time.LocalDate

class ViewAppealDetailsControllerISpec extends ControllerISpecHelper with FileUploadFixtures {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]

  lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]
  lazy val fileUploadRepo: FileUploadJourneyRepository = app.injector.instanceOf[FileUploadJourneyRepository]

  class Setup(userAnswers: UserAnswers, isAgent: Boolean) {
    userAnswersRepo.collection.deleteMany(Document()).toFuture().futureValue
    userAnswersRepo.upsertUserAnswer(userAnswers).futureValue
    stubAuthRequests(isAgent)
  }

  object Selectors extends BaseSelectors

  Seq(
    false,
    true
  ).foreach { case isAgent =>

    val url = if(isAgent) "/agent-appeal-details" else "/appeal-details"

    s"When the user is an ${if (isAgent) "Agent" else "Individual"}" when {

      Seq(
        (
          PrintAppealMessages.English,
          ReasonableExcuseMessages.English,
          WhoPlannedToSubmitMessages.English,
          WhatCausedYouToMissDeadlineMessages.English,
          WhenDidEventHappenMessages.English,
          LateAppealMessages.English,
          UploadedDocumentsSummaryMessages.English,
          MissedDeadlineReasonMessages.English,
          ExtraEvidenceMessages.English
        ),
        (
          PrintAppealMessages.Welsh,
          ReasonableExcuseMessages.Welsh,
          WhoPlannedToSubmitMessages.Welsh,
          WhatCausedYouToMissDeadlineMessages.Welsh,
          WhenDidEventHappenMessages.Welsh,
          LateAppealMessages.Welsh,
          UploadedDocumentsSummaryMessages.Welsh,
          MissedDeadlineReasonMessages.Welsh,
          ExtraEvidenceMessages.Welsh
        )
      ).foreach { case (
        messagesForLanguage,
        reasonableExcuseMessages,
        whoPlannedMessaeges,
        whatCausedMessages,
        whenEventHappenedMessages,
        lateAppealMessages,
        fileUploadMessages,
        missedDeadlineMessages,
        extraEvidenceMessages) =>

        implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
        val userAnswersWithReason = emptyUserAnswersWithLSP
          .setAnswer(ReasonableExcusePage, Bereavement)
          .setAnswer(HonestyDeclarationPage, true)
          .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 1, 1))

        s"when Accept-Language is '${messagesForLanguage.lang.code}'" when {

          s"GET $url" when {

            if (!isAgent) {
              testNavBar(url = url) {
                userAnswersRepo.upsertUserAnswer(userAnswersWithReason).futureValue
              }
            }

            s"reason is NOT '$Other'" should {

              val baseUserAnswers = emptyUserAnswersWithLSP
                .setAnswer(ReasonableExcusePage, Bereavement)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 2, 1))
                .setAnswer(LateAppealPage, "I was late")

              val userAnswers = if (!isAgent) baseUserAnswers else baseUserAnswers
                .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
                .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)

              "return an OK with a view with expected data" in new Setup(userAnswers, isAgent) {

                val result: WSResponse = get(
                  uri = url,
                  isAgent = isAgent,
                  cookie = if (messagesForLanguage.lang.code == "cy") cyLangCookie else enLangCookie
                )
                result.status shouldBe OK

                val document: nodes.Document = Jsoup.parse(result.body)
                document.select(Selectors.title).text() should include(messagesForLanguage.headingAndTitle)
                document.select(Selectors.h1).text() shouldBe messagesForLanguage.headingAndTitle
                document.select(Selectors.summaryRowKey(1)).text() shouldBe messagesForLanguage.nino
                document.select(Selectors.summaryRowValue(1)).text() shouldBe sessionData.nino
                document.select(Selectors.summaryRowKey(2)).text() shouldBe messagesForLanguage.appealDate
                document.select(Selectors.summaryRowValue(2)).text() shouldBe dateToString(timeMachine.getCurrentDate, withNBSP = false)
                document.select(Selectors.summaryRowKey(3)).text() shouldBe messagesForLanguage.penaltyAppealed
                document.select(Selectors.summaryRowValue(3)).text() shouldBe messagesForLanguage.lspCaption(
                  dateToString(lateSubmissionAppealData.startDate, withNBSP = false),
                  dateToString(lateSubmissionAppealData.endDate, withNBSP = false)
                )
                if (isAgent) {
                  document.select(Selectors.summaryRowKey(4)).text() shouldBe whoPlannedMessaeges.cyaKey
                  document.select(Selectors.summaryRowValue(4)).text() shouldBe whoPlannedMessaeges.agent
                  document.select(Selectors.summaryRowKey(5)).text() shouldBe whatCausedMessages.cyaKey
                  document.select(Selectors.summaryRowValue(5)).text() shouldBe whatCausedMessages.client
                  document.select(Selectors.summaryRowKey(6)).text() shouldBe reasonableExcuseMessages.cyaKey
                  document.select(Selectors.summaryRowValue(6)).text() shouldBe reasonableExcuseMessages.bereavement
                  document.select(Selectors.summaryRowKey(7)).text() shouldBe whenEventHappenedMessages.cyaKey(Bereavement)
                  document.select(Selectors.summaryRowValue(7)).text() shouldBe dateToString(LocalDate.of(2025, 2, 1), withNBSP = false)
                  document.select(Selectors.summaryRowKey(8)).text() shouldBe lateAppealMessages.cyaKey(appConfig.bereavementLateDays)
                  document.select(Selectors.summaryRowValue(8)).text() shouldBe "I was late"
                } else {
                  document.select(Selectors.summaryRowKey(4)).text() shouldBe reasonableExcuseMessages.cyaKey
                  document.select(Selectors.summaryRowValue(4)).text() shouldBe reasonableExcuseMessages.bereavement
                  document.select(Selectors.summaryRowKey(5)).text() shouldBe whenEventHappenedMessages.cyaKey(Bereavement)
                  document.select(Selectors.summaryRowValue(5)).text() shouldBe dateToString(LocalDate.of(2025, 2, 1), withNBSP = false)
                  document.select(Selectors.summaryRowKey(6)).text() shouldBe lateAppealMessages.cyaKey(appConfig.bereavementLateDays)
                  document.select(Selectors.summaryRowValue(6)).text() shouldBe "I was late"
                }
              }
            }

            s"reason is '$Other'" should {

              val baseUserAnswers = emptyUserAnswersWithLSP
                .setAnswer(ReasonableExcusePage, Other)
                .setAnswer(HonestyDeclarationPage, true)
                .setAnswer(WhenDidEventHappenPage, LocalDate.of(2025, 2, 1))
                .setAnswer(MissedDeadlineReasonPage, "Forgot")
                .setAnswer(ExtraEvidencePage, true)

              val userAnswers = if (!isAgent) baseUserAnswers else baseUserAnswers
                .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
                .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)

              "return an OK with a view with expected data" in new Setup(userAnswers, isAgent) {

                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel).futureValue
                fileUploadRepo.upsertFileUpload(testJourneyId, callbackModel2).futureValue

                val result: WSResponse = get(
                  uri = url,
                  isAgent = isAgent,
                  cookie = if (messagesForLanguage.lang.code == "cy") cyLangCookie else enLangCookie
                )
                result.status shouldBe OK

                val document: nodes.Document = Jsoup.parse(result.body)
                document.select(Selectors.title).text() should include(messagesForLanguage.headingAndTitle)
                document.select(Selectors.h1).text() shouldBe messagesForLanguage.headingAndTitle
                document.select(Selectors.summaryRowKey(1)).text() shouldBe messagesForLanguage.nino
                document.select(Selectors.summaryRowValue(1)).text() shouldBe sessionData.nino
                document.select(Selectors.summaryRowKey(2)).text() shouldBe messagesForLanguage.appealDate
                document.select(Selectors.summaryRowValue(2)).text() shouldBe dateToString(timeMachine.getCurrentDate, withNBSP = false)
                document.select(Selectors.summaryRowKey(3)).text() shouldBe messagesForLanguage.penaltyAppealed
                document.select(Selectors.summaryRowValue(3)).text() shouldBe messagesForLanguage.lspCaption(
                  dateToString(lateSubmissionAppealData.startDate, withNBSP = false),
                  dateToString(lateSubmissionAppealData.endDate, withNBSP = false)
                )
                if (isAgent) {
                  document.select(Selectors.summaryRowKey(4)).text() shouldBe whoPlannedMessaeges.cyaKey
                  document.select(Selectors.summaryRowValue(4)).text() shouldBe whoPlannedMessaeges.agent
                  document.select(Selectors.summaryRowKey(5)).text() shouldBe whatCausedMessages.cyaKey
                  document.select(Selectors.summaryRowValue(5)).text() shouldBe whatCausedMessages.client
                  document.select(Selectors.summaryRowKey(6)).text() shouldBe reasonableExcuseMessages.cyaKey
                  document.select(Selectors.summaryRowValue(6)).text() shouldBe reasonableExcuseMessages.other
                  document.select(Selectors.summaryRowKey(7)).text() shouldBe whenEventHappenedMessages.cyaKey(Other, isAgent = isAgent, wasClientInformationIssue = true)
                  document.select(Selectors.summaryRowValue(7)).text() shouldBe dateToString(LocalDate.of(2025, 2, 1), withNBSP = false)
                  document.select(Selectors.summaryRowKey(8)).text() shouldBe missedDeadlineMessages.cyaKey(isLPP = false, is2ndStageAppeal = false, isJointAppeal = false)
                  document.select(Selectors.summaryRowValue(8)).text() shouldBe "Forgot"
                  document.select(Selectors.summaryRowKey(9)).text() shouldBe extraEvidenceMessages.cyaKey
                  document.select(Selectors.summaryRowValue(9)).text() shouldBe extraEvidenceMessages.yes
                  document.select(Selectors.summaryRowKey(10)).text() shouldBe fileUploadMessages.cyaKey
                  document.select(Selectors.summaryRowValue(10)).text() shouldBe "file1.txt file2.txt"

                } else {
                  document.select(Selectors.summaryRowKey(4)).text() shouldBe reasonableExcuseMessages.cyaKey
                  document.select(Selectors.summaryRowValue(4)).text() shouldBe reasonableExcuseMessages.other
                  document.select(Selectors.summaryRowKey(5)).text() shouldBe whenEventHappenedMessages.cyaKey(Other)
                  document.select(Selectors.summaryRowValue(5)).text() shouldBe dateToString(LocalDate.of(2025, 2, 1), withNBSP = false)
                  document.select(Selectors.summaryRowKey(6)).text() shouldBe missedDeadlineMessages.cyaKey(isLPP = false, is2ndStageAppeal = false, isJointAppeal = false)
                  document.select(Selectors.summaryRowValue(6)).text() shouldBe "Forgot"
                  document.select(Selectors.summaryRowKey(7)).text() shouldBe extraEvidenceMessages.cyaKey
                  document.select(Selectors.summaryRowValue(7)).text() shouldBe extraEvidenceMessages.yes
                  document.select(Selectors.summaryRowKey(8)).text() shouldBe fileUploadMessages.cyaKey
                  document.select(Selectors.summaryRowValue(8)).text() shouldBe "file1.txt file2.txt"
                }
              }
            }
          }
        }
      }
    }
  }
}
