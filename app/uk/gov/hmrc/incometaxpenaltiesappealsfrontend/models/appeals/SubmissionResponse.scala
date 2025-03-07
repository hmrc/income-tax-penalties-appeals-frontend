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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.appeals


sealed trait SubmissionSuccessResponse

case class SuccessfulAppeal(success: AppealSubmissionResponseModel) extends SubmissionSuccessResponse

case class SuccessfulMultiAppeal(lpp1Success: AppealSubmissionResponseModel,
                                 lpp2Success: AppealSubmissionResponseModel) extends SubmissionSuccessResponse


sealed trait SubmissionErrorResponse

case object AppealFailed extends SubmissionErrorResponse

case class MultiAppealFailedLPP1(lpp2Success: AppealSubmissionResponseModel) extends SubmissionErrorResponse

case class MultiAppealFailedLPP2(lpp1Success: AppealSubmissionResponseModel) extends SubmissionErrorResponse

case object MultiAppealFailedBoth extends SubmissionErrorResponse

case class UnexpectedFailedFuture(e: Throwable) extends SubmissionErrorResponse
