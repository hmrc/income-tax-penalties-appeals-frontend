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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms

import fixtures.messages.i18n
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

trait FormBehaviours extends AnyWordSpec with Matchers {

  val invalidChars: String = "コし"

  def mandatoryField(form: Form[_],
                     fieldName: String,
                     requiredError: FormError): Unit = {

    s"not bind when key is not present at all for field $fieldName" in {

      val result = form.bind(Map.empty[String, String]).apply(fieldName)
      result.errors.headOption shouldBe Some(requiredError)
    }

    s"not bind blank values for field $fieldName" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors.headOption shouldBe Some(requiredError)
    }
  }



  //scalastyle:off
  def dateForm(form: Form[_],
               fieldName: String,
               errorMessageKey: String => String,
               errorMessageValue: (String, Seq[String]) => String,
               messagesForLanguage: i18n)(implicit messages: Messages): Unit = {

    "bind when the date is valid" in {

      val result = form.bind(
        Map(
          s"$fieldName.day" -> "1",
          s"$fieldName.month" -> "2",
          s"$fieldName.year" -> "2021"
        )
      )
      result.errors shouldBe List.empty
    }

    "not bind" when {
      "the date is in the future" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "1",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2050"
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(
          key = s"$fieldName.day",
          message = errorMessageKey("notInFuture"),
          args = Seq(messagesForLanguage.day, messagesForLanguage.month, messagesForLanguage.year)
        )
        messages(error.message, error.args:_*) shouldBe errorMessageValue("notInFuture", Seq(messagesForLanguage.day, messagesForLanguage.month, messagesForLanguage.year))
      }

      "the date is not valid" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "31",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.day", errorMessageKey("invalid"), Seq())
        messages(error.message, error.args:_*) shouldBe errorMessageValue("invalid", Seq())
      }

      "the date contains strings instead of numbers" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "thirtyFirst",
            s"$fieldName.month" -> "ofTheSecond",
            s"$fieldName.year" -> "twentyTwentyOne"
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.day", errorMessageKey("invalid"), Seq(messagesForLanguage.day, messagesForLanguage.month, messagesForLanguage.year))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("invalid", Seq(messagesForLanguage.day, messagesForLanguage.month, messagesForLanguage.year))
      }

      "the date has no day" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.day", errorMessageKey("required"), Seq(messagesForLanguage.day))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required", Seq(messagesForLanguage.day))
      }

      "the date has no month" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "1",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.month", errorMessageKey("required"), Seq(messagesForLanguage.month))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required", Seq(messagesForLanguage.month))
      }

      "the date has no year" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "1",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.year", errorMessageKey("required"), Seq(messagesForLanguage.year))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required", Seq(messagesForLanguage.year))
      }

      "the date has a day but no month and year" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "2",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.month", errorMessageKey("required.two"), Seq(messagesForLanguage.month, messagesForLanguage.year))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required.two", Seq(messagesForLanguage.month, messagesForLanguage.year))
      }

      "the date has a month but no day and year" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.day", errorMessageKey("required.two"), Seq(messagesForLanguage.day, messagesForLanguage.year))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required.two", Seq(messagesForLanguage.day, messagesForLanguage.year))
      }

      "the date has a year but no day and month" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.day", errorMessageKey("required.two"), Seq(messagesForLanguage.day, messagesForLanguage.month))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required.two", Seq(messagesForLanguage.day, messagesForLanguage.month))
      }

      "the date has no values" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        val error = result.errors.head
        error shouldBe FormError(s"$fieldName.day", errorMessageKey("required.all"), Seq(messagesForLanguage.day, messagesForLanguage.month, messagesForLanguage.year))
        messages(error.message, error.args:_*) shouldBe errorMessageValue("required.all", Seq(messagesForLanguage.day, messagesForLanguage.month, messagesForLanguage.year))
      }
    }
  }
}
