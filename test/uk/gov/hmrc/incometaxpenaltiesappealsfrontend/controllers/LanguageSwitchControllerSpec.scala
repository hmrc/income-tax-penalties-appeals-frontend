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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers


import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.play.language.LanguageUtils


class LanguageSwitchControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with Injecting {
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]

  val testAction = new LanguageSwitchController(
    languageUtils = languageUtils,
    cc = stubControllerComponents()
  )

  "LanguageSwitchController" should {

    "switch to english and redirect to fallback URL" in {

      val result = testAction.switchToLanguage("english")(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/appeal-penalty/self-assessment/appeal-start")

      cookies(result).get("PLAY_LANG").get.value shouldBe "en"

    }

    "switch to Welsh and redirect to fallback URL" in {

      val result = testAction.switchToLanguage("cymraeg")(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/appeal-penalty/self-assessment/appeal-start")

      cookies(result).get("PLAY_LANG").get.value shouldBe "cy"

    }
  }

  "redirect to fallback in English when alternative language is provided" in {

    val result = testAction.switchToLanguage("spanish")(FakeRequest())
    status(result) shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some("/appeal-penalty/self-assessment/appeal-start")

    cookies(result).get("PLAY_LANG").get.value shouldBe "en"

  }

}
