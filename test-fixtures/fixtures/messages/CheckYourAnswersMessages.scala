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

object CheckYourAnswersMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Check your answers"
    val declarationH2 = "Declaration"
    val declarationP1 = "By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge."
    val declarationP1Review = "By submitting this request for a review, you are making a legal declaration that the information is correct and complete to the best of your knowledge."
    val declarationP2 = "A false declaration can result in prosecution."
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Check your answers"
    override val declarationH2 = "Declaration (Welsh)"
    override val declarationP1 = "By submitting this appeal, you are making a legal declaration that the information is correct and complete to the best of your knowledge. (Welsh)"
    override val declarationP1Review = "By submitting this request for a review, you are making a legal declaration that the information is correct and complete to the best of your knowledge. (Welsh)"
    override val declarationP2 = "A false declaration can result in prosecution. (Welsh)"
  }
}
