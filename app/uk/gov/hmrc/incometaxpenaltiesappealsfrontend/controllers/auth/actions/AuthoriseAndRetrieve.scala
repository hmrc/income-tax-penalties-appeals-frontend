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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions

import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.IncomeTaxSessionDataConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledIndividual, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil._
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAndRetrieve @Inject()(override val authConnector: AuthConnector,
                                     val parser: BodyParsers.Default,
                                     sessionDataConnector: IncomeTaxSessionDataConnector,
                                     val appConfig: AppConfig,
                                     val errorHandler: ErrorHandler,
                                     mcc: MessagesControllerComponents)
  extends AuthoriseHelper with ActionRefiner[Request, CurrentUserRequest] with AuthorisedFunctions with ActionBuilder[CurrentUserRequest, AnyContent] {

  lazy val logger: Logger = Logger(getClass)

  implicit val executionContext: ExecutionContext = mcc.executionContext

  override protected def refine[A](request: Request[A]): Future[Either[Result, CurrentUserRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter
      .fromRequestAndSession(request, request.session)

    implicit val req: Request[A] = request

    val isAgent: Predicate = Enrolment("HMRC-AS-AGENT") and AffinityGroup.Agent
    val isNotAgent: Predicate = AffinityGroup.Individual or AffinityGroup.Organisation

    authorised(isAgent or isNotAgent)
      .retrieve(affinityGroup and allEnrolments and nino) {
        case Some(AffinityGroup.Agent) ~ enrolments ~ _ =>
          checkAgentEnrolledWithClient(enrolments.agentReferenceNumber)
        case (_ ~ enrolments ~ Some(nino)) =>
          enrolments.mtdItId match {
          case Some(mtdItId) => Future.successful(Right(AuthorisedAndEnrolledIndividual(mtdItId, nino, None)))
          case None => logger.warn("Auth check - User does not have an HMRC-MTD-IT enrolment")
            //ToDo need create a not enrolled page
            errorHandler.internalServerErrorTemplate.map(html =>
              Left(InternalServerError(html))
            )
        }
        case _ => logger.warn("Missing Nino")
          //ToDo error handling for no nino
          errorHandler.internalServerErrorTemplate.map(html =>
            Left(InternalServerError(html))
          )
      }.recoverWith {
        case authorisationException: AuthorisationException =>
          handleAuthFailure(authorisationException, isAgent = false).map(Left(_))
      }

  }

  def checkAgentEnrolledWithClient[A](arn: Option[String])(implicit request: Request[A], hc: HeaderCarrier): Future[Either[Result, CurrentUserRequest[A]]] = {
    sessionDataConnector.getSessionData()(hc, executionContext).flatMap {
      case Right(Some(sessionData)) =>
        authorised(agentDelegatedAuthorityRule(sessionData.mtditid)) {
          Future.successful(Right(AuthorisedAndEnrolledAgent(sessionData, arn)))
        }.recoverWith {
          case authorisationException: AuthorisationException =>
            handleAuthFailure(authorisationException, isAgent = true).map(Left(_))
        }
      case Right(None) => Future.successful(
        Left(Redirect(appConfig.enterClientUTRVandCUrl))
      )
      case Left(_) => logger.error("unexpected error retrieving agent client data")
        errorHandler.internalServerErrorTemplate.map(html =>
          Left(InternalServerError(html)))
    }
  }
}
