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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.services


import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.FeatureSwitchSetting
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.connectors.FeatureSwitchApiConnector

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class FeatureSwitchApiConnectorSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockitoSugar with Injecting {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val mockResponse: HttpResponse = mock[HttpResponse]

  val testAction = new FeatureSwitchApiConnector(mockHttpClient)

  val testFeatureSwitches: Seq[FeatureSwitchSetting] = Seq(
    FeatureSwitchSetting("config1","feature1",isEnabled = true),
    FeatureSwitchSetting("config2","feature2",isEnabled = false)
  )

  val testUpdatedFeatureSwitches: Seq[FeatureSwitchSetting] = Seq(
    FeatureSwitchSetting("config1","feature1",isEnabled = false),
    FeatureSwitchSetting("config2","feature2",isEnabled = true)
  )

  "retrieveFeatureSwitches" should {

    "Retrieve Feature Switch Settings when OK and has a valid Json Body" in {

      val url = new URL("http://localhost:9000/test")
      val jsonResponse = Json.toJson(testFeatureSwitches)

      when(mockHttpClient.get(url)) thenReturn mockRequestBuilder

      when(
        mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])
      ) thenReturn Future.successful(mockResponse)

      when(mockResponse.status) thenReturn OK
      when(mockResponse.json) thenReturn jsonResponse

      val result = await(testAction.retrieveFeatureSwitches(url.toString))
      result shouldEqual testFeatureSwitches

    }

    "Throw Exception when not OK" in {

      val url = new URL("http://localhost:9000/test")

      when(mockHttpClient.get(url)) thenReturn mockRequestBuilder

      when(
        mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])
      ) thenReturn Future.successful(mockResponse)

      when(mockResponse.status) thenReturn INTERNAL_SERVER_ERROR

      val result = the[Exception] thrownBy await(testAction.retrieveFeatureSwitches(url.toString))
      result.getMessage should include ("Could not retrieve feature switches")

    }

    "Throw Exception when the Json Body is Invalid" in {

      val url = new URL("http://localhost:9000/test")
      val invalidJson = Json.obj("return" -> "invalidData")

      when(mockHttpClient.get(url)) thenReturn mockRequestBuilder

      when(
        mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])
      ) thenReturn Future.successful(mockResponse)

      when(mockResponse.status) thenReturn OK
      when(mockResponse.json) thenReturn invalidJson

      val result = the[Exception] thrownBy await(testAction.retrieveFeatureSwitches(url.toString))
      result.getMessage should include ("(,List(JsonValidationError(List(error.expected.jsarray),List())))")

    }

  }


  "updateFeatureSwitches" should {

    "Retrieve Feature Switch Settings when OK and has a valid Json Body" in {

      val url = new URL("http://localhost:9000/test")
      val jsonResponse = Json.toJson(testUpdatedFeatureSwitches)

      when(mockHttpClient.post(url)) thenReturn mockRequestBuilder

      when(mockRequestBuilder.withBody(any[JsValue])(any, any, any)) thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])
      ) thenReturn Future.successful(mockResponse)

      when(mockResponse.status) thenReturn OK
      when(mockResponse.json) thenReturn jsonResponse

      val result = await(testAction.updateFeatureSwitches(url.toString, testFeatureSwitches))
      result shouldEqual testUpdatedFeatureSwitches

    }

    "Throw Exception when not OK" in {

      val url = new URL("http://localhost:9000/test")

      when(mockHttpClient.post(url)) thenReturn mockRequestBuilder

      when(mockRequestBuilder.withBody(any[JsValue])(any, any, any)) thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])
      ) thenReturn Future.successful(mockResponse)

      when(mockResponse.status) thenReturn INTERNAL_SERVER_ERROR

      val result = the[Exception] thrownBy await(testAction.updateFeatureSwitches(url.toString,testFeatureSwitches))
      result.getMessage should include ("Could not update feature switches")

    }

    "Throw Exception when the Json Body is Invalid" in {

      val url = new URL("http://localhost:9000/test")
      val invalidJson = Json.obj("return" -> "invalidData")

      when(mockHttpClient.post(url)) thenReturn mockRequestBuilder
      when(mockRequestBuilder.withBody(any[JsValue])(any, any, any)) thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])
      ) thenReturn Future.successful(mockResponse)

      when(mockResponse.status) thenReturn OK
      when(mockResponse.json) thenReturn invalidJson

      val result = the[Exception] thrownBy await(testAction.updateFeatureSwitches(url.toString,testFeatureSwitches))
      result.getMessage should include ("(,List(JsonValidationError(List(error.expected.jsarray),List())))")

    }

  }

}
