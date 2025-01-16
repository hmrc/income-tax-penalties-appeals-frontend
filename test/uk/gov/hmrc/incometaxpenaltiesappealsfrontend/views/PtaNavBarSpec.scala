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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views

import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.navBar.PtaNavBar

class PtaNavBarSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures {

  lazy val ptaNavBar = app.injector.instanceOf[PtaNavBar]
  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  "ptaNavBar partial" should {

    Seq(Cy, En).foreach { lang =>

      s"render the Account Menu in ${lang.name} with a sign-out link" in {

        val html = ptaNavBar(0)(FakeRequest(), messagesApi.preferred(Seq(Lang(lang.code))))
        val document = Jsoup.parse(html.toString())

        val signOutLink = document.select("nav ul a").get(4)

        signOutLink.text() shouldBe (if(lang == En) "Sign out" else "Allgofnodi")
        signOutLink.attr("href") shouldBe controllers.routes.ServiceController.logout.url
      }
    }
  }
}
