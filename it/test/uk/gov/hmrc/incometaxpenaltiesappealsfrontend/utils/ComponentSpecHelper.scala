/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import org.mongodb.scala.Document
import org.mongodb.scala.result.DeleteResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSCookie, WSRequest, WSResponse}
import play.api.mvc.{Cookie, Session, SessionCookieBaker}
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

import scala.concurrent.Future

trait ComponentSpecHelper
  extends AnyWordSpec
    with Matchers
    with CustomMatchers
    with WiremockHelper
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite {

  lazy val injector: Injector = app.injector

  def extraConfig(): Map[String, String] = Map.empty

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig())
    .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
    .build()

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"

  def config: Map[String, String] = Map(
    "microservice.services.penalties.host" -> mockHost,
    "microservice.services.penalties.port" -> mockPort,
    "microservice.services.income-tax-penalties-stubs.host" -> mockHost,
    "microservice.services.income-tax-penalties-stubs.port" -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.message-frontend.host" -> mockHost,
    "microservice.services.message-frontend.port" -> mockPort,
    "microservice.services.business-tax-account.host" -> mockHost,
    "microservice.services.business-tax-account.port" -> mockPort,
    "auditing.enabled" -> "true",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck"
  )

  implicit val ws: WSClient = app.injector.instanceOf[WSClient]

  override def beforeAll(): Unit = {
    startWiremock()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    resetWiremock()
    super.beforeEach()
  }

  def get[T](uri: String, isLate: Boolean = true, isAgent: Boolean = false, reasonableExcuse: Option[String] = None, cookie: WSCookie = enLangCookie, queryParams: Map[String, String] = Map.empty): WSResponse = {
    await(buildClient(uri)
      .withHttpHeaders("Authorization" -> "Bearer 123")
      .withCookies(cookie, mockSessionCookie(isAgent, reasonableExcuse))
      .withQueryStringParameters(queryParams.toSeq: _*)
      .get())
  }

  def post[T](uri: String, isLate: Boolean = true, isAgent: Boolean = false, reasonableExcuse: Option[String] = None, cookie: WSCookie = enLangCookie)(body: T)(implicit writes: Writes[T]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer 123")
        .withCookies(cookie, mockSessionCookie(isAgent, reasonableExcuse))
        .post(writes.writes(body).toString())
    )
  }

  def put[T](uri: String, isLate: Boolean = true ,isAgent: Boolean = false, reasonableExcuse: Option[String] = None)(body: T)(implicit writes: Writes[T]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer 123")
        .withCookies(mockSessionCookie(isAgent, reasonableExcuse))
        .put(writes.writes(body).toString())
    )
  }

  def delete[T](uri: String, isLate: Boolean = true, isAgent: Boolean = false, reasonableExcuse: Option[String] = None): WSResponse = {
    await(buildClient(uri).withHttpHeaders("Authorization" -> "Bearer 123")
      .withCookies(mockSessionCookie(isAgent, reasonableExcuse))
      .delete())
  }

  val baseUrl: String = "/view-or-appeal-penalty/self-assessment"

  private def buildClient(path: String): WSRequest =
    ws.url(s"http://localhost:$port$baseUrl$path").withFollowRedirects(false)


  val cyLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "cy")

  val enLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "en")

  def mockSessionCookie(isAgent: Boolean, reasonableExcuse: Option[String]): WSCookie = {

    def makeSessionCookie(session: Session): Cookie = {
      val cookieCrypto = app.injector.instanceOf[SessionCookieCrypto]
      val cookieBaker = app.injector.instanceOf[SessionCookieBaker]
      val sessionCookie = cookieBaker.encodeAsCookie(session)
      val encryptedValue = cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value))
      sessionCookie.copy(value = encryptedValue.value)
    }

    val mockSession = Session(Map(
      SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
      SessionKeys.authToken -> "mock-bearer-token",
      SessionKeys.sessionId -> "mock-sessionid"
    ) ++ {if(isAgent) Map(IncomeTaxSessionKeys.agentSessionMtditid -> "123456789") else Map.empty}
      ++ {if(reasonableExcuse.isDefined) Map(IncomeTaxSessionKeys.reasonableExcuse -> reasonableExcuse.getOrElse("")) else Map.empty}
    )

    val cookie = makeSessionCookie(mockSession)

    new WSCookie() {
      override def name: String = cookie.name

      override def value: String = cookie.value

      override def domain: Option[String] = cookie.domain

      override def path: Option[String] = Some(cookie.path)

      override def maxAge: Option[Long] = cookie.maxAge.map(_.toLong)

      override def secure: Boolean = cookie.secure

      override def httpOnly: Boolean = cookie.httpOnly
    }
  }

  def deleteAll[A<: PlayMongoRepository[_]](repository: A): Future[DeleteResult] =
    repository
      .collection
      .deleteMany(filter = Document())
      .toFuture()

}
