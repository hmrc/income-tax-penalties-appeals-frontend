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

import fixtures.messages.WhenDidEventHappenMessages
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine

import java.time.LocalDate

class WhenDidEventHappenFormSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with FormBehaviours {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(WhenDidEventHappenMessages.English, WhenDidEventHappenMessages.Welsh).foreach { messagesForLanguage =>

    s"rendering the form in '${messagesForLanguage.lang.name}'" when {

      implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      val reasonsList: List[String] = List(
        "bereavementReason",
        "cessationReason",
        "crimeReason",
        "fireOrFloodReason",
        "healthReason",
        "technicalReason",
        "unexpectedHospitalReason",
        "otherReason"
      )

      for(reason <- reasonsList) {
        val form: Form[LocalDate] = WhenDidEventHappenForm.form(reason)(messages, appConfig, timeMachine)

        s"WhenDidEventHappenForm with $reason" should {
          behave like dateForm(form, "date", errorType => s"$reason.date.error.$errorType", messagesForLanguage)
        }
      }
    }
  }
}
