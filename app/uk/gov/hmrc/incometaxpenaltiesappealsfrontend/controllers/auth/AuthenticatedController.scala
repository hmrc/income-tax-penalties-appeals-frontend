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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth

import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUser
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.EnrolmentUtil._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class AuthenticatedController @Inject()(mcc: MessagesControllerComponents)
                                                (implicit ec: ExecutionContext, appConfig: AppConfig) extends FrontendController(mcc) with AuthorisedFunctions with I18nSupport {

  override implicit def request2Messages(implicit request: RequestHeader): Messages = {
    messagesApi.preferred(request)
  }

  def isAuthenticated(function: Request[AnyContent] => CurrentUser => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(affinityGroup and allEnrolments) {
        case Some(AffinityGroup.Agent) ~ _ =>
          logger.debug("Auth check - Authorising user as Agent")
          request.session.get(IncomeTaxSessionKeys.agentSessionMtditid) match {
            case Some(mtdItId) =>
              authorised(agentDelegatedAuthorityRule(mtdItId)).retrieve(allEnrolments) {
                enrolments =>
                  enrolments.agentReferenceNumber match {
                    case Some(arn) =>
                      function(request)(CurrentUser(mtdItId, Some(arn)))
                    case _ =>
                      logger.error("Auth check - Agent does not have HMRC-AS-AGENT enrolment")
                      throw InsufficientEnrolments("User does not have Agent Enrolment")
                  }
              }
            case _ => ??? // TODO handle this case by trying the new storage system for income tax
          }
        case Some(AffinityGroup.Individual) ~ enrolments => // TODO check if we allow Organisations too
          logger.debug("Auth check - Authorising user as Individual")
          enrolments.mtdItId match {
            case Some(mtdItId) =>
              function(request)(CurrentUser(mtdItId))
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



