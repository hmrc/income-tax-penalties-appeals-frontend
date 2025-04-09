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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config

import org.mockito.Mockito.when
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.models.FeatureSwitch

class FeatureSwitchRegistrySpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockitoSugar with Injecting {


  val testFeatureSwitch: FeatureSwitch = mock[FeatureSwitch]
  object TestRegistry extends FeatureSwitchRegistry {
    override val switches: Seq[FeatureSwitch] = Seq(testFeatureSwitch)
  }

  "FeatureSwitchRegistry" should {

    when(testFeatureSwitch.configName) thenReturn "feature1"

    "return a switch from get if exists" in {
      val result = TestRegistry.get("feature1")
      result shouldBe Some(testFeatureSwitch)
    }

    "return None from get if switch doesnt exists" in {
      val result = TestRegistry.get("unknown.feature")
      result shouldBe None
    }

    "return a switch from apply if exists " in {
      val result = TestRegistry("feature1")
      result shouldBe testFeatureSwitch
    }

    "throw an exception if switch does not exist when using apply" in {
      val ex = intercept[IllegalArgumentException] {
        TestRegistry("invalid.feature")
      }
      ex.getMessage should include ("Invalid feature switch")
    }

  }
}
