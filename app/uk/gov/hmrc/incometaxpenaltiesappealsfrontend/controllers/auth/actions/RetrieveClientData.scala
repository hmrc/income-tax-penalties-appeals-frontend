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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.IncomeTaxSessionDataConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedUserRequest}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveClientData @Inject()(sessionDataConnector: IncomeTaxSessionDataConnector,
                                   errorHandler: ErrorHandler,
                                   mcc: MessagesControllerComponents,
                                   appConfig: AppConfig)
  extends ActionRefiner[AuthorisedUserRequest, AuthorisedAndEnrolledAgent] {

  lazy val logger: Logger = Logger(getClass)

    implicit val executionContext: ExecutionContext = mcc.executionContext

    override protected def refine[A](request: AuthorisedUserRequest[A]): Future[Either[Result, AuthorisedAndEnrolledAgent[A]]] = {

      implicit val r: Request[A] = request
      implicit val hc: HeaderCarrier = HeaderCarrierConverter
        .fromRequestAndSession(request, request.session)

      sessionDataConnector.getSessionData()(hc, executionContext).flatMap {
        case Right(Some(sessionData)) => Future.successful(
          Right(AuthorisedAndEnrolledAgent(sessionData, request.arn))
        )
        case Right(None) => Future.successful(
          Left(Redirect(appConfig.enterClientUTRVandCUrl))
        )
        case Left(_) => logger.error("unexpected error retrieving agent client data")
          errorHandler.internalServerErrorTemplate.map(html =>
          Left(InternalServerError(html)))
      }
    }
}
