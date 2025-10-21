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

import fixtures.BaseFixtures
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{Document, SingleObservableFuture}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Writes
import play.api.libs.ws.{DefaultBodyWritables, DefaultWSCookie, WSClient, WSCookie, WSRequest, WSResponse}
import play.api.mvc.{Cookie, Session, SessionCookieBaker}
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait ComponentSpecHelper
  extends AnyWordSpec
    with Matchers
    with CustomMatchers
    with WiremockHelper
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite
    with BaseFixtures
    with DefaultBodyWritables {

  lazy val injector: Injector = app.injector

  lazy val mockUUIDGenerator: UUIDGenerator = new UUIDGenerator {
    override def generateUUID: String = testJourneyId
  }

  def extraConfig(): Map[String, String] = Map.empty

  lazy val baseApp = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig())
    .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
    .overrides(inject.bind[UUIDGenerator].toInstance(mockUUIDGenerator))

  override lazy val app: Application = baseApp.build()

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"

  val timeMachineDateFormatter1: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
  lazy val testDate: LocalDate = LocalDate.now()

  def config: Map[String, String] = Map(
    "microservice.services.penalties.host" -> mockHost,
    "microservice.services.penalties.port" -> mockPort,
    "microservice.services.income-tax-penalties-stubs.host" -> mockHost,
    "microservice.services.income-tax-penalties-stubs.port" -> mockPort,
    "microservice.services.income-tax-session-data.host" -> mockHost,
    "microservice.services.income-tax-session-data.port" -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.message-frontend.host" -> mockHost,
    "microservice.services.message-frontend.port" -> mockPort,
    "microservice.services.business-tax-account.host" -> mockHost,
    "microservice.services.business-tax-account.port" -> mockPort,
    "microservice.services.upscan-initiate.host" -> mockHost,
    "microservice.services.upscan-initiate.port" -> mockPort,
    "auditing.enabled" -> "true",
    "timemachine.date" -> testDate.format(timeMachineDateFormatter1),
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

  def get[T](
              uri: String,
              isLate: Boolean = true,
              isAgent: Boolean = false,
              cookie: WSCookie = enLangCookie,
              queryParams: Map[String, String] = Map.empty,
              origin: Option[String] = None,
              journeyId: Option[String] = Some(testJourneyId),
              otherSessionValues: Map[String,String] = Map(),
              otherHeaderValues: Map[String, String] = Map()): WSResponse = {
    await(buildClient(uri)
      .withHttpHeaders(otherHeaderValues.toSeq :+ "Authorization" -> "Bearer 123" :_*)
      .withCookies(cookie, mockSessionCookie(isAgent, origin = origin, journeyId, otherSessionValues = otherSessionValues))
      .withQueryStringParameters(queryParams.toSeq: _*)
      .get())
  }

  def post[T](uri: String,
              isLate: Boolean = true,
              isAgent: Boolean = false,
              cookie: WSCookie = enLangCookie,
              journeyId: Option[String] = Some(testJourneyId),
              otherSessionValues: Map[String,String] = Map(),
              otherHeaderValues: Map[String, String] = Map())(body: T)(implicit writes: Writes[T]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders(
          otherHeaderValues.toSeq ++ Seq(
            "Csrf-Token" -> "nocheck",
            "Content-Type" -> "application/json",
            "Authorization" -> "Bearer 123"
          ): _*
        )
        .withCookies(cookie, mockSessionCookie(isAgent, journeyId = journeyId, otherSessionValues = otherSessionValues))
        .post(writes.writes(body).toString())
    )
  }

  def put[T](uri: String,
             isLate: Boolean = true ,
             isAgent: Boolean = false,
             journeyId: Option[String] = Some(testJourneyId),
             otherSessionValues: Map[String,String] = Map(),
             otherHeaderValues: Map[String, String] = Map())(body: T)(implicit writes: Writes[T]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders(
          otherHeaderValues.toSeq ++ Seq(
            "Csrf-Token" -> "nocheck",
            "Content-Type" -> "application/json",
            "Authorization" -> "Bearer 123"
          ): _*
        )
        .withCookies(mockSessionCookie(isAgent, journeyId = journeyId, otherSessionValues = otherSessionValues))
        .put(writes.writes(body).toString())
    )
  }

  def delete[T](uri: String,
                isLate: Boolean = true,
                isAgent: Boolean = false,
                journeyId: Option[String] = Some(testJourneyId),
                otherSessionValues: Map[String,String] = Map(),
                otherHeaderValues: Map[String, String] = Map()): WSResponse = {
    await(buildClient(uri).withHttpHeaders(
        otherHeaderValues.toSeq ++ Seq(
          "Csrf-Token" -> "nocheck",
          "Authorization" -> "Bearer 123"
        ): _*
      )
      .withCookies(mockSessionCookie(isAgent, journeyId = journeyId, otherSessionValues = otherSessionValues))
      .delete())
  }

  val baseUrl: String = "/appeal-penalty/self-assessment"

  private def buildClient(path: String): WSRequest = {
    val pathBuilder = if(path.startsWith("/internal/")) path else s"$baseUrl$path"
    ws.url(s"http://localhost:$port$pathBuilder").withFollowRedirects(false)
  }

  val cyLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "cy")

  val enLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "en")

  def mockSessionCookie(isAgent: Boolean,
                        origin: Option[String] = None,
                        journeyId: Option[String] = None,
                        otherSessionValues: Map[String,String] = Map()): WSCookie = {

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
      ++ {if(origin.isDefined) Map(IncomeTaxSessionKeys.origin -> origin.get) else Map.empty}
      ++ {if(journeyId.isDefined) Map(IncomeTaxSessionKeys.journeyId -> journeyId.get) else Map.empty}
      ++ otherSessionValues
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

  def deleteAll[A<: PlayMongoRepository[_]](repository: A): DeleteResult =
    repository
      .collection
      .deleteMany(filter = Document())
      .toFuture()
      .futureValue

}
