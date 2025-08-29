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

import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{CurrentUserRequest, CurrentUserRequestWithAnswers}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.PenaltyData
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.routes.PageNotFoundController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersAction @Inject()(sessionService: UserAnswersService,
                                  errorHandler: ErrorHandler,
                                  appConfig: AppConfig)
                                 (implicit val executionContext: ExecutionContext) extends ActionRefiner[CurrentUserRequest, CurrentUserRequestWithAnswers] {

  override protected def refine[A](request: CurrentUserRequest[A]): Future[Either[Result, CurrentUserRequestWithAnswers[A]]] = {
    println(s"request_session: ${request.session}")
    request.session.get(IncomeTaxSessionKeys.journeyId).fold[Future[Either[Result, CurrentUserRequestWithAnswers[A]]]](
      { // if no journey id is found -> go back the home page
        logger.warn(s"[DataRetrievalAction][refine] No journey ID was found in the session for MTDITID: ${request.mtdItId}")
        //  Bypass/unknown journey → send to the new error page
        Future(Left(Redirect(appConfig.penaltiesHomePage(request.isAgent))))
//        Future.successful(Left(Redirect(PageNotFoundController.onPageLoad(isAgent = request.isAgent))))
      }

    )(
      journeyId => {
        sessionService.getUserAnswers(journeyId).flatMap {
          case Some(storedAnswers) =>
            println("11111")
            storedAnswers.getAnswerForKey[PenaltyData](IncomeTaxSessionKeys.penaltyData) match {
              case Some(penaltyData) =>
                println("22222")
                Future(Right(CurrentUserRequestWithAnswers(storedAnswers, penaltyData)(request)))
              case None =>
                println("33333")
                logger.warn(s"[DataRetrievalActionImpl][refine] No Penalty Appeal Data found in User Answers found for MTDITID: ${request.mtdItId}, journey ID: $journeyId")
                // Answers exist but missing critical data → treat this as the bypass maybeee???
                 Future(Left(Redirect(appConfig.penaltiesHomePage(request.isAgent))))
               // Future.successful(Left(Redirect(PageNotFoundController.onPageLoad(isAgent = request.isAgent))))

            }
          case None =>
            println("444444")
            logger.warn(s"[DataRetrievalActionImpl][refine] No User Answers found for MTDITID: ${request.mtdItId}, journey ID: $journeyId")
            // journeyId present but nothing stored → likely bookmarked/expired too??!
             Future(Left(Redirect(appConfig.penaltiesHomePage(request.isAgent))))
            // Future.successful(Left(Redirect(PageNotFoundController.onPageLoad(isAgent = request.isAgent))))
        }.recoverWith {
          case e =>
            logger.error(s"[DataRetrievalActionImpl][refine] Failed to query mongo for journey data with message: ${e.getMessage}")
            errorHandler.internalServerErrorTemplate(request).map(page => Left(InternalServerError(page)))
        }
      }
    )
  }
}