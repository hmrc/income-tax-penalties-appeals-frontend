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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions

import com.google.inject.Singleton
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.AuthorisedUserRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil.AuthReferenceExtractor
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAndRetrieveAgent @Inject()(override val authConnector: AuthConnector,
                                          val parser: BodyParsers.Default,
                                          val appConfig: AppConfig,
                                          val errorHandler: ErrorHandler,
                                          mcc: MessagesControllerComponents)

  extends AuthoriseHelper
    with ActionRefiner[Request, AuthorisedUserRequest]
    with AuthorisedFunctions
    with ActionBuilder[AuthorisedUserRequest, AnyContent]
    with Logging {

    implicit val executionContext: ExecutionContext = mcc.executionContext

    override protected def refine[A](request: Request[A]): Future[Either[Result, AuthorisedUserRequest[A]]] = {

      implicit val hc: HeaderCarrier = HeaderCarrierConverter
        .fromRequestAndSession(request, request.session)

      implicit val req: Request[A] = request

      val isAgent: Predicate = Enrolment("HMRC-AS-AGENT") and AffinityGroup.Agent
      val isNotAgent: Predicate = AffinityGroup.Individual or AffinityGroup.Organisation

      authorised(isAgent or isNotAgent)
        .retrieve(affinityGroup and allEnrolments) {
          case Some(AffinityGroup.Agent) ~ enrolments =>
            Future.successful(
              Right(AuthorisedUserRequest(AffinityGroup.Agent, enrolments.agentReferenceNumber))
            )
          case _ =>
            logger.info("Individual on agent endpoint")
            Future.successful(
              Left(Redirect(appConfig.ITSAPenaltiesAppealsHomeUrl(false))))
        }.recoverWith {
          case authorisationException: AuthorisationException =>
            handleAuthFailure(authorisationException, isAgent = true)(implicitly, implicitly, logger)
              .map(Left(_))
        }
    }
}
