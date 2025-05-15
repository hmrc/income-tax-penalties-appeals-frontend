package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

class TimeMachineSpec extends AnyWordSpec with Matchers{

  private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")


  "TimeMachine -getCurrentDate" should {

    "Return the configured date specified" in {
      val app = new GuiceApplicationBuilder().configure(
        "timemachine.enabled" -> true,
        "timemachine.date" -> "01-01-2021"
      ).build()

      val timeMachine = app.injector.instanceOf[TimeMachine]
      timeMachine.getCurrentDate shouldEqual LocalDate.parse("01-01-2021", dateFormatter)
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
