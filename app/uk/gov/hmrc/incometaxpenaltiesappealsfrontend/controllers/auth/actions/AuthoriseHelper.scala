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

import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil.agentEnrolmentKey

import scala.concurrent.{ExecutionContext, Future}

trait AuthoriseHelper {

  val appConfig: AppConfig
  val errorHandler: ErrorHandler

  val logger: Logger

  def handleAuthFailure(authorisationException: AuthorisationException,
                        isAgent: Boolean)
                       (implicit rh: RequestHeader,
                        ec: ExecutionContext): Future[Result] = {
    authorisationException match {
      case _: BearerTokenExpired =>
        logger.warn("Bearer Token Timed Out.")
        //ToDo need create a timeout page
        errorHandler.internalServerErrorTemplate.map(html =>
          InternalServerError(html)
        )
      case insufficientEnrolments: InsufficientEnrolments if insufficientEnrolments.msg.contains(agentEnrolmentKey) =>
        logger.warn(s"Agent enrolment missing")
        //ToDo need create not an agent page
        errorHandler.internalServerErrorTemplate.map(html =>
          InternalServerError(html)
        )
      case insufficientEnrolments: InsufficientEnrolments if !isAgent=>
        logger.warn(s"Insufficient enrolments: ${insufficientEnrolments.msg}")
        //ToDo need create a not enrolled page
        errorHandler.internalServerErrorTemplate.map(html =>
          InternalServerError(html)
        )
      case insufficientEnrolments: InsufficientEnrolments =>
        logger.warn(s"Insufficient enrolments: ${insufficientEnrolments.msg}")
        //ToDo need create agent not enrolled page
        errorHandler.internalServerErrorTemplate.map(html =>
          InternalServerError(html)
        )
      case authorisationException: AuthorisationException =>
        logger.error(s"Unauthorised request: ${authorisationException.reason}. Redirect to Sign In.")
        Future.successful(Redirect(appConfig.signInUrl))
    }
  }
}
