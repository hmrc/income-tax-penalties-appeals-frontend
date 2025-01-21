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

object WhenDidEventHappenMessages {

  sealed trait Messages { _: i18n =>
    val crimeReasonInvalid = "The date of the crime must be a real date"
    val fireOrFloodReasonInvalid = "The date of the fire or flood must be a real date"
    val technicalReasonInvalid = "The date the software or technology issues began must be a real date"
    val bereavementReasonInvalid = "The date the person died must be a real date"

    val crimeReasonRequiredAll = "Enter the date of the crime"
    val fireOrFloodReasonRequiredAll = "Enter the date of the fire or flood"
    val technicalReasonRequiredAll = "Enter the date when the software or technology issues began"
    val bereavementReasonRequiredAll = "Enter the date when the person died"

    val crimeReasonRequiredTwo = "The date of the crime must include a {0} and a {1}"
    val fireOrFloodReasonRequiredTwo = "The date of the fire or flood must include a {0} and a {1}"
    val technicalReasonRequiredTwo = "The date the software or technology issues began must include a {0} and a {1}"
    val bereavementReasonRequiredTwo = "The date the person died must include a {0} and {1}"

    val crimeReasonRequired = "The date of the crime must include a {0}"
    val fireOrFloodReasonRequired = "The date of the fire or flood must include a {0}"
    val technicalReasonRequired = "The date the software or technology issues began must include a {0}"
    val bereavementReasonRequired = "The date the person died must include a {0}"

    val crimeReasonNotInFuture = "The date of the crime must be today or in the past"
    val fireOrFloodReasonNotInFuture = "The date of the fire or flood must be today or in the past"
    val technicalReasonNotInFuture = "The date the software or technology issues began must be today or in the past"
    val bereavementReasonNotInFuture = "The date the person died must be today or in the past"

  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

  }
}
