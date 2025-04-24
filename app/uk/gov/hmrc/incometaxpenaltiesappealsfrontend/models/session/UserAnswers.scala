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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.session

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.incometaxpenaltiesappealsfrontend.pages.Page
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class UserAnswers(
                        journeyId: String,
                        data: JsObject = Json.obj(),
                        lastUpdated: Instant = Instant.now
                      ) {

  def getAnswerForKey[A](key: String)(implicit reads: Reads[A]): Option[A] =
    (data \ key).validate.fold(_ => None, Some(_))

  def getAnswer[A](page: Page[A])(implicit reads: Reads[A]): Option[A] =
    getAnswerForKey(page.pageKey)

  def setAnswerForKey[A](key: String, value: A)(implicit writes: Writes[A]): UserAnswers =
    UserAnswers(journeyId, data ++ Json.obj(key -> value))

  def setAnswer[A](page: Page[A], value: A)(implicit writes: Writes[A]): UserAnswers =
    setAnswerForKey(page.pageKey, value)

  def removeAnswer[A](page: Page[A]): UserAnswers =
    UserAnswers(journeyId, data - page.pageKey)

  def removeAppealReasonsData(): UserAnswers = {
    val keysToRemove = data.keys.filter(_.startsWith("appealReasons."))
    keysToRemove.foreach(key => data - key)
    UserAnswers(journeyId, data)
  }

//  def removeAppealReasonsData(): UserAnswers = {
//    UserAnswers(journeyId, data - "appealReasons")
//  }
}

object UserAnswers {
  val reads: Reads[UserAnswers] = {
    (__ \ "journeyId").read[String].and((__ \ "data").read[JsObject]).and(
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat))(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {
    (__ \ "journeyId").write[String].and((__ \ "data").write[JsObject]).and(
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat))(unlift(UserAnswers.unapply))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)

}
