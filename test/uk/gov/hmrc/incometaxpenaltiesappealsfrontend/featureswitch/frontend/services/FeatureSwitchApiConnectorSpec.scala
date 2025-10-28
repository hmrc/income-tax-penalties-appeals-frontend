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


import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
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
import izumi.reflect.Tag
import play.api.libs.ws.BodyWritable

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class FeatureSwitchApiConnectorSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockFactory with Injecting {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val mockResponse: HttpResponse = mock[HttpResponse]

  val testAction = new FeatureSwitchApiConnector(mockHttpClient)

  val testFeatureSwitches: Seq[FeatureSwitchSetting] = Seq(
    FeatureSwitchSetting("config1", "feature1", isEnabled = true),
    FeatureSwitchSetting("config2", "feature2", isEnabled = false)
  )

  val testUpdatedFeatureSwitches: Seq[FeatureSwitchSetting] = Seq(
    FeatureSwitchSetting("config1", "feature1", isEnabled = false),
    FeatureSwitchSetting("config2", "feature2", isEnabled = true)
  )

  "retrieveFeatureSwitches" should {

    "Retrieve Feature Switch Settings when OK and has a valid Json Body" in {

      val url = new URL("http://localhost:9000/test")
      val jsonResponse = Json.toJson(testFeatureSwitches)

      (mockHttpClient.get(_: URL)(_:HeaderCarrier)).expects(url, *).returning(mockRequestBuilder)
      (mockRequestBuilder.execute[HttpResponse](_: HttpReads[HttpResponse], _: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(mockResponse))
      (() => mockResponse.status).expects().returning(OK)
      (() => mockResponse.json).expects().returning(jsonResponse)

      val result = await(testAction.retrieveFeatureSwitches(url.toString))
      result shouldEqual testFeatureSwitches

    }

    "Throw Exception when not OK" in {

      val url = new URL("http://localhost:9000/test")

      (mockHttpClient.get(_: URL)(_:HeaderCarrier)).expects(url, *).returning(mockRequestBuilder)
      (mockRequestBuilder.execute[HttpResponse](_: HttpReads[HttpResponse], _: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(mockResponse))

      (() => mockResponse.status).expects().returning(INTERNAL_SERVER_ERROR)

      val result = the[Exception] thrownBy await(testAction.retrieveFeatureSwitches(url.toString))
      result.getMessage should include("Could not retrieve feature switches")

    }

    "Throw Exception when the Json Body is Invalid" in {

      val url = new URL("http://localhost:9000/test")
      val invalidJson = Json.obj("return" -> "invalidData")


      (mockHttpClient.get(_: URL)(_:HeaderCarrier)).expects(url, *).returning(mockRequestBuilder)
      (mockRequestBuilder.execute[HttpResponse](_: HttpReads[HttpResponse], _: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(mockResponse))
      (() => mockResponse.status).expects().returning(OK)
      (() => mockResponse.json).expects().returning(invalidJson)

      val result = the[Exception] thrownBy await(testAction.retrieveFeatureSwitches(url.toString))
      result.getMessage should include("(,List(JsonValidationError(List(error.expected.jsarray),List())))")

    }

  }


  "updateFeatureSwitches" should {

    "Retrieve Feature Switch Settings when OK and has a valid Json Body" in {

      val url = new URL("http://localhost:9000/test")
      val jsonResponse = Json.toJson(testUpdatedFeatureSwitches)

      (mockHttpClient.post(_: URL)(_:HeaderCarrier)).expects(url, *).returning(mockRequestBuilder)
      (mockRequestBuilder.withBody[JsValue](_: JsValue)(_: BodyWritable[JsValue], _: Tag[JsValue], _: ExecutionContext)).expects(*,*,*,*).returning(mockRequestBuilder)
      (mockRequestBuilder.execute[HttpResponse](_: HttpReads[HttpResponse], _: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(mockResponse))
      (() => mockResponse.status).expects().returning(OK)
      (() => mockResponse.json).expects().returning(jsonResponse)

      val result = await(testAction.updateFeatureSwitches(url.toString, testFeatureSwitches))
      result shouldEqual testUpdatedFeatureSwitches

    }

    "Throw Exception when not OK" in {

      val url = new URL("http://localhost:9000/test")

      (mockHttpClient.post(_: URL)(_:HeaderCarrier)).expects(url, *).returning(mockRequestBuilder)
      (mockRequestBuilder.withBody[JsValue](_: JsValue)(_: BodyWritable[JsValue], _: Tag[JsValue], _: ExecutionContext)).expects(*,*,*,*).returning(mockRequestBuilder)
      (mockRequestBuilder.execute[HttpResponse](_: HttpReads[HttpResponse], _: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(mockResponse))
      (() => mockResponse.status).expects().returning(INTERNAL_SERVER_ERROR)

      val result = the[Exception] thrownBy await(testAction.updateFeatureSwitches(url.toString, testFeatureSwitches))
      result.getMessage should include("Could not update feature switches")

    }

    "Throw Exception when the Json Body is Invalid" in {

      val url = new URL("http://localhost:9000/test")
      val invalidJson = Json.obj("return" -> "invalidData")

      (mockHttpClient.post(_: URL)(_:HeaderCarrier)).expects(url, *).returning(mockRequestBuilder)
      (mockRequestBuilder.withBody[JsValue](_: JsValue)(_: BodyWritable[JsValue], _: Tag[JsValue], _: ExecutionContext)).expects(*,*,*,*).returning(mockRequestBuilder)
      (mockRequestBuilder.execute[HttpResponse](_: HttpReads[HttpResponse], _: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(mockResponse))
      (() => mockResponse.status).expects().returning(OK)
      (() => mockResponse.json).expects().returning(invalidJson)

      val result = the[Exception] thrownBy await(testAction.updateFeatureSwitches(url.toString, testFeatureSwitches))
      result.getMessage should include("(,List(JsonValidationError(List(error.expected.jsarray),List())))")

    }

  }

}
