/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.http.Status.OK
import org.jsoup.Jsoup
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}


class UnauthorisedErrorControllerISpec extends ComponentSpecHelper with ViewSpecHelper {
  
  "GET /unauthorised" should {
    "return OK and render the unauthorised page fot an individual" in {
      val result = get("/unauthorised")
      result.status shouldBe OK
    }
    "return OK and render the unauthorised page fot an agent" in {
      val result = get("/unauthorised")
      result.status shouldBe OK
    }
  }

}
