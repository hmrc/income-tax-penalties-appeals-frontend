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
import play.api.mvc.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.AuthorisedAndEnrolledAgent
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil.{agentDelegatedAuthorityRule, secondaryAgentDelegatedAuthorityRule}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.audit.SecondaryAgentAccessDeniedAuditModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.AuditService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAndRetrieveMtdAgent @Inject()(override val authConnector: AuthConnector,
                                             val appConfig: AppConfig,
                                             val errorHandler: ErrorHandler,
                                             auditService: AuditService,
                                             mcc: MessagesControllerComponents)
  extends AuthoriseHelper
    with ActionRefiner[AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledAgent]
    with AuthorisedFunctions
    with Logging {

  implicit val executionContext: ExecutionContext = mcc.executionContext

  override protected def refine[A](request: AuthorisedAndEnrolledAgent[A]): Future[Either[Result, AuthorisedAndEnrolledAgent[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter
      .fromRequestAndSession(request, request.session)
    implicit val req: AuthorisedAndEnrolledAgent[A] = request

    authorised(agentDelegatedAuthorityRule(request.mtdItId))
     {
      Future.successful(Right(request))
    }.recoverWith {
        case _ =>
          authorised(secondaryAgentDelegatedAuthorityRule(request.mtdItId))
          {
            auditService.audit(SecondaryAgentAccessDeniedAuditModel(request))
            Future.successful(Left(Redirect(routes.UnauthorisedErrorController.onPageLoad())))
          }.recoverWith {
            case authorisationException: AuthorisationException =>
              handleAuthFailure(authorisationException, isAgent = true)(implicitly, implicitly, logger).map(Left(_))
          }
    }
  }
}

