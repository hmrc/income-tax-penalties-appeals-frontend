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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.mocks


import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, nino}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, BearerTokenExpired, Enrolment, EnrolmentIdentifier, Enrolments, InsufficientEnrolments, InternalError, MissingBearerToken, UnsupportedAffinityGroup}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil.incomeTaxEnrolmentKey

import scala.concurrent.{ExecutionContext, Future}

trait AuthMocks extends MockFactory {
  _: TestSuite =>

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  lazy val predicateInitial: Predicate = {
    val isAgent: Predicate = Enrolment("HMRC-AS-AGENT") and AffinityGroup.Agent
    val isNotAgent: Predicate = AffinityGroup.Individual or AffinityGroup.Organisation
    isAgent or isNotAgent
  }

  lazy val predicateMTDIndOrOrg: Predicate = {
    val predicate = Enrolment(incomeTaxEnrolmentKey) and
      (AffinityGroup.Organisation or AffinityGroup.Individual)
    AffinityGroup.Agent or predicate
  }

  lazy val retrievalInitial: Retrieval[Option[AffinityGroup] ~ Enrolments ~ Option[String]] = affinityGroup and allEnrolments and nino
  lazy val retrievalAgent: Retrieval[Option[AffinityGroup] ~ Enrolments] = affinityGroup and allEnrolments

  lazy val agentEnrolment = Enrolments(
    Set(Enrolment(
      key = "HMRC-AS-AGENT",
      identifiers = Seq(EnrolmentIdentifier("AgentReferenceNumber", "1234567")),
      state = "Activated"
    ))
  )

  lazy val mtdIndorOrgEnrolment = Enrolments(
    Set(Enrolment(
      key = "HMRC-MTD-IT",
      identifiers = Seq(EnrolmentIdentifier("MTDITID", "1234567")),
      state = "Activated"
    ))
  )

  def getEnrolments(af: AffinityGroup, hasEnrolment: Boolean): Enrolments = {
    if (hasEnrolment && af == AffinityGroup.Agent) {
      agentEnrolment
    } else if (hasEnrolment) {
      mtdIndorOrgEnrolment
    } else {
      Enrolments(Set.empty[Enrolment])
    }
  }

  def getInitialAuthResponse(af: AffinityGroup,
                             hasNino: Boolean,
                             hasEnrolment: Boolean): Option[AffinityGroup] ~ Enrolments ~ Option[String] = {
    val nino = if (hasNino) Some("AA123456A") else None
    val enrolments = getEnrolments(af, hasEnrolment)
    new~(new~(Some(af), enrolments), nino)
  }

  def getAgentAuthResponse(hasEnrolment: Boolean = true, af: AffinityGroup = AffinityGroup.Agent) = {
    val enrolments = getEnrolments(af, hasEnrolment)
    new~(Some(af), enrolments)
  }

  def mockAuthenticated(af: AffinityGroup, hasNino: Boolean = true, hasEnrolment: Boolean = true): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(predicateInitial, retrievalInitial, *, *)
      .returning(Future.successful(getInitialAuthResponse(af, hasNino, hasEnrolment)))
  }

  def mockAuthenticatedAgent(hasEnrolment: Boolean = true, af: AffinityGroup = AffinityGroup.Agent): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(predicateInitial, retrievalAgent, *, *)
      .returning(Future.successful(getAgentAuthResponse(hasEnrolment, af)))
  }

  def mockAuthenticatedMTDIndorOrg(af: AffinityGroup, hasNino: Boolean = true, hasEnrolment: Boolean = true): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(predicateMTDIndOrOrg, retrievalInitial, *, *)
      .returning(Future.successful(getInitialAuthResponse(af, hasNino, hasEnrolment)))
  }

  def mockAuthEnrolledAgent(): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[Unit])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, EmptyRetrieval, *, *)
      .returning(Future.successful(EmptyRetrieval))
  }

  def mockAgentWithoutDelegatedEnrolment(): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[Unit])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, EmptyRetrieval, *, *)
      .returning(Future.failed(InsufficientEnrolments("No MTDIT enrolment")))
  }

  def mockAuthenticatedWithNoAffinityGroup(): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(UnsupportedAffinityGroup("No affinity group")))

  def mockAgentWithoutAgentEnrolment(): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(InsufficientEnrolments("No HMRC-AS-AGENT enrolment")))

  def mockAuthenticatedWithNoMTDEnrolment(): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(InsufficientEnrolments("No MTDIT enrolment")))

  def mockAuthenticatedNoActiveSession(): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(MissingBearerToken("No token")))

  def mockAuthenticatedBearerTokenExpired(): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(BearerTokenExpired("expired")))

  def mockAuthenticatedFailure(): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(InternalError("There has been an error")))

}
