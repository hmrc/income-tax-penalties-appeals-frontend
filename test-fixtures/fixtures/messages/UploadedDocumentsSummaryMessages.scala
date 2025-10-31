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

object UploadedDocumentsSummaryMessages {

  sealed trait Messages { this: i18n =>
    val cyaKey = "Evidence to support this appeal"
    val cyaHidden = "evidence to support this appeal"
    val cyaKeyReview = "Evidence to support this review"
    val cyaHiddenReview = "evidence to support this review"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val cyaKey = "Tystiolaeth i ategu’r apêl hon"
    override val cyaHidden = "Tystiolaeth i ategu’r apêl hon"
    override val cyaKeyReview = "Tystiolaeth i gefnogi’r adolygiad hwn"
    override val cyaHiddenReview = "tystiolaeth i gefnogi’r adolygiad hwn"
  }
}
