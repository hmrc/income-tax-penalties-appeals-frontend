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

import fixtures.messages.MissedDeadlineReasonMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig

class MissedDeadlineReasonFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with FormBehaviours {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(MissedDeadlineReasonMessages.English, MissedDeadlineReasonMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      for (isLPP <- Seq(true, false)) {
        for (isSecondStageAppeal <- Seq(true, false)) {
          s"MissedDeadlineReasonForm with LPP='$isLPP' and second stage='$isSecondStageAppeal'" when {

            for (isMultipleAppeal <- Seq(true, false)) {
              s"multiple appeal='$isMultipleAppeal'" should {
                if (isSecondStageAppeal && isMultipleAppeal && isLPP) {
                  behave like mandatoryField(
                    form = MissedDeadlineReasonForm.form(isLPP = isLPP, isSecondStageAppeal = isSecondStageAppeal, isMultipleAppeal = isMultipleAppeal),
                    fieldName = MissedDeadlineReasonForm.key,
                    requiredError = FormError(MissedDeadlineReasonForm.key, messagesForLanguage.errorRequiredMultiple)
                  )
                }
              }
            }
            behave like mandatoryField(
              form = MissedDeadlineReasonForm.form(isLPP = isLPP, isSecondStageAppeal = isSecondStageAppeal, isMultipleAppeal = false),
              fieldName = MissedDeadlineReasonForm.key,
              requiredError = FormError(MissedDeadlineReasonForm.key, if (isSecondStageAppeal) {
                messagesForLanguage.errorRequiredSecondStage
              } else {
                messagesForLanguage.errorRequired(isLPP, isSecondStageAppeal)
              })
            )
          }
        }
      }
    }
  }
}
