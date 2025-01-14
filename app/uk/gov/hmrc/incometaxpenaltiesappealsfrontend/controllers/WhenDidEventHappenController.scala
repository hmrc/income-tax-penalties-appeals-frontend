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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.AuthAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class WhenDidEventHappenController @Inject()(whenDidEventHappenPage: WhenDidEventHappenPage,
                                             val authorised: AuthAction,
                                             override val controllerComponents: MessagesControllerComponents
                                            )(implicit ec: ExecutionContext,
                                              val appConfig: AppConfig) extends FrontendBaseController with I18nSupport with FeatureSwitching {
  def onPageLoad(): Action[AnyContent] = authorised { implicit currentUser =>
        val optReasonableExcuse = currentUser.session.get(IncomeTaxSessionKeys.reasonableExcuse)

        optReasonableExcuse match {
          case Some(reasonableExcuse) =>
           Ok(whenDidEventHappenPage(
              true, currentUser.isAgent, reasonableExcuse
            ))
          case _ =>
            Redirect(routes.ReasonableExcuseController.onPageLoad())
        }
  }


  def submit(): Action[AnyContent] = authorised { implicit currentUser =>
        val optReasonableExcuse = currentUser.session.get(IncomeTaxSessionKeys.reasonableExcuse)

        optReasonableExcuse match {
          case Some("technicalReason") =>
            Redirect(routes.WhenDidEventEndController.onPageLoad())
          case Some("bereavementReason" | "fireOrFloodReason") =>
            Redirect(routes.LateAppealController.onPageLoad())
          case Some("crimeReason") =>
            Redirect(routes.CrimeReportedController.onPageLoad())
          case _ =>
            Redirect(routes.AppealStartController.onPageLoad())
        }
  }

}
