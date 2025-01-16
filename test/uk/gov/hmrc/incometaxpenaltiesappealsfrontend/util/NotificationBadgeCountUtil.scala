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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.util

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.NotificationBadgeCountUtil

class NotificationBadgeCountUtil extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures {

  "NotificationBadgeCountUtil" should {

    "truncate any values greater than 99 to +99" in {
      NotificationBadgeCountUtil.notificationBadgeCount(100) shouldBe "+99"
      NotificationBadgeCountUtil.notificationBadgeCount(101) shouldBe "+99"
      NotificationBadgeCountUtil.notificationBadgeCount(5440) shouldBe "+99"
    }

    "any value less than 100 unchanged" in {
      NotificationBadgeCountUtil.notificationBadgeCount(99) shouldBe "99"
      NotificationBadgeCountUtil.notificationBadgeCount(98) shouldBe "98"
      NotificationBadgeCountUtil.notificationBadgeCount(1) shouldBe "1"
    }
  }
}
