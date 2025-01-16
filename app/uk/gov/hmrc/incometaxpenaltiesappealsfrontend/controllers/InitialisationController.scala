/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.AuthAction
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.SessionService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.{IncomeTaxSessionKeys, UUIDGenerator}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class InitialisationController @Inject()(val authorised: AuthAction,
                                         override val controllerComponents: MessagesControllerComponents,
                                         userAnswersService: SessionService,
                                         uuid: UUIDGenerator
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(penaltyId: String): Action[AnyContent] = authorised.async { implicit user =>

    //TODO: In future story, this controller will need to validate that the penaltyId exists for the User and is in a valid state to be appealed

    val journeyId = uuid.generateUUID
    logger.debug(s"[InitialisationController][onPageLoad] Starting journey for penaltyId: $penaltyId, created journeyId: $journeyId")

    val answers = UserAnswers(journeyId)
      .setAnswer(IncomeTaxSessionKeys.penaltyNumber, penaltyId)

    userAnswersService.updateAnswers(answers).map { _ =>
      Redirect(routes.AppealStartController.onPageLoad())
        .addingToSession(IncomeTaxSessionKeys.journeyId -> journeyId)
    }
  }
}
