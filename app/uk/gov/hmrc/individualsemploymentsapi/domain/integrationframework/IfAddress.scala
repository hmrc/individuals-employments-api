/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{maxLength, minLength}
import play.api.libs.json.{Format, JsPath}

case class IfAddress(
  line1: Option[String],
  line2: Option[String],
  line3: Option[String],
  line4: Option[String],
  line5: Option[String] = None,
  postcode: Option[String]
)

object IfAddress {
  implicit val addressFormat: Format[IfAddress] = Format(
    (
      (JsPath \ "line1").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line2").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line3").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line4").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line5").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "postcode").readNullable[String](minLength[String](0) keepAnd maxLength[String](10))
    )(IfAddress.apply _),
    (
      (JsPath \ "line1").writeNullable[String] and
        (JsPath \ "line2").writeNullable[String] and
        (JsPath \ "line3").writeNullable[String] and
        (JsPath \ "line4").writeNullable[String] and
        (JsPath \ "line5").writeNullable[String] and
        (JsPath \ "postcode").writeNullable[String]
    )(unlift(IfAddress.unapply))
  )
}
