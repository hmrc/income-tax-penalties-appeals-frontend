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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.connectors.IncomeTaxSessionDataConnector
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.UpscanService
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.PrintAppealHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class ViewAppealDetailsController @Inject()(viewAppealDetails: ViewAppealDetailsView,
                                            val authActions: AuthActions,
                                            printAppealService: PrintAppealHelper,
                                            upscanService: UpscanService,
                                            incomeTaxSessionDataConnector: IncomeTaxSessionDataConnector,
                                            override val errorHandler: ErrorHandler,
                                            override val controllerComponents: MessagesControllerComponents,
                                           )(implicit ec: ExecutionContext, val appConfig: AppConfig) extends BaseUserAnswersController {

  def onPageLoad(): Action[AnyContent] = authActions.asMTDUserOldWithUserAnswers().async { implicit user =>

    val fUploadedFiles = upscanService.getAllReadyFiles(user.journeyId)

    for {
      uploadedFiles   <- fUploadedFiles
      summaryListRows =  printAppealService.constructPrintSummaryRows(uploadedFiles, user.nino)
    } yield Ok(viewAppealDetails(summaryListRows))
  }
}
