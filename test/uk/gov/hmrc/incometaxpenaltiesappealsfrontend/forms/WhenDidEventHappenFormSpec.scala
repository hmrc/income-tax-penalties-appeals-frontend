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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms

import fixtures.BaseFixtures
import fixtures.messages.WhenDidEventHappenMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{WhatCausedYouToMissDeadlinePage, WhoPlannedToSubmitPage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

class WhenDidEventHappenFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with FormBehaviours with BaseFixtures {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(WhenDidEventHappenMessages.English, WhenDidEventHappenMessages.Welsh).foreach { implicit messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      for(reason <- ReasonableExcuse.allReasonableExcuses) {

        for (isLPP <- Seq(true, false)) {

          for (isAgent <- Seq(true, false)) {

            s"WhenDidEventHappenForm with $reason and isLPP='$isLPP' and isAgent='$isAgent'" should {

              if(reason == Other) {

                if(isAgent) {

                  "testing content for scenario where Client didn't get information to the agent in time" should {

                    val userAnswers = emptyUserAnswersWithLSP
                      .setAnswer(WhoPlannedToSubmitPage, AgentClientEnum.agent)
                      .setAnswer(WhatCausedYouToMissDeadlinePage, AgentClientEnum.client)

                    implicit val agent: CurrentUserRequestWithAnswers[_] = agentUserRequestWithAnswers(userAnswers)

                    behave like dateForm(
                      form = WhenDidEventHappenForm.form(reason, isLPP),
                      fieldName = "date",
                      errorMessageKey = errorType => s"agent.whenDidEventHappen.$reason.clientInformation.date.error.$errorType",
                      errorMessageValue = (errorType, args) => messagesForLanguage.errorMessageConstructor(
                        reasonableExcuse = reason,
                        suffix = errorType,
                        isLPP = isLPP,
                        isAgent = true,
                        wasClientInformationIssue = true,
                        args = args
                      )
                    )
                  }
                } else { //Reason is 'other' BUT User is NOT an Agent

                  implicit val user: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswersWithLSP)

                  val infix = if(isLPP) ".lpp" else ".lsp"

                  behave like dateForm(
                    form = WhenDidEventHappenForm.form(reason, isLPP),
                    fieldName = "date",
                    errorMessageKey = errorType => s"whenDidEventHappen.$reason$infix.date.error.$errorType",
                    errorMessageValue = (errorType, args) => messagesForLanguage.errorMessageConstructor(
                      reasonableExcuse = reason,
                      suffix = errorType,
                      isLPP = isLPP,
                      args = args
                    )
                  )
                }
              } else { //Reason is NOT 'other'

                implicit val user: CurrentUserRequestWithAnswers[_] = userRequestWithAnswers(emptyUserAnswersWithLSP)

                behave like dateForm(
                  form = WhenDidEventHappenForm.form(reason, isLPP),
                  fieldName = "date",
                  errorMessageKey = errorType => s"whenDidEventHappen.$reason.date.error.$errorType",
                  errorMessageValue = (errorType, args) => messagesForLanguage.errorMessageConstructor(
                    reasonableExcuse = reason,
                    suffix = errorType,
                    isLPP = isLPP,
                    args = args
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
