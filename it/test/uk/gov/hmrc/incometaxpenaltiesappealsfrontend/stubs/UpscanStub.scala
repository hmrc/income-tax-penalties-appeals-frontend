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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.WiremockMethods

trait UpscanStub extends WiremockMethods {

  def stubUpscanInitiate[T](status: Int, body: T)(implicit writes: Writes[T]): StubMapping =
    when(POST, uri = "/upscan/v2/initiate").thenReturn(status, body)

}
