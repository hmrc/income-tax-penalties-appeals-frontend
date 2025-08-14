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
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals.AppealSubmission
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.audit.ViewStatusAuditModel
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.ReasonableExcusePage
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.services.{AuditService, UpscanService}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers.PrintAppealHelper
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.html._

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class ViewAppealDetailsController @Inject()(viewAppealDetails: ViewAppealDetailsView,
                                            val authActions: AuthActions,
                                            printAppealService: PrintAppealHelper,
                                            upscanService: UpscanService,
                                            auditService: AuditService,
                                            override val errorHandler: ErrorHandler,
                                            override val controllerComponents: MessagesControllerComponents,
                                           )(implicit ec: ExecutionContext, val appConfig: AppConfig, timeMachine: TimeMachine) extends BaseUserAnswersController {

  def onPageLoad(isAgent: Boolean, is2ndStageAppeal: Boolean): Action[AnyContent] = authActions.asMTDUserWithUserAnswers(isAgent).async { implicit user =>
    withAnswer(ReasonableExcusePage) { reasonableExcuse =>

      val fUploadedFiles = upscanService.getAllReadyFiles(user.journeyId)

      for {
        uploadedFiles <- fUploadedFiles
        summaryListRows = printAppealService.constructPrintSummaryRows(uploadedFiles, user.nino)
      } yield {

        val viewStatusAuditModel = ViewStatusAuditModel(
          penaltyNumber = user.penaltyNumber,
          penaltyType = user.penaltyData.appealData.`type`,
          appealSubmission = AppealSubmission.constructModelBasedOnReasonableExcuse(
            reasonableExcuse = reasonableExcuse,
            uploadedFiles = Option.when(uploadedFiles.nonEmpty)(uploadedFiles)
          ),
          penaltyData = user.penaltyData
        )

        logger.info(s" Sending audit for ViewStatusAuditModel: $viewStatusAuditModel")
        auditService.audit(viewStatusAuditModel)
        Ok(viewAppealDetails(summaryListRows))
      }

    }
  }
}
