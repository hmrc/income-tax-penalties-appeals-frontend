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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan

import fixtures.BaseFixtures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.internal.{routes => internalRoutes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.upscan.{routes => upscanRoutes}

class UpscanInitiateRequestSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BaseFixtures {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "calling .apply(journeyID: String, appConfig: AppConfig)" should {

    "construct a model with the correct values" in {

      val actualModel = UpscanInitiateRequest(testJourneyId, appConfig)
      val expectedModel = UpscanInitiateRequest(
        callbackUrl     = "http://localhost:9188" + internalRoutes.UpscanCallbackController.callbackFromUpscan(testJourneyId).url,
        successRedirect = Some("http://localhost:9188" + upscanRoutes.UpscanInitiateController.onSubmitSuccessRedirect("").url.replace("?key=", "")),
        errorRedirect   = Some("http://localhost:9188" + upscanRoutes.UpscanInitiateController.onPageLoad().url),
        minimumFileSize = Some(1),
        maximumFileSize = Some(6291456)
      )

      actualModel shouldBe expectedModel
    }
  }
}