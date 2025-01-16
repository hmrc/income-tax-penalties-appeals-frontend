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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.btaNavBar.ListLink
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.navBar.BtaNavBar

class BtaNavBarSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures {

  lazy val btaNavBar = app.injector.instanceOf[BtaNavBar]

  "btaNavBar partial" should {

    "render the expected HTML for a supplied list of links" in {

      val linksToRender = Seq(
        ListLink(message = "foo link", url = "/foo", alert = Some("0")),
        ListLink(message = "bar link", url = "/bar", alert = None),
        ListLink(message = "wizz link", url = "/wizz", alert = Some("2"))
      )

      val html = btaNavBar(linksToRender)
      val document = Jsoup.parse(html.toString())
      val allLinks = document.select("nav > ul > li > a")

      linksToRender.zipWithIndex.foreach { case (link, index) =>
        val linkElement = allLinks.get(index)
        linkElement.text() shouldBe link.message + link.alert.fold("")(n => if(n != "0") s" $n" else "")
        linkElement.attr("href") shouldBe link.url
        linkElement.id() shouldBe s"nav-bar-link-${link.message.replace(" ", "-")}"
      }
    }
  }

}
