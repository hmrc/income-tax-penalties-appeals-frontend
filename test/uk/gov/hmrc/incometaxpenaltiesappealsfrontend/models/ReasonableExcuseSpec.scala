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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import fixtures.messages.ReasonableExcuseMessages
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, RadioItem, Text}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.{FeatureSwitching, ReasonableExcusesEnabled}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._

class ReasonableExcuseSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with FeatureSwitching with BeforeAndAfterAll {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit override lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def afterAll(): Unit = {
    sys.props.remove(ReasonableExcusesEnabled.configName)
    super.afterAll()
  }

  "should have the correct names" in {
    Bereavement.toString        shouldBe "bereavement"
    Cessation.toString          shouldBe "cessation"
    Crime.toString              shouldBe "crime"
    FireOrFlood.toString        shouldBe "fireandflood"
    Health.toString             shouldBe "health"
    TechnicalIssues.toString    shouldBe "technicalIssue"
    UnexpectedHospital.toString shouldBe "unexpectedHospital"
    LossOfStaff.toString        shouldBe "lossOfEssentialStaff"
    Other.toString              shouldBe "other"
  }

  "serialise to JSON" in {
    Json.toJson[ReasonableExcuse](Bereavement)        shouldBe JsString(Bereavement.toString)
    Json.toJson[ReasonableExcuse](Cessation)          shouldBe JsString("cessation")
    Json.toJson[ReasonableExcuse](Crime)              shouldBe JsString("crime")
    Json.toJson[ReasonableExcuse](FireOrFlood)        shouldBe JsString("fireandflood")
    Json.toJson[ReasonableExcuse](Health)             shouldBe JsString("health")
    Json.toJson[ReasonableExcuse](TechnicalIssues)    shouldBe JsString("technicalIssue")
    Json.toJson[ReasonableExcuse](UnexpectedHospital) shouldBe JsString("unexpectedHospital")
    Json.toJson[ReasonableExcuse](LossOfStaff)        shouldBe JsString("lossOfEssentialStaff")
    Json.toJson[ReasonableExcuse](Other)              shouldBe JsString("other")
  }

  "deserialise from JSON" in {
    JsString(Bereavement.toString).as[ReasonableExcuse]          shouldBe Bereavement
    JsString("cessation").as[ReasonableExcuse]            shouldBe Cessation
    JsString("crime").as[ReasonableExcuse]                shouldBe Crime
    JsString("fireandflood").as[ReasonableExcuse]         shouldBe FireOrFlood
    JsString("health").as[ReasonableExcuse]               shouldBe Health
    JsString("technicalIssue").as[ReasonableExcuse]       shouldBe TechnicalIssues
    JsString("unexpectedHospital").as[ReasonableExcuse]   shouldBe UnexpectedHospital
    JsString("lossOfEssentialStaff").as[ReasonableExcuse] shouldBe LossOfStaff
    JsString("other").as[ReasonableExcuse]                shouldBe Other
  }

  ".radioOptions()" when {

    Seq(ReasonableExcuseMessages.English, ReasonableExcuseMessages.Welsh).foreach { messagesForLang =>

      implicit val messages: Messages = messagesApi.preferred(Seq(Lang(messagesForLang.lang.code)))

      s"when rendering in language of '${messagesForLang.lang.name}'" should {

        "all reasons are enabled" should {

          "return the expected RadioItem models" in {
            setEnabledSwitches(ReasonableExcusesEnabled, ReasonableExcuse.allReasonableExcuses.map(_.toString))

            ReasonableExcuse.radioOptions() shouldBe Seq(
              RadioItem(
                Text(messagesForLang.bereavement),
                value = Some(Bereavement.toString),
                id = Some(Bereavement.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.cessation),
                value = Some(Cessation.toString),
                id = Some(Cessation.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.crime),
                value = Some(Crime.toString),
                id = Some(Crime.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.fireOrFlood),
                value = Some(FireOrFlood.toString),
                id = Some(FireOrFlood.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.health),
                value = Some(Health.toString),
                id = Some(Health.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.technical),
                value = Some(TechnicalIssues.toString),
                id = Some(TechnicalIssues.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.unexpectedHospital),
                value = Some(UnexpectedHospital.toString),
                id = Some(UnexpectedHospital.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.lossOfStaff),
                value = Some(LossOfStaff.toString),
                id = Some(LossOfStaff.toString)
              ),
              RadioItem(
                divider = Some(messagesForLang.or)
              ),
              RadioItem(
                content = Text(messagesForLang.other),
                value = Some(Other.toString),
                hint = Some(Hint(
                  content = Text(messages(messagesForLang.otherHint))
                )),
                id = Some(Other.toString)
              )
            )
          }
        }

        "when SOME of reasons are enabled (including Other)" should {

          "return the expected RadioItem models" in {

            setEnabledSwitches(
              ReasonableExcusesEnabled,
              Seq(
                Bereavement.toString,
                Cessation.toString,
                Crime.toString,
                Other.toString
              )
            )

            ReasonableExcuse.radioOptions() shouldBe Seq(
              RadioItem(
                Text(messagesForLang.bereavement),
                value = Some(Bereavement.toString),
                id = Some(Bereavement.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.cessation),
                value = Some(Cessation.toString),
                id = Some(Cessation.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.crime),
                value = Some(Crime.toString),
                id = Some(Crime.toString)
              ),
              RadioItem(
                divider = Some(messagesForLang.or)
              ),
              RadioItem(
                content = Text(messagesForLang.other),
                value = Some(Other.toString),
                hint = Some(Hint(
                  content = Text(messages(messagesForLang.otherHint))
                )),
                id = Some(Other.toString),
              )
            )
          }
        }

        "when SOME of reasons are enabled (excluding Other)" should {

          "return the expected RadioItem models" in {

            setEnabledSwitches(
              ReasonableExcusesEnabled,
              Seq(
                Bereavement.toString,
                Crime.toString
              )
            )

            ReasonableExcuse.radioOptions() shouldBe Seq(
              RadioItem(
                Text(messagesForLang.bereavement),
                value = Some(Bereavement.toString),
                id = Some(Bereavement.toString)
              ),
              RadioItem(
                content = Text(messagesForLang.crime),
                value = Some(Crime.toString),
                id = Some(Crime.toString)
              )
            )
          }
        }
      }
    }
  }
}