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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.MessageCountConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{AuthorisedAndEnrolledIndividual, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.BtaNavBarService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.navBar.PtaNavBar
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NavBarRetrievalAction @Inject()(val messageCountConnector: MessageCountConnector,
                                      val btaNavBarService: BtaNavBarService,
                                      val ptaNavBar: PtaNavBar)
                                     (implicit val appConfig: AppConfig,
                                      val executionContext: ExecutionContext,
                                      val messagesApi: MessagesApi) extends ActionRefiner[CurrentUserRequest, CurrentUserRequest] with I18nSupport {

  override def refine[A](request: CurrentUserRequest[A]): Future[Either[Result, CurrentUserRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request match {
      case _req: AuthorisedAndEnrolledIndividual[A] => request.session.get(IncomeTaxSessionKeys.origin) match {
        case Some("PTA") => handlePtaNavBar()(_req, implicitly)
        case Some("BTA") => handleBtaNavBar()(_req, implicitly)
        case _ =>
          logger.info("[NavBarRetrievalAction][refine] No origin found in session, not constructing a Nav Bar as can't determine PTA or BTA")
          Future.successful(Right(request))
      }
      case _ => Future.successful(Right(request))
    }
  }

  private def handlePtaNavBar[A]()(implicit request: AuthorisedAndEnrolledIndividual[A], hc: HeaderCarrier): Future[Either[Result, CurrentUserRequest[A]]] = {
    messageCountConnector.getMessageCount().map {
      case Right(count) =>
        Right(request.addNavBar(ptaNavBar(count.count)))
      case _ =>
        logger.warn("[NavBarRetrievalAction][refine] Failed to retrieve message count from 'message' microservice, continuing with 0 messages to continue gracefully")
        Right(request.addNavBar(ptaNavBar(0)))
    }
  }

  private def handleBtaNavBar[A]()(implicit request: AuthorisedAndEnrolledIndividual[A], hc: HeaderCarrier): Future[Right[Result, CurrentUserRequest[A]]] =
    btaNavBarService.retrieveBtaLinksAndRenderNavBar().map(btaNavBarHtml =>
      Right(btaNavBarHtml.fold[CurrentUserRequest[A]](request)(content => request.addNavBar(content)))
    )
}
