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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models

import play.api.libs.json.Reads
import play.api.mvc.{Request, WrappedRequest}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.UserAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{Page, ReasonableExcusePage}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger

case class CurrentUserRequestWithAnswers[A](mtdItId: String,
                                            arn: Option[String] = None,
                                            override val navBar: Option[Html] = None,
                                            userAnswers: UserAnswers)(implicit val request: Request[A]) extends WrappedRequest[A](request) with RequestWithNavBar {
  val isAgent: Boolean = arn.isDefined
  val journeyId: String = userAnswers.journeyId

  def getMandatoryAnswer[T](page: Page[T])(implicit reads: Reads[T]): T =
    userAnswers.getAnswer(page) match {
      case Some(value) => value
      case None =>
        logger.error(s"[AppealSubmission][mandatoryAnswer] Missing mandatory answer for page key ${page.key}, mtditid: $mtdItId")
        throw new NoSuchFieldError(s"Missing mandatory answer for page key ${page.key}, mtditid: $mtdItId")
    }

  def lateAppealDays()(implicit appConfig: AppConfig): Int =
    if(userAnswers.getAnswer(ReasonableExcusePage).exists(_.contains("bereavement"))) appConfig.bereavementLateDays else appConfig.lateDays
}

object CurrentUserRequestWithAnswers {
  def apply[A](userAnswers: UserAnswers)(implicit userRequest: CurrentUserRequest[A]): CurrentUserRequestWithAnswers[A] =
    CurrentUserRequestWithAnswers(userRequest.mtdItId, userRequest.arn, userRequest.navBar, userAnswers)
}
