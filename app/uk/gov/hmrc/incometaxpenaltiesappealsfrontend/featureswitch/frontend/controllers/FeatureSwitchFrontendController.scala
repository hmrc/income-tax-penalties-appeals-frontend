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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.services.FeatureSwitchRetrievalService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.featureswitch.frontend.views.html.feature_switch
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class FeatureSwitchFrontendController @Inject()(featureSwitchService: FeatureSwitchRetrievalService,
                                                featureSwitchView: feature_switch,
                                                mcc: MessagesControllerComponents
                                               )(implicit ec: ExecutionContext,
                                                 val appConfig: AppConfig) extends FrontendController(mcc) with FeatureSwitching with I18nSupport {


  def show(): Action[AnyContent] = Action.async {
    implicit req =>
      featureSwitchService.retrieveFeatureSwitches().map {
        featureSwitches =>
          Ok(featureSwitchView(featureSwitches, routes.FeatureSwitchFrontendController.submit()))
      }
  }

  def submit(): Action[Map[String, Seq[String]]] = Action.async(parse.formUrlEncoded) {
    implicit req =>
      featureSwitchService.updateFeatureSwitches(req.body.keys).map {
        featureSwitches =>
          Ok(featureSwitchView(featureSwitches, routes.FeatureSwitchFrontendController.submit()))
      }
  }

  def setTimeMachineDate(dateToSet: Option[String]): Action[AnyContent] = Action {
    dateToSet.fold({
      setFeatureDate(None)
      logger.info(s"[FeatureSwitchController][setFeatureDate] - Time machine reset to now (${LocalDate.now()})")
      Ok(s"Time machine set to: ${LocalDate.now()}")
    })(
      dateAsString => {
        val timeMachineStringDate = dateAsString.replace("/", "-")
        Try(LocalDateTime.parse(timeMachineStringDate)).fold(
          err => {
            logger.error(s"[FeatureSwitchController][setFeatureDate] - Exception was thrown when setting time machine date: ${err.getMessage}")
            BadRequest("The date provided is in an invalid format")
          },
          date => {
            setFeatureDate(Some(date.toLocalDate))
            logger.info(s"[FeatureSwitchController][setFeatureDate] - Time machine set to $date")
            Ok(s"Time machine set to: $date")
          }
        )
      }
    )
  }
}
