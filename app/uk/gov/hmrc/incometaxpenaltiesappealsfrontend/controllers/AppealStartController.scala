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
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.AuthenticatedController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.AppealStartPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealStartController @Inject()(appealStartPage: AppealStartPage,
                                      val authConnector: AuthConnector
                                     )(implicit mcc: MessagesControllerComponents,
                                       ec: ExecutionContext,
                                       val appConfig: AppConfig) extends AuthenticatedController(mcc) with I18nSupport with FeatureSwitching {
  def onPageLoad(): Action[AnyContent] = isAuthenticated {
    implicit request =>
      implicit currentUser =>
        //      logger.debug(s"[AppealStartController][onPageLoad] - Session keys received: \n" +
        //        s"Appeal Type = ${userRequest.answers.getAnswer[PenaltyTypeEnum.Value](IncomeTaxSessionKeys.appealType)}, \n" +
        //        s"Penalty Number = ${userRequest.answers.getAnswer[String](IncomeTaxSessionKeys.penaltyNumber)}, \n" +
        //        s"Start date of period = ${userRequest.answers.getAnswer[LocalDate](IncomeTaxSessionKeys.startDateOfPeriod)}, \n" +
        //        s"End date of period = ${userRequest.answers.getAnswer[LocalDate](IncomeTaxSessionKeys.endDateOfPeriod)}, \n" +
        //        s"Due date of period = ${userRequest.answers.getAnswer[LocalDate](IncomeTaxSessionKeys.dueDateOfPeriod)}, \n" +
        //        s"Is find out how to appeal = ${userRequest.answers.getAnswer[Boolean](IncomeTaxSessionKeys.isFindOutHowToAppeal)}, \n" +
        //        s"Date communication sent of period = ${userRequest.answers.getAnswer[LocalDate](IncomeTaxSessionKeys.dateCommunicationSent)}, \n")


        Future.successful(Ok(appealStartPage(
          true, currentUser.isAgent
        )))
  }

  //  private def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
  //      val dateCommunicationSentParsedAsLocalDate = userRequest.answers.getAnswer[LocalDate](IncomeTaxSessionKeys.dateCommunicationSent).get
  //      dateCommunicationSentParsedAsLocalDate.isBefore(getFeatureDate.minusDays(appConfig.daysRequiredForLateAppeal))
  //  }

}
