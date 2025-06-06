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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.actions

import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.{CurrentUserRequest, CurrentUserRequestWithAnswers}

@Singleton
class AuthActions @Inject()(val authoriseAndRetrieveMTDIndividual: AuthoriseAndRetrieveMTDIndividual,
                            val authoriseAndRetrieveAgent: AuthoriseAndRetrieveAgent,
                            val withAgentClientData: RetrieveClientData,
                            val authoriseAndRetrieveMtdAgent: AuthoriseAndRetrieveMtdAgent,
                            val navBarRetrievalAction: NavBarRetrievalAction,
                            val userAnswersAction: UserAnswersAction,
                            override val authConnector: AuthConnector) extends AuthorisedFunctions {

  // $COVERAGE-OFF$
  def asMTDIndividual(): ActionBuilder[CurrentUserRequest, AnyContent] = {
    authoriseAndRetrieveMTDIndividual andThen navBarRetrievalAction
  }

  def asMTDAgent(): ActionBuilder[CurrentUserRequest, AnyContent] = {
    authoriseAndRetrieveAgent andThen withAgentClientData andThen authoriseAndRetrieveMtdAgent
  }

  def asMTDUser(isAgent: Boolean): ActionBuilder[CurrentUserRequest, AnyContent] = {
    if(isAgent) asMTDAgent() else asMTDIndividual()
  }

  def asMTDAgentWithUserAnswers(): ActionBuilder[CurrentUserRequestWithAnswers, AnyContent] = {
    asMTDAgent() andThen userAnswersAction
  }

  def asMTDUserWithUserAnswers(isAgent: Boolean): ActionBuilder[CurrentUserRequestWithAnswers, AnyContent] = {
    asMTDUser(isAgent) andThen userAnswersAction
  }
  // $COVERAGE-ON$


}
