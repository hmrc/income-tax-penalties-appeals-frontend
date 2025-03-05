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

package fixtures.messages

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.CurrencyFormatter

object SingleAppealConfirmationMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "The appeal will cover a single penalty"
    def p1_LPP1(amount: BigDecimal): String = s"You have chosen to appeal the £${CurrencyFormatter.uiFormat(amount)} first late payment penalty."
    def p1_LPP2(amount: BigDecimal): String = s"You have chosen to appeal the £${CurrencyFormatter.uiFormat(amount)} second late payment penalty."
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "The appeal will cover a single penalty (Welsh)"
    override def p1_LPP1(amount: BigDecimal): String = s"You have chosen to appeal the £${CurrencyFormatter.uiFormat(amount)} first late payment penalty. (Welsh)"
    override def p1_LPP2(amount: BigDecimal): String = s"You have chosen to appeal the £${CurrencyFormatter.uiFormat(amount)} second late payment penalty. (Welsh)"
  }
}
