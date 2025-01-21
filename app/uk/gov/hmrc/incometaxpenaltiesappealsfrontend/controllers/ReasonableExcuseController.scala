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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.forms.ReasonableExcusesForm
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.IncomeTaxSessionKeys.reasonableExcuse
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html.ReasonableExcusePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.predicates.{AuthAction, UserAnswersAction}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UserAnswersService
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.NavBarRetrievalAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ReasonableExcuseController @Inject()(reasonableExcusePage: ReasonableExcusePage,
                                           val authorised: AuthAction,
                                           withNavBar: NavBarRetrievalAction,
                                           withAnswers: UserAnswersAction,
                                           userAnswersService: UserAnswersService,
                                           override val controllerComponents: MessagesControllerComponents
                                          )(implicit ec: ExecutionContext,
                                            val appConfig: AppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authorised andThen withNavBar) { implicit currentUser =>

        Ok(reasonableExcusePage(
          true, currentUser.isAgent, ReasonableExcusesForm.form
        ))
  }


  def submit(): Action[AnyContent] = (authorised andThen withNavBar andThen withAnswers).async { implicit user =>
        ReasonableExcusesForm.form.bindFromRequest().fold(
          formWithErrors =>
            Future(BadRequest(reasonableExcusePage(
              true, user.isAgent,
              formWithErrors
            ))),
          reasonableExcuse =>
              reasonableExcuse match {
                case ("technicalReason" | "bereavementReason" | "fireOrFloodReason" | "crimeReason") =>
                  val updatedAnswers = user.userAnswers.setAnswer(ReasonableExcusePage, reasonableExcuse)
                    userAnswersService.updateAnswers(updatedAnswers).map { _ =>
                      Redirect(routes.HonestyDeclarationController.onPageLoad ())
                    }

                case _ =>
                  Future(Redirect(routes.AppealStartController.onPageLoad()))
              }
        )
  }

}
