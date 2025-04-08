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

import fixtures.BaseFixtures


object HonestyDeclarationMessages extends BaseFixtures {

  sealed trait Messages { _: i18n =>
    val headingAndTitle: String = "Honesty declaration"
    val confirmParagraph:String = "I confirm that:"
    val bereavementMessageLSP: String = "because I was affected by someone’s death, I was unable to send the submission due on 1 January 2022"
    val cessationMessageLSP: String = "TBC cessation - I was unable to send the submission due on 1 January 2022"
    val crimeMessageLSP: String = "because I was affected by a crime, I was unable to send the submission due on 1 January 2022"
    val fireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was unable to send the submission due on 1 January 2022"
    val healthMessageLSP: String = "because of a serious or life-threatening health issue, I was unable to send the submission due on 1 January 2022"
    val technicalIssueMessageLSP: String = "because of software or technology issues, I was unable to send the submission due on 1 January 2022"
    val unexpectedHospitalMessageLSP: String = "because of a stay in hospital, I was unable to send the submission due on 1 January 2022"
    val otherMessageLSP: String = "I was unable to send the submission due on 1 January 2022"

    val bereavementMessageLPP: String = "because I was affected by someone’s death, I was unable to make the payment due on 1 January 2022"
    val cessationMessageLPP: String = "TBC cessation - I was unable to make the payment due on 1 January 2022"
    val crimeMessageLPP: String = "because I was affected by a crime, I was unable to make the payment due on 1 January 2022"
    val fireOrFloodReasonMessageLPP: String = "because of a fire or flood, I was unable to make the payment due on 1 January 2022"
    val healthMessageLPP: String = "because of a serious or life-threatening health issue, I was unable to make the payment due on 1 January 2022"
    val technicalIssueMessageLPP: String = "because of software or technology issues, I was unable to make the payment due on 1 January 2022"
    val unexpectedHospitalMessageLPP: String = "because of a stay in hospital, I was unable to make the payment due on 1 January 2022"
    val otherMessageLPP: String = "I was unable to make the payment due on 1 January 2022"

    val honestyDeclarationLPP = "no one else was available to make the payment for me"
    val honestyDeclarationHospital = "the timing of the hospital stay was unexpected"
    val honestyDeclarationHealth = "the timing of the health issue was unexpected"
    val honestyDeclarationInfo= "I will provide honest and accurate information in this appeal"
    val honestyDeclarationInfoReview= "I will provide honest and accurate information in this request for a review"
    val acceptAndContinue = "Accept and continue"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle: String = "Honesty declaration (Welsh)"
    override val confirmParagraph:String = "I confirm that: (Welsh)"
    override val bereavementMessageLSP: String = "because I was affected by someone’s death, I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val cessationMessageLSP: String = "TBC cessation - I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val crimeMessageLSP: String = "because I was affected by a crime, I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val fireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val healthMessageLSP: String = "because of a serious or life-threatening health issue, I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val technicalIssueMessageLSP: String = "because of software or technology issues, I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val unexpectedHospitalMessageLSP: String = "because of a stay in hospital, I was unable to send the submission due on 1 January 2022 (Welsh)"
    override val otherMessageLSP: String = "I was unable to send the submission due on 1 January 2022 (Welsh)"

    override val bereavementMessageLPP: String = "because I was affected by someone’s death, I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val cessationMessageLPP: String = "TBC cessation - I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val crimeMessageLPP: String = "because I was affected by a crime, I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val fireOrFloodReasonMessageLPP: String = "because of a fire or flood, I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val healthMessageLPP: String = "because of a serious or life-threatening health issue, I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val technicalIssueMessageLPP: String = "because of software or technology issues, I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val unexpectedHospitalMessageLPP: String = "because of a stay in hospital, I was unable to make the payment due on 1 January 2022 (Welsh)"
    override val otherMessageLPP: String = "I was unable to make the payment due on 1 January 2022 (Welsh)"

    override val honestyDeclarationLPP = "no one else was available to make the payment for me (Welsh)"
    override val honestyDeclarationHospital = "the timing of the hospital stay was unexpected (Welsh)"
    override val honestyDeclarationHealth = "the timing of the health issue was unexpected (Welsh)"
    override val honestyDeclarationInfo = "I will provide honest and accurate information in this appeal (Welsh)"
    override val honestyDeclarationInfoReview= "I will provide honest and accurate information in this request for a review (Welsh)"
    override val acceptAndContinue = "Accept and continue (Welsh)"

  }
}
