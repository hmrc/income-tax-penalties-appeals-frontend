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
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.{AppealStartView, ReviewAppealStartView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

class AppealStartController @Inject()(appealStart: AppealStartView,
                                      reviewAppealStartView: ReviewAppealStartView,
                                      val authActions: AuthActions,
                                      override val controllerComponents: MessagesControllerComponents
                                     )(implicit timeMachine: TimeMachine, val appConfig: AppConfig) extends FrontendBaseController with I18nSupport with FeatureSwitching {

  def onPageLoad(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers() { implicit user =>
    Ok(
      if(user.is2ndStageAppeal) renderReviewAppeal()
      else renderAppealStartAppeal()
    )
  }

  private def renderReviewAppeal()(implicit user: CurrentUserRequestWithAnswers[_]): Html =
    reviewAppealStartView(
      user.isAppealLate(),
      if(user.isLPP && user.hasMultipleLPPs) routes.JointAppealController.onPageLoad()
      else routes.ReasonableExcuseController.onPageLoad(isAgent = user.isAgent)
    )

  private def renderAppealStartAppeal()(implicit user: CurrentUserRequestWithAnswers[_]): Html = {
    val redirect =
      (user.isAgent, user.isLPP, user.hasMultipleLPPs) match {
        case (true, false, _) => routes.WhoPlannedToSubmitController.onPageLoad()
        case (_, _, true) => routes.JointAppealController.onPageLoad()
        case _ => routes.ReasonableExcuseController.onPageLoad(isAgent = user.isAgent)
      }

    appealStart(user.isAppealLate(), redirect)
  }
}
