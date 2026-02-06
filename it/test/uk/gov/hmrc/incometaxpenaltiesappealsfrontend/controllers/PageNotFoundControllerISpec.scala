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
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.En
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.repositories.UserAnswersRepository


class PageNotFoundControllerISpec extends ControllerISpecHelper {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang(En.code)))
  override lazy val userAnswersRepo: UserAnswersRepository = app.injector.instanceOf[UserAnswersRepository]

  Map("/page-not-found" -> false, "/agent-page-not-found" -> true).foreach { case (path, isAgent) =>

    s"GET $path" should {

      "render the page with the correct elements" in {
        stubAuthRequests(isAgent)
        userAnswersRepo.upsertUserAnswer(emptyUserAnswers).futureValue

        val result = get(path, isAgent = isAgent)

        val document = Jsoup.parse(result.body)

        document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Page not found - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Page not found"
        document.getParagraphs.get(0).text() shouldBe "If you typed the web address, check it is correct."
        document.getParagraphs.get(1).text() shouldBe "If you pasted the web address, check you copied the entire address."
        document.getLink("returnToSALink").text() shouldBe "Back to Self Assessment penalties and appeals"

      }
    }
  }

}
