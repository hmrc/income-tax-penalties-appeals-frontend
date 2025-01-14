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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates

import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequest
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil.{AuthReferenceExtractor, agentDelegatedAuthorityRule}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthAction @Inject()(mcc: MessagesControllerComponents,
                           appConfig: AppConfig,
                           override val authConnector: AuthConnector)(implicit override val executionContext: ExecutionContext)
  extends ActionBuilder[CurrentUserRequest, AnyContent]
    with ActionFunction[Request, CurrentUserRequest]
    with AuthorisedFunctions {

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], f: CurrentUserRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val _req: Request[A] = request

    authorised().retrieve(affinityGroup and allEnrolments) {
      case Some(AffinityGroup.Agent) ~ _ =>
        logger.debug("Auth check - Authorising user as Agent")
        request.session.get(IncomeTaxSessionKeys.agentSessionMtditid) match {
          case Some(mtdItId) =>
            authorised(agentDelegatedAuthorityRule(mtdItId)).retrieve(allEnrolments) {
              enrolments =>
                enrolments.agentReferenceNumber match {
                  case Some(arn) =>
                    f(CurrentUserRequest(mtdItId, Some(arn)))
                  case _ =>
                    logger.error("Auth check - Agent does not have HMRC-AS-AGENT enrolment")
                    throw InsufficientEnrolments("User does not have Agent Enrolment")
                }
            }
          case _ => ??? // TODO handle this case by trying the new storage system for income tax
        }
      case Some(_) ~ enrolments  =>
        logger.debug("Auth check - Authorising user as Individual")
        enrolments.mtdItId match {
          case Some(mtdItId) =>
            f(CurrentUserRequest(mtdItId))
          case _ =>
            logger.error("Auth check - User does not have an HMRC-MTD-IT enrolment")
            throw InsufficientEnrolments("User does not have an HMRC-MTD-IT enrolment")
        }
      case _ =>
        logger.error("Auth check - Invalid affinity group")
        throw UnsupportedAffinityGroup("Invalid affinity group")
    }.recover {
      case _: NoActiveSession =>
        logger.error(s"Auth check - No active session. Redirecting to ${appConfig.signInUrl}")
        Redirect(appConfig.signInUrl)
      case _: AuthorisationException =>
        logger.error(s"Auth check - User not authorised. Redirecting to ${appConfig.signInUrl}")
        Redirect(appConfig.signInUrl)
    }
  }
}
