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

import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

class TimeMachineSpec extends AnyWordSpec with Matchers with MockFactory {


  "TimeMachine -getCurrentDate" should {

    "Return the configured date specified" in {
      val app = new GuiceApplicationBuilder().configure(
        "timemachine.enabled" -> true,
        "timemachine.date" -> "01-05-2025"
      ).build()

      val timeMachine = app.injector.instanceOf[TimeMachine]
      sys.props -= "TIME_MACHINE_NOW"
      sys.props += ("TIME_MACHINE_NOW" -> "02-05-2024")
      timeMachine.getCurrentDate shouldEqual LocalDate.of(2024, 5, 2)
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
      sys.props -= "TIME_MACHINE_NOW"
      timeMachine.getCurrentDate shouldEqual LocalDate.now()

    }

    "optCurrentDate" should {
      "return None when timeMachineEnabled is false" in {
        val config = Configuration("timemachine.enabled" -> false)
        val servicesConfig = mock[ServicesConfig]
        (servicesConfig.getString(_: String)).expects(*).returning("#").atLeastOnce()
        val appConfig = new AppConfig(config, servicesConfig)
        appConfig.optCurrentDate mustBe None
      }

      "return Some(LocalDate) from system property when timeMachineEnabled is true and property is set" in {
        val config = Configuration(
          "timemachine.enabled" -> true,
          "timemachine.date" -> "01-01-2024"
        )
        val servicesConfig = mock[ServicesConfig]
        (servicesConfig.getString(_: String)).expects(*).returning("#").atLeastOnce()
        val appConfig = new AppConfig(config, servicesConfig)
        val dateStr = "31-12-2023"
        System.setProperty("TIME_MACHINE_NOW", dateStr)
        try {
          appConfig.optCurrentDate mustBe Some(LocalDate.parse(dateStr, appConfig.timeMachineDateFormatter))
        } finally {
          System.clearProperty("TIME_MACHINE_NOW")
        }
      }

      "return Some(LocalDate) from config when timeMachineEnabled is true and property is not set" in {
        val config = Configuration(
          "timemachine.enabled" -> true,
          "timemachine.date" -> "01-01-2024"
        )
        val servicesConfig = mock[ServicesConfig]
        (servicesConfig.getString(_: String)).expects(*).returning("#").atLeastOnce()
        val appConfig = new AppConfig(config, servicesConfig)
        appConfig.optCurrentDate mustBe Some(LocalDate.parse("01-01-2024", appConfig.timeMachineDateFormatter))
      }

      "return None if date string is invalid" in {
        val config = Configuration(
          "timemachine.enabled" -> true,
          "timemachine.date" -> "invalid-date"
        )
        val servicesConfig = mock[ServicesConfig]
        (servicesConfig.getString(_: String)).expects(*).returning("#").atLeastOnce()
        val appConfig = new AppConfig(config, servicesConfig)
        appConfig.optCurrentDate mustBe None
      }
    }

  }
}
