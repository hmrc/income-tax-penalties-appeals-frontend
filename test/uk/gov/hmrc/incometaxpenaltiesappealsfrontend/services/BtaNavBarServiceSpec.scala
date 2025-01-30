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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services

import fixtures.BtaNavContentFixture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.Helpers._
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.MockBtaNavLinksConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.btaNavBar.ListLink
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.NotificationBadgeCountUtil.notificationBadgeCount
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.navBar.BtaNavBar

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class BtaNavBarServiceSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite
  with MockBtaNavLinksConnector
  with BtaNavContentFixture {

  lazy val btaNavBar = app.injector.instanceOf[BtaNavBar]
  lazy val appConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  lazy val service: BtaNavBarService = new BtaNavBarService(mockBtaNavLinksConnector, btaNavBar)(appConfig)

  "BtaNavBarService" when {

    Seq(Cy, En).foreach { lang =>

      implicit val messages = messagesApi.preferred(Seq(Lang(lang.code)))
      val isWelsh = lang == Cy

      s"language is set to ${lang.name}" when {

        "calling .retrieveBtaLinksAndRenderNavBar()" when {

          "connector returns Some(NavContent) on successful response" should {

            "return the expected Html" in {
              mockGetBtaNavLinks()(Future.successful(Some(btaNavContent)))
              await(service.retrieveBtaLinksAndRenderNavBar()) shouldBe
                Some(btaNavBar(
                  Seq(
                    Some(ListLink(if(isWelsh) btaHomeLink.cy else btaHomeLink.en, btaHomeLink.url)),
                    Some(ListLink(if(isWelsh) btaAccountLink.cy else btaAccountLink.en, btaAccountLink.url)),
                    Some(ListLink(if(isWelsh) btaMessagesLink.cy else btaMessagesLink.en, btaMessagesLink.url, Some(notificationBadgeCount(btaMessagesLink.alerts.getOrElse(0))))),
                    service.formsNav(btaFormsLink),
                    Some(ListLink(if(isWelsh) btaHelpLink.cy else btaHelpLink.en, btaFormsLink.url))
                  ).flatten
                ))
            }
          }

          "connector returns None on successful response" should {

            "return None" in {
              mockGetBtaNavLinks()(Future.successful(None))
              await(service.retrieveBtaLinksAndRenderNavBar()) shouldBe None
            }
          }
        }

        "calling .formsNav()" when {

          "the navLink has an alert" should {

            "output the expected ListLink model" in {
              service.formsNav(btaFormsLink) shouldBe Some(
                ListLink(
                  message = if(isWelsh) btaFormsLink.cy else btaFormsLink.en,
                  url = btaFormsLink.url,
                  alert = Some(notificationBadgeCount(1))
                )
              )
            }
          }

          "the navLink does NOT have an alert" should {

            "output None" in {
              service.formsNav(btaFormsLink.copy(alerts = None)) shouldBe None
            }
          }
        }
      }
    }
  }
}
