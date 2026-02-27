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

package fixtures.messages.upscan

import fixtures.messages.{Cy, En, i18n}
import fixtures.messages.HonestyDeclarationMessages.fakeRequestForBereavementJourney.isAgent


object NonJsFileUploadMessages {

  sealed trait Messages { this: i18n =>
    val headingAndTitle = "Upload evidence to support this appeal"
    val headingAndTitleReview = "Upload evidence to support this review"
    val p1 = "Use this page to upload any evidence to help us review this penalty."
    val p1Joint = "Use this page to upload any evidence to help us review these penalties."
    val p1Review = "Use this page to upload any evidence to help us review the appeal decision."
    val p1JointReview = "Use this page to upload any evidence to help us review the appeal decisions."
    val p2LSP = "Upload evidence that explains why you missed the submission deadline, such as a letter or email."
    val p2LSPReview = "Upload evidence that explains why you missed the submission deadline, such as a letter or email."
    val p2LSPAgent = "Upload evidence that explains why you missed the submission deadline, such as a letter or email."
    val heading3 = "Files you can upload"
    val p2LPP = "Upload evidence that explains why you missed the payment deadline, such as a letter or email."
    val p2LPPReview = "Upload evidence that explains why you missed the payment deadline, such as a letter or email."
    val p2LPPAgent = "Upload evidence that explains why your client missed the payment deadline, such as a letter or email"
    val p3: Int => String = n => s"You can upload up to $n files."
    val p4: Int => String = n => s"Each file must be smaller than ${n}MB."
    val label = "Select a file"
    val newLabel = "Upload a file"
    val p2LSPOrp2LSPAgent = if (isAgent) p2LSPAgent else p2LSP
    val p2LPPOrp2LPPAgent = if(isAgent) p2LPPAgent else p2LPP

  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Uwchlwythwch dystiolaeth i gefnogi’r apêl hon"
    override val headingAndTitleReview = "Uwchlwythwch dystiolaeth i gefnogi’r adolygiad hwn"
    override val p1 = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu ni i adolygu’r gosb."
    override val p1Joint = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu ni i adolygu’r cosbau hyn."
    override val p1Review = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu i adolygu’r penderfyniad ynghylch apêl."
    override val p1JointReview = "Defnyddiwch y dudalen hon i uwchlwytho unrhyw dystiolaeth i’n helpu i adolygu’r penderfyniad ynghylch apêl."
    override val p2LSP = "Uwchlwythwch dystiolaeth, fel llythyr neu e-bost, sy’n egluro pam gwnaethoch fethu’r dyddiad cau ar gyfer cyflwyno."
    override val p2LSPReview = "Uwchlwythwch dystiolaeth, fel llythyr neu e-bost, sy’n egluro pam gwnaethoch fethu’r dyddiad cau ar gyfer cyflwyno."
    override val p2LSPAgent = "Uwchlwythwch dystiolaeth, fel llythyr neu e-bost, sy’n egluro pam gwnaethoch fethu’r dyddiad cau ar gyfer cyflwyno."
    override val heading3 = "Ffeiliau y mae modd i chi eu huwchlwytho"
    override val p2LPP = "Uwchlwythwch dystiolaeth sy’n egluro pam y gwnaethoch fethu’r dyddiad cau ar gyfer talu, fel llythyr neu e-bost."
    override val p2LPPReview = "Uwchlwythwch dystiolaeth sy’n egluro pam y gwnaethoch fethu’r dyddiad cau ar gyfer talu, fel llythyr neu e-bost."
    override val p2LPPAgent = "Uwchlwythwch dystiolaeth, fel llythyr neu e-bost, sy’n egluro pam gwnaeth eich cleient fethu’r dyddiad cau ar gyfer talu."
    override val p3: Int => String = n => s"Gallwch uwchlwytho hyd at $n ffeil."
    override val p4: Int => String = n => s"Mae’n rhaid i bob ffeil fod yn llai na ${n}MB."
    override val label = "Dewiswch ffeil"
    override val newLabel: String = "Uwchlwytho ffeil"
    override val  p2LSPOrp2LSPAgent = if (isAgent) p2LSPAgent else p2LSP
    override val p2LPPOrp2LPPAgent = if(isAgent) p2LPPAgent else p2LPP
  }
}
