/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}

object EnrolmentUtil {

  val agentEnrolmentKey = "HMRC-AS-AGENT"
  val incomeTaxEnrolmentKey = "HMRC-MTD-IT"
  val arnKey = "AgentReferenceNumber"
  val mtdItIdKey = "MTDITID"
  val agentDelegatedAuthRuleKey = "mtd-it-auth"

  val agentDelegatedAuthorityRule: String => Enrolment =
    mtdItId =>
      Enrolment(incomeTaxEnrolmentKey)
        .withIdentifier(mtdItIdKey, mtdItId)
        .withDelegatedAuthRule(agentDelegatedAuthRuleKey)


  implicit class AuthReferenceExtractor(enrolments: Enrolments) {

    def agentReferenceNumber: Option[String] =
      for {
        agentEnrolment <- enrolments.getEnrolment(agentEnrolmentKey)
        identifier <- agentEnrolment.getIdentifier(arnKey)
        arn = identifier.value
      } yield arn

    def mtdItId: Option[String] =
      for {
        incomeTaxEnrolment <- enrolments.getEnrolment(incomeTaxEnrolmentKey)
        identifier <- incomeTaxEnrolment.getIdentifier(mtdItIdKey)
        mtdItId = identifier.value
      } yield mtdItId

  }

  def buildItsaEnrolment(mtdItid: String) : String =
    incomeTaxEnrolmentKey + "~" + mtdItIdKey + "~" + mtdItid

}
