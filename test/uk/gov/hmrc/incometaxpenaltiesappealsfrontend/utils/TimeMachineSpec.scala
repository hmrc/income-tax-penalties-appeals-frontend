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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

class TimeMachineSpec extends AnyWordSpec with Matchers{

  private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")


  "TimeMachine -getCurrentDate" should {

    "Return the configured date specified" in {
      val app = new GuiceApplicationBuilder().configure(
        "timemachine.enabled" -> true,
        "timemachine.date" -> "01-01-21"
      ).build()

      val timeMachine = app.injector.instanceOf[TimeMachine]
      timeMachine.getCurrentDate shouldEqual LocalDate.parse("01-01-21", dateFormatter)
    }
  }

  "Return the current date if timemachine is disabled" in {
    val app = new GuiceApplicationBuilder().configure(
      "timemachine.enabled" -> false
    ).build()

    val timeMachine = app.injector.instanceOf[TimeMachine]
    timeMachine.getCurrentDate shouldEqual LocalDate.now()
  }

  "Return the current date if timemachine date set to now" in {

    val app = new GuiceApplicationBuilder().configure(
      "timemachine.enabled" -> true,
      "timemachine.date" -> "now"
    ).build()

    val timeMachine = app.injector.instanceOf[TimeMachine]
    timeMachine.getCurrentDate shouldEqual LocalDate.now()

  }

  "TimeMachine -getCurrentDateTime" should {
    "always return the real current date‐time" in {
      val app = new GuiceApplicationBuilder().build()
      val tm = app.injector.instanceOf[TimeMachine]

      val now = LocalDateTime.now()
      val got = tm.getCurrentDateTime
      java.time.Duration.between(got, now).abs().toSeconds should be < 2L
    }
  }
}
