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
      redirectLocation(result) shouldBe Some("/view-or-appeal-penalty/self-assessment/appeal-start")

      cookies(result).get("PLAY_LANG").get.value shouldBe "en"

    }

    "switch to Welsh and redirect to fallback URL" in {

      val result = testAction.switchToLanguage("cymraeg")(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/view-or-appeal-penalty/self-assessment/appeal-start")

      cookies(result).get("PLAY_LANG").get.value shouldBe "cy"

    }
  }

  "redirect to fallback in English when alternative language is provided" in {

    val result = testAction.switchToLanguage("spanish")(FakeRequest())
    status(result) shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some("/view-or-appeal-penalty/self-assessment/appeal-start")

    cookies(result).get("PLAY_LANG").get.value shouldBe "en"

  }

}
