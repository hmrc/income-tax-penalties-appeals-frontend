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
import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledIndividual, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil.{AuthReferenceExtractor, incomeTaxEnrolmentKey}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAndRetrieveMTDIndividual @Inject()(override val authConnector: AuthConnector,
                                                  val parser: BodyParsers.Default,
                                                  val appConfig: AppConfig,
                                                  val errorHandler: ErrorHandler,
                                                  mcc: MessagesControllerComponents)
  extends AuthoriseHelper with ActionRefiner[Request, AuthorisedAndEnrolledIndividual] with AuthorisedFunctions with ActionBuilder[CurrentUserRequest, AnyContent]{

  implicit val executionContext: ExecutionContext = mcc.executionContext

  lazy val logger = Logger(getClass)

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthorisedAndEnrolledIndividual[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter
      .fromRequestAndSession(request, request.session)

    implicit val req: Request[A] = request

    // authorise on HMRC-MTD-IT enrolment and Individual / Organisation affinity group
    val predicate: Predicate =
      Enrolment(incomeTaxEnrolmentKey) and
        (AffinityGroup.Organisation or AffinityGroup.Individual)

    authorised(AffinityGroup.Agent or predicate)
      .retrieve(affinityGroup and allEnrolments and nino) {
        case Some(AffinityGroup.Agent) ~ _ ~ _ =>
          logger.info("Agent on individual endpoint")
          Future.successful(
            Left(Redirect(appConfig.ITSAPenaltiesAppealsHomeUrl)))
        case _ ~ enrolments ~ Some(nino) =>
          enrolments.mtdItId match {
            case Some(mtditid) => Future.successful(
              Right(AuthorisedAndEnrolledIndividual(mtditid, nino, navBar = None))
            )
            case None =>
              logger.error("Auth check - User does not have an HMRC-MTD-IT enrolment")
            // ToDo this needs updating for unenrolled error when implemented
              errorHandler.internalServerErrorTemplate.map(html => Left(
                InternalServerError(html)
              ))
          }
        case _ =>
          logger.error("Auth check - User does not have an nino enrolment")
          // ToDo this needs updating for user without nino error when implemented
          errorHandler.internalServerErrorTemplate.map(html => Left(
            InternalServerError(html)
          ))
      }.recoverWith {
        case authorisationException: AuthorisationException => handleAuthFailure(authorisationException, isAgent = false).map(Left(_))
      }
  }

}