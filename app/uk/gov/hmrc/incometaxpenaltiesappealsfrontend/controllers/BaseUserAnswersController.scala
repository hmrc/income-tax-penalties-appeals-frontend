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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.libs.json.Reads
import play.api.mvc.Result
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.Page
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

trait BaseUserAnswersController extends FrontendBaseController with I18nSupport {

  val errorHandler: ErrorHandler

  def withAnswer[A](page: Page[A])(f: A => Future[Result])
                   (implicit user: CurrentUserRequestWithAnswers[_], reads: Reads[A], ec: ExecutionContext): Future[Result] =
    user.userAnswers.getAnswer[A](page) match {
      case Some(value) => f(value)
      case None =>
        logger.warn(s"[BaseUserAnswersController][withAnswer] No answer found for pageKey: ${page.key}, mtditid: ${user.mtdItId}")
        //TODO: In future, redirect to a SessionTimeout page, or JourneyExpired page that the User can recover from???
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }
}
