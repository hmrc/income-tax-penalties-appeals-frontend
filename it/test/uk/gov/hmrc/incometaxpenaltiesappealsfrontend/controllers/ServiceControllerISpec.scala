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

import org.jsoup.Jsoup
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

import java.net.URLEncoder

class ServiceControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {


  "GET /view-or-appeal-penalty/self-assessment/sign-out" should {
    "redirect to sign-out route with the continue URL set to the feedback survey" in {
      val appConfig = app.injector.instanceOf[AppConfig]
      stubAuth(OK, successfulIndividualAuthResponse)

      val result = get("/logout")

      val expectedRedirectUrl = s"${appConfig.signOutUrl}"

      result.status shouldBe SEE_OTHER
      result.header(LOCATION) shouldBe Some(expectedRedirectUrl)
    }
  }

  "GET /view-or-appeal-penalty/self-assessment/keep-alive" should {
    "return No-Content" in {
      stubAuth(OK, successfulIndividualAuthResponse)

      val result = get("/keep-alive")
      result.status shouldBe NO_CONTENT
    }
  }
  "GET /view-or-appeal-penalty/self-assessment/" should {
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/")

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/", isAgent = true)

        result.status shouldBe OK
      }
    }
  }
}
