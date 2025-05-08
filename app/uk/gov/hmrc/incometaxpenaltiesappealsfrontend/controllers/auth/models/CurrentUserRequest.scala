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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models

import play.api.mvc.{Request, WrappedRequest}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.RequestWithNavBar
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session.SessionData

abstract class CurrentUserRequest[A](request: Request[A]) extends WrappedRequest[A](request) with RequestWithNavBar {
  val mtdItId: String
  val nino: String
  val navBar: Option[Html]
  val arn: Option[String]

  val isAgent: Boolean
}

case class AuthorisedAndEnrolledIndividual[A](mtdItId: String,
                                              nino: String,
                                              navBar: Option[Html])(implicit request: Request[A]) extends CurrentUserRequest[A](request) {
  override val isAgent: Boolean = false
  override val arn: Option[String] = None
  def addNavBar(content: Html): CurrentUserRequest[A] = copy(navBar = Some(content))
}

case class AuthorisedAndEnrolledAgent[A](sessionData: SessionData,
                                         arn: Option[String]
                                         )(implicit request: Request[A]) extends CurrentUserRequest[A](request) {
  override val mtdItId: String = sessionData.mtditid
  override val nino: String = sessionData.nino
  override val isAgent: Boolean = true
  override val navBar: Option[Html] = None
}

case class AuthorisedUserRequest[A](affinityGroup: AffinityGroup,
                                    arn: Option[String] = None)
                                   (implicit request: Request[A]) extends WrappedRequest[A](request)




