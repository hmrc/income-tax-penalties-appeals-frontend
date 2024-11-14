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
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

class ServiceControllerISpec extends ComponentSpecHelper with ViewSpecHelper {



  "GET /" should {

    val result = get("/")
    val document = Jsoup.parse(result.body)

    "return an OK with a view" in {

      result.status shouldBe OK

    }

    "have the correct page elements" in {

      document.getServiceName.text() shouldBe "Appeal a Self Assessment penalty"
      document.title() shouldBe "Appeal a Self Assessment penalty - Appeal a Self Assessment penalty - GOV.UK"
      document.getH1Elements.text() shouldBe "Appeal a Self Assessment penalty"

    }
  }

}
