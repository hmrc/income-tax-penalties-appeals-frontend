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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.upscan

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

import scala.util.Try

object UploadStatusEnum extends Enumeration {
  val WAITING: UploadStatusEnum.Value = Value
  val READY: UploadStatusEnum.Value = Value
  val FAILED: UploadStatusEnum.Value = Value

  implicit def format: Format[UploadStatusEnum.Value] = new Format[UploadStatusEnum.Value] {
    override def writes(o: UploadStatusEnum.Value): JsValue = JsString(o.toString.toUpperCase)
    override def reads(json: JsValue): JsResult[UploadStatusEnum.Value] =
      Try(UploadStatusEnum.withName(json.as[String])).toOption match {
        case Some(value) => JsSuccess(value)
        case error => JsError(s"Invalid value '$error' for UploadStatusEnum")
      }
  }
}
