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

package utils

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS}

trait TimerUtil { _: Matchers =>

  def calculateRuntime(f: => Unit): FiniteDuration = {
    val start = System.currentTimeMillis()
    f
    Duration(System.currentTimeMillis() - start, MILLISECONDS)
  }

  implicit class TimerUtilOps(a: FiniteDuration) {
    def shouldTakeAtLeast(b: FiniteDuration): Assertion =
      if(a >= b) succeed else fail(s"$a was not greater than or equal to $b")
  }
}
