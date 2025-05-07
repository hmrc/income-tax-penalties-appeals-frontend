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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.views.helpers

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.controllers.auth.models.CurrentUserRequestWithAnswers
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse.Other
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.{AgentClientEnum, ReasonableExcuse}
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.{WhatCausedYouToMissDeadlinePage, WhoPlannedToSubmitPage}

trait WhenDidEventHappenHelper {

  def messageKeyPrefix(reason: ReasonableExcuse, isLPP: Boolean)(implicit user: CurrentUserRequestWithAnswers[_]): String =
    if(reason != Other) s"whenDidEventHappen.$reason" else {
      if(user.isAgent && WhoPlannedToSubmitPage.value.contains(AgentClientEnum.agent) && WhatCausedYouToMissDeadlinePage.value.contains(AgentClientEnum.client)) {
        "agent.whenDidEventHappen.other.clientInformation"
      } else {
        s"${if(user.isAgent) "agent." else ""}whenDidEventHappen.other${if(isLPP) ".lpp" else ".lsp"}"
      }
    }
}

object WhenDidEventHappenHelper extends WhenDidEventHappenHelper



