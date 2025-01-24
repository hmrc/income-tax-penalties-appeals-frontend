/*
 * Copyright 2023 HM Revenue & Customs
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
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html

trait ViewBehaviours extends AnyWordSpec with Matchers {

  trait BaseSelectors {
    val title: String = "title"
    val h1: String = "h1"
    val h2: Int => String = i => s"h2:nth-of-type($i)"
    val p: Int => String = i => s"p:nth-of-type($i)"
    val bullet: Int => String = i => s"ul li:nth-of-type($i)"
    val details: String = "details"
    val detailsSummary: String = s"$details summary"
    val label: String => String = input => s"label[for=$input]"
    val button: String = "button.govuk-button"
  }

  def concat(selectors: String*): String = selectors.mkString(" ")

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def pageWithExpectedElementsAndMessages(checks: (String, String)*)(implicit document: Document): Unit = checks foreach {
    case (selector, message) =>
      s"element with selector '$selector'" should {
        s"include the message '$message'" in {
          val updatedSelect = if(selector == "title") selector else concat("main", selector)
          document.select(updatedSelect) match {
            case elements if elements.size() == 0 =>
              fail(s"Could not find element with CSS selector: '$updatedSelect'")
            case elements =>
              elements.first().text() should include(message)
          }
        }
      }
  }
}

