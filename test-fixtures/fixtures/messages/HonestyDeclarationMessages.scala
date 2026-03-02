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

  sealed trait Messages { this: i18n =>
    val headingAndTitle: String = "Honesty declaration"
    val confirmParagraph:String = "I confirm that:"
    
    val bereavementMessageLSP: String = "because I was affected by someone’s death, I was not able to send the submission due on 1 January 2022"
    val cessationMessageLSP: String = "TBC cessation - I was not able to send the submission due on 1 January 2022"
    val crimeMessageLSP: String = "because I was affected by a crime, I was not able to send the submission due on 1 January 2022"
    val fireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was not able to send the submission due on 1 January 2022"
    val healthMessageLSP: String = "because of a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022"
    val technicalIssueMessageLSP: String = "because of software or technology issues, I was not able to send the submission due on 1 January 2022"
    val unexpectedHospitalMessageLSP: String = "because of a stay in hospital, I was not able to send the submission due on 1 January 2022"
    val otherMessageLSP: String = "I was not able to send the submission due on 1 January 2022"

    val bereavementMessageLPP: String = "because I was affected by someone’s death, I was not able to make the payment due on 1 January 2022"
    val cessationMessageLPP: String = "TBC cessation - I was not able to make the payment due on 1 January 2022"
    val crimeMessageLPP: String = "because I was affected by a crime, I was not able to make the payment due on 1 January 2022"
    val fireOrFloodReasonMessageLPP: String = "because of a fire or flood, I was not able to make the payment due on 1 January 2022"
    val healthMessageLPP: String = "because of a serious or life-threatening health issue, I was not able to make the payment due on 1 January 2022"
    val technicalIssueMessageLPP: String = "because of software or technology issues, I was not able to make the payment due on 1 January 2022"
    val unexpectedHospitalMessageLPP: String = "because of a stay in hospital, I was not able to make the payment due on 1 January 2022"
    val otherMessageLPP: String = "I was not able to make the payment due on 1 January 2022"

    val agentBereavementMessageLPP: String = "because my client was affected by someone’s death, they were not able to make the payment due on 1 January 2022"
    val agentCessationMessageLPP: String = "TBC cessation - they were not able to make the payment due on 1 January 2022"
    val agentCrimeMessageLPP: String = "because my client was affected by a crime, they were not able to make the payment due on 1 January 2022"
    val agentFireOrFloodReasonMessageLPP: String = "because my client was affected by a fire or flood, they were not able to make the payment due on 1 January 2022"
    val agentHealthMessageLPP: String = "because my client had a serious or life-threatening health issue, they were not able to make the payment due on 1 January 2022"
    val agentTechnicalIssueMessageLPP: String = "because my client was affected by software or technology issues, they were not able to make the payment due on 1 January 2022"
    val agentUnexpectedHospitalMessageLPP: String = "because my client had an unexpected stay in hospital, they were not able to make the payment due on 1 January 2022"
    val agentOtherMessageLPP: String = "my client was not able to make the payment due on 1 January 2022"

    val clientPlannedBereavementMessageLSP: String = "because my client was affected by someone’s death, they were not able to send the submission due on 1 January 2022"
    val clientPlannedCessationMessageLSP: String = "TBC cessation - they were not able to send the submission due on 1 January 2022"
    val clientPlannedCrimeMessageLSP: String = "because my client was affected by a crime, they were not able to send the submission due on 1 January 2022"
    val clientPlannedFireOrFloodReasonMessageLSP: String = "because of a fire or flood, my client was not able to send the submission due on 1 January 2022"
    val clientPlannedHealthMessageLSP: String = "because my client had a serious or life-threatening health issue, they were not able to send the submission due on 1 January 2022"
    val clientPlannedTechnicalIssueMessageLSP: String = "because of software or technology issues, my client was not able to send the submission due on 1 January 2022"
    val clientPlannedUnexpectedHospitalMessageLSP: String = "because my client had an unexpected hospital stay, they were not able to send the submission due on 1 January 2022"
    val clientPlannedOtherMessageLSP: String = "my client was not able to send the submission due on 1 January 2022"
    
    val agentPlannedClientAffectedBereavementMessageLSP: String = "because my client was affected by someone’s death, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedCessationMessageLSP: String = "TBC cessation - I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedCrimeMessageLSP: String = "because my client was affected by a crime, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedFireOrFloodReasonMessageLSP: String = "because my client was affected by a fire or flood, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedHealthMessageLSP: String = "because my client had a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedTechnicalIssueMessageLSP: String = "because my client was affected by software or technology issues, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedUnexpectedHospitalMessageLSP: String = "because my client had an unexpected hospital stay, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedOtherMessageLSP: String = "because of an issue affecting my client, I was not able to send the submission due on 1 January 2022"
    
    val agentPlannedAgentAffectedBereavementMessageLSP: String = "because I was affected by someone’s death, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedCessationMessageLSP: String = "TBC cessation - I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedCrimeMessageLSP: String = "because I was affected by a crime, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedFireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedHealthMessageLSP: String = "because of a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedTechnicalIssueMessageLSP: String = "because of software or technology issues, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedUnexpectedHospitalMessageLSP: String = "because of a stay in hospital, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedOtherMessageLSP: String = "I was not able to send the submission due on 1 January 2022"
    
    val honestyDeclarationLPP = "no one else was available to make the payment for me"
    val honestyDeclarationHospital = "the timing of the hospital stay was unexpected"
    val honestyDeclarationHealth = "the timing of the health issue was unexpected"
    val honestyDeclarationInfo= "I will provide honest and accurate information in this appeal"
    val honestyDeclarationInfoReview= "I confirm that I will provide honest and accurate information in this request for a review."
    val acceptAndContinue = "Accept and continue"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val headingAndTitle: String = "Honesty declaration (Welsh)"
    override val confirmParagraph:String = "I confirm that: (Welsh)"
    
    override val bereavementMessageLSP: String = "because I was affected by someone’s death, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val cessationMessageLSP: String = "TBC cessation - I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val crimeMessageLSP: String = "because I was affected by a crime, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val fireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val healthMessageLSP: String = "because of a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val technicalIssueMessageLSP: String = "because of software or technology issues, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val unexpectedHospitalMessageLSP: String = "because of a stay in hospital, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val otherMessageLSP: String = "I was not able to send the submission due on 1 January 2022 (Welsh)"

    override val bereavementMessageLPP: String = "because I was affected by someone’s death, I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val cessationMessageLPP: String = "TBC cessation - I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val crimeMessageLPP: String = "because I was affected by a crime, I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val fireOrFloodReasonMessageLPP: String = "because of a fire or flood, I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val healthMessageLPP: String = "because of a serious or life-threatening health issue, I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val technicalIssueMessageLPP: String = "because of software or technology issues, I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val unexpectedHospitalMessageLPP: String = "because of a stay in hospital, I was not able to make the payment due on 1 January 2022 (Welsh)"
    override val otherMessageLPP: String = "I was not able to make the payment due on 1 January 2022 (Welsh)"

    override val agentBereavementMessageLPP: String = "because my client was affected by someone’s death, they were not able to make the payment due on 1 January 2022 (Welsh)"
    override val agentCessationMessageLPP: String = "TBC cessation - they were not able to make the payment due on 1 January 2022 (Welsh)"
    override val agentCrimeMessageLPP: String = "because my client was affected by a crime, they were not able to make the payment due on 1 January 2022 (Welsh)"
    override val agentFireOrFloodReasonMessageLPP: String = "because my client was affected by a fire or flood, they were not able to make the payment due on 1 January 2022 (Welsh)"
    override val agentHealthMessageLPP: String = "because my client had a serious or life-threatening health issue, they were not able to make the payment due on 1 January 2022 (Welsh)"
    override val agentTechnicalIssueMessageLPP: String = "because my client was affected by software or technology issues, they were not able to make the payment due on 1 January 2022 (Welsh)"
    override val agentUnexpectedHospitalMessageLPP: String = "oherwydd bod fy nghleient wedi aros yn yr ysbyty yn annisgwyl, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 January 2022"
    override val agentOtherMessageLPP: String = "my client was not able to make the payment due on 1 January 2022 (Welsh)"

    override val clientPlannedBereavementMessageLSP: String = "because my client was affected by someone’s death, they were not able to send the submission due on 1 January 2022 (Welsh)"
    override val clientPlannedCessationMessageLSP: String = "TBC cessation - they were not able to send the submission due on 1 January 2022 (Welsh)"
    override val clientPlannedCrimeMessageLSP: String = "because my client was affected by a crime, they were not able to send the submission due on 1 January 2022 (Welsh)"
    override val clientPlannedFireOrFloodReasonMessageLSP: String = "because of a fire or flood, my client was not able to send the submission due on 1 January 2022 (Welsh)"
    override val clientPlannedHealthMessageLSP: String = "because my client had a serious or life-threatening health issue, they were not able to send the submission due on 1 January 2022 (Welsh)"
    override val clientPlannedTechnicalIssueMessageLSP: String = "because of software or technology issues, my client was not able to send the submission due on 1 January 2022 (Welsh)"
    override val clientPlannedUnexpectedHospitalMessageLSP: String = "oherwydd bod fy nghleient wedi aros yn yr ysbyty yn annisgwyl, nid oedd modd iddo anfon y cyflwyniad erbyn y dyddiad cau, 1 January 2022"
    override val clientPlannedOtherMessageLSP: String = "my client was not able to send the submission due on 1 January 2022 (Welsh)"
    
    override val agentPlannedClientAffectedBereavementMessageLSP: String = "because my client was affected by someone’s death, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedClientAffectedCessationMessageLSP: String = "TBC cessation - I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedClientAffectedCrimeMessageLSP: String = "because my client was affected by a crime, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedClientAffectedFireOrFloodReasonMessageLSP: String = "because my client was affected by a fire or flood, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedClientAffectedHealthMessageLSP: String = "because my client had a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedClientAffectedTechnicalIssueMessageLSP: String = "because my client was affected by software or technology issues, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedClientAffectedUnexpectedHospitalMessageLSP: String = "oherwydd bod fy nghleient wedi aros yn yr ysbyty yn annisgwyl, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 January 2022"
    override val agentPlannedClientAffectedOtherMessageLSP: String = "because of an issue affecting my client, I was not able to send the submission due on 1 January 2022 (Welsh)"

    override val agentPlannedAgentAffectedBereavementMessageLSP: String = "because I was affected by someone’s death, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedAgentAffectedCessationMessageLSP: String = "TBC cessation - I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedAgentAffectedCrimeMessageLSP: String = "because I was affected by a crime, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedAgentAffectedFireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedAgentAffectedHealthMessageLSP: String = "oherwydd salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 January 2022"
    override val agentPlannedAgentAffectedTechnicalIssueMessageLSP: String = "because of software or technology issues, I was not able to send the submission due on 1 January 2022 (Welsh)"
    override val agentPlannedAgentAffectedUnexpectedHospitalMessageLSP: String = "oherwydd arhosiad yn yr ysbyty, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 January 2022"
    override val agentPlannedAgentAffectedOtherMessageLSP: String = "I was not able to send the submission due on 1 January 2022 (Welsh)"
    
    override val honestyDeclarationLPP = "no one else was available to make the payment for me (Welsh)"
    override val honestyDeclarationHospital = "the timing of the hospital stay was unexpected (Welsh)"
    override val honestyDeclarationHealth = "the timing of the health issue was unexpected (Welsh)"
    override val honestyDeclarationInfo = "I will provide honest and accurate information in this appeal (Welsh)"
    override val honestyDeclarationInfoReview= "I confirm that I will provide honest and accurate information in this request for a review. (Welsh)"
    override val acceptAndContinue = "Accept and continue (Welsh)"

  }
}
