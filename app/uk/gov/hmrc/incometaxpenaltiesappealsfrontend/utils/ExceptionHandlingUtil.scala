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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils.PagerDutyHelper.PagerDutyKeys

import scala.concurrent.{ExecutionContext, Future}

trait ExceptionHandlingUtil {

  lazy val className: String = this.getClass.getSimpleName.replace("$", "")

  def withExceptionHandling[A](methodName: String,
                               identifiers: Map[String, String] = Map(),
                               pagerDutyTriggerKey: Option[PagerDutyKeys.Value] = None)(f: => Future[A])(implicit ec: ExecutionContext): Future[A] =
    f.recover {
      case e: Throwable =>
        val formattedIdentifiers = identifiers.map { case (key, value) => s"$key: $value" }.mkString(", ")
        val errorMessage = s"[$className][$methodName] An exception of type ${e.getClass.getSimpleName} occurred for $formattedIdentifiers"
        logger.debug(errorMessage + ", exception message: " + e.getMessage)
        pagerDutyTriggerKey.fold {
          logger.warn(errorMessage)
        }{ pdTriggerKey =>
          logger.error(s"[$pdTriggerKey]" + errorMessage)
        }
        throw e
    }
}
