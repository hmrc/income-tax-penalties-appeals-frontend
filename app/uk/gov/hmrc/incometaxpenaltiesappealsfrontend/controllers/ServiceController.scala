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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.AppealStartPage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction
import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class ServiceController @Inject()(
                                   val authorised: AuthAction,
                                   withNavBar: NavBarRetrievalAction,
                                   startPage: AppealStartPage,
                                   override val controllerComponents: MessagesControllerComponents
                                 )(implicit appConfig: AppConfig) extends FrontendBaseController  with I18nSupport {

  def logout: Action[AnyContent] = Action {
    Redirect(appConfig.serviceSignOut).withNewSession
  }

  val homePage: Action[AnyContent] = (authorised andThen withNavBar) { implicit currentUserRequest =>
    Ok(startPage(true, currentUserRequest.isAgent))
      .addingToSession(IncomeTaxSessionKeys.pocAchievementDate -> LocalDate.now().toString)
  }

  val keepAlive: Action[AnyContent] = authorised { _ => NoContent }

}
