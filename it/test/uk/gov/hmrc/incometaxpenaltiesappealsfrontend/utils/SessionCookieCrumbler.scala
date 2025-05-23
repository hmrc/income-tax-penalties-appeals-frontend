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

package uk.gov.hmrc.incometaxpenaltiesappealsfrontend.utils

import play.api.libs.ws.{WSCookie, WSResponse}
import uk.gov.hmrc.crypto.{Crypted, SymmetricCryptoFactory}

object SessionCookieCrumbler {
  private val cookieKey = "gvBoGdgzqG1AarzF1LY0zQ=="

  private def crumbleCookie(cookie: WSCookie): Map[String, String] = {
    val crypted = Crypted(cookie.value)
    val decrypted = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).decrypt(crypted).value

    def decode(data: String): Map[String, String] = {
      // this part is hard coded because we are not certain at this time which hash algorithm is used by default
      val map = data.substring(41, data.length)

      val Regex = """(.*)=(.*)""".r
      map.split("&").collect {
        case Regex(k, v) => k -> v
      }.toMap
    }

    decode(decrypted)
  }

  def getSessionMap(wSResponse: WSResponse): Map[String, String] =
    wSResponse.cookie("mdtp").fold(Map.empty: Map[String, String])(data => crumbleCookie(data))

}
