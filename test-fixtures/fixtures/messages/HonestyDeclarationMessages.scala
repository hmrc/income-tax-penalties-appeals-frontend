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
    val crimeMessageLSP: String = "because I was affected by a crime, I was not able to send the submission due on 1 January 2022"
    val fireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was not able to send the submission due on 1 January 2022"
    val healthMessageLSP: String = "because of a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022"
    val technicalIssueMessageLSP: String = "because of a software or technology issue, I was not able to send the submission due on 1 January 2022"
    val unexpectedHospitalMessageLSP: String = "because of a stay in hospital, I was not able to send the submission due on 1 January 2022"
    val otherMessageLSP: String = "I was not able to send the submission due on 1 January 2022"

    val bereavementMessageLPP: String = "because I was affected by someone’s death, I was not able to make the payment due on 1 January 2022"
    val crimeMessageLPP: String = "because I was affected by a crime, I was not able to make the payment due on 1 January 2022"
    val fireOrFloodReasonMessageLPP: String = "because of a fire or flood, I was not able to make the payment due on 1 January 2022"
    val healthMessageLPP: String = "because of a serious or life-threatening health issue, I was not able to make the payment due on 1 January 2022"
    val technicalIssueMessageLPP: String = "because of a software or technology issue, I was not able to make the payment due on 1 January 2022"
    val unexpectedHospitalMessageLPP: String = "because of a stay in hospital, I was not able to make the payment due on 1 January 2022"
    val otherMessageLPP: String = "I was not able to make the payment due on 1 January 2022"

    val agentBereavementMessageLPP: String = "because my client was affected by someone’s death, they were not able to make the payment due on 1 January 2022"
    val agentCrimeMessageLPP: String = "because my client was affected by a crime, they were not able to make the payment due on 1 January 2022"
    val agentFireOrFloodReasonMessageLPP: String = "because my client was affected by a fire or flood, they were not able to make the payment due on 1 January 2022"
    val agentHealthMessageLPP: String = "because my client had a serious or life-threatening health issue, they were not able to make the payment due on 1 January 2022"
    val agentTechnicalIssueMessageLPP: String = "because my client was affected by software or technology issues, they were not able to make the payment due on 1 January 2022"
    val agentUnexpectedHospitalMessageLPP: String = "because my client had an unexpected stay in hospital, they were not able to make the payment due on 1 January 2022"
    val agentOtherMessageLPP: String = "my client was not able to make the payment due on 1 January 2022"

    val clientPlannedBereavementMessageLSP: String = "because my client was affected by someone’s death, they were not able to send the submission due on 1 January 2022"
    val clientPlannedCrimeMessageLSP: String = "because my client was affected by a crime, they were not able to send the submission due on 1 January 2022"
    val clientPlannedFireOrFloodReasonMessageLSP: String = "because of a fire or flood, my client was not able to send the submission due on 1 January 2022"
    val clientPlannedHealthMessageLSP: String = "because my client had a serious or life-threatening health issue, they were not able to send the submission due on 1 January 2022"
    val clientPlannedTechnicalIssueMessageLSP: String = "because of a software or technology issue, my client was not able to send the submission due on 1 January 2022"
    val clientPlannedUnexpectedHospitalMessageLSP: String = "because my client had an unexpected hospital stay, they were not able to send the submission due on 1 January 2022"
    val clientPlannedOtherMessageLSP: String = "my client was not able to send the submission due on 1 January 2022"
    
    val agentPlannedClientAffectedBereavementMessageLSP: String = "because my client was affected by someone’s death, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedCrimeMessageLSP: String = "because my client was affected by a crime, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedFireOrFloodReasonMessageLSP: String = "because my client was affected by a fire or flood, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedHealthMessageLSP: String = "because my client had a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedTechnicalIssueMessageLSP: String = "because my client was affected by software or technology issues, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedUnexpectedHospitalMessageLSP: String = "because my client had an unexpected hospital stay, I was not able to send the submission due on 1 January 2022"
    val agentPlannedClientAffectedOtherMessageLSP: String = "because of an issue affecting my client, I was not able to send the submission due on 1 January 2022"
    
    val agentPlannedAgentAffectedBereavementMessageLSP: String = "because I was affected by someone’s death, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedCrimeMessageLSP: String = "because I was affected by a crime, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedFireOrFloodReasonMessageLSP: String = "because of a fire or flood, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedHealthMessageLSP: String = "because of a serious or life-threatening health issue, I was not able to send the submission due on 1 January 2022"
    val agentPlannedAgentAffectedTechnicalIssueMessageLSP: String = "because of a software or technology issue, I was not able to send the submission due on 1 January 2022"
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
    override val headingAndTitle: String = "Datganiad gonestrwydd"
    override val confirmParagraph:String = "Rwy’n cadarnhau’r canlynol:"
    
    override val bereavementMessageLSP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith arna i, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val crimeMessageLSP: String = "oherwydd bod trosedd wedi cael effaith arna i, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val fireOrFloodReasonMessageLSP: String = "oherwydd tân neu lifogydd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val healthMessageLSP: String = "oherwydd salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val technicalIssueMessageLSP: String = "oherwydd problemau technegol neu broblemau meddalwedd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val unexpectedHospitalMessageLSP: String = "oherwydd arhosiad yn yr ysbyty, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val otherMessageLSP: String = "nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"

    override val bereavementMessageLPP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith arna i, nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val crimeMessageLPP: String = "oherwydd bod trosedd wedi cael effaith arna i, nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val fireOrFloodReasonMessageLPP: String = "oherwydd tân neu lifogydd, nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val healthMessageLPP: String = "oherwydd salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val technicalIssueMessageLPP: String = "oherwydd problemau technegol neu broblemau meddalwedd, nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val unexpectedHospitalMessageLPP: String = "oherwydd arhosiad yn yr ysbyty, nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val otherMessageLPP: String = "nid oedd modd i mi dalu erbyn y dyddiad cau, 1 Ionawr 2022"

    override val agentBereavementMessageLPP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith ar fy nghleient, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentCrimeMessageLPP: String = "oherwydd bod trosedd wedi cael effaith ar fy nghleient, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentFireOrFloodReasonMessageLPP: String = "oherwydd bod tân neu lifogydd wedi cael effaith ar fy nghleient, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentHealthMessageLPP: String = "oherwydd bod gan fy nghleient salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentTechnicalIssueMessageLPP: String = "oherwydd problemau technegol neu broblemau meddalwedd, nid oedd modd i fy nghleient dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentUnexpectedHospitalMessageLPP: String = "oherwydd bod fy nghleient wedi aros yn yr ysbyty yn annisgwyl, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentOtherMessageLPP: String = "nid oedd modd i fy nghleient dalu erbyn y dyddiad cau, 1 Ionawr 2022"

    override val clientPlannedBereavementMessageLSP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith ar fy nghleient, nid oedd modd iddo anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val clientPlannedCrimeMessageLSP: String = "oherwydd bod trosedd wedi cael effaith ar fy nghleient, nid oedd modd iddo anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val clientPlannedFireOrFloodReasonMessageLSP: String = "oherwydd tân neu lifogydd, nid oedd modd i fy nghleient anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val clientPlannedHealthMessageLSP: String = "oherwydd salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd i fy nghleient anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val clientPlannedTechnicalIssueMessageLSP: String = "oherwydd problemau technegol neu broblemau meddalwedd, nid oedd modd i fy nghleient anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val clientPlannedUnexpectedHospitalMessageLSP: String = "oherwydd bod fy nghleient wedi aros yn yr ysbyty yn annisgwyl, nid oedd modd iddo anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val clientPlannedOtherMessageLSP: String = "nid oedd modd i fy nghleient anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    
    override val agentPlannedClientAffectedBereavementMessageLSP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith ar fy nghleient, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedClientAffectedCrimeMessageLSP: String = "oherwydd bod trosedd wedi cael effaith ar fy nghleient, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedClientAffectedFireOrFloodReasonMessageLSP: String = "oherwydd bod tân neu lifogydd wedi cael effaith ar fy nghleient, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedClientAffectedHealthMessageLSP: String = "oherwydd bod gan fy nghleient salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedClientAffectedTechnicalIssueMessageLSP: String = "oherwydd bod problemau technegol neu broblemau meddalwedd wedi cael effaith ar fy nghleient, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedClientAffectedUnexpectedHospitalMessageLSP: String = "oherwydd bod fy nghleient wedi aros yn yr ysbyty yn annisgwyl, nid oedd modd iddo dalu erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedClientAffectedOtherMessageLSP: String = "oherwydd bod fy nghleient wedi wynebu problem, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedBereavementMessageLSP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith arna i, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedCrimeMessageLSP: String = "oherwydd bod marwolaeth rhywun wedi cael effaith arna i, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedFireOrFloodReasonMessageLSP: String = "oherwydd tân neu lifogydd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedHealthMessageLSP: String = "oherwydd salwch difrifol neu salwch a oedd yn berygl i fywyd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedTechnicalIssueMessageLSP: String = "oherwydd problemau technegol neu broblemau meddalwedd, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedUnexpectedHospitalMessageLSP: String = "oherwydd arhosiad yn yr ysbyty, nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    override val agentPlannedAgentAffectedOtherMessageLSP: String = "nid oedd modd i mi anfon y cyflwyniad erbyn y dyddiad cau, 1 Ionawr 2022"
    
    override val honestyDeclarationLPP = "nid oedd neb arall ar gael i dalu ar fy rhan"
    override val honestyDeclarationHospital = "roedd yr arhosiad hwn yn yr ysbyty yn un annisgwyl"
    override val honestyDeclarationHealth = "roedd y salwch hwn yn annisgwyl"
    override val honestyDeclarationInfo = "byddaf yn rhoi gwybodaeth onest a chywir yn yr apêl hon"
    override val honestyDeclarationInfoReview= "Rwy’n cadarnhau y byddaf yn rhoi gwybodaeth onest a chywir yn y cais hwn i gael adolygiad."
    override val acceptAndContinue = "Derbyn a pharhau"

  }
}
