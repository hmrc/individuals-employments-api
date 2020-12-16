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

case class IfEmployer(
  name: Option[String],
  address: Option[IfAddress],
  districtNumber: Option[String],
  schemeRef: Option[String])

object IfEmployer {
  implicit val employerFormat: Format[IfEmployer] = Format(
    (
      (JsPath \ "name").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "address").readNullable[IfAddress] and
        (JsPath \ "districtNumber").readNullable[String](minLength[String](0) keepAnd maxLength[String](3)) and
        (JsPath \ "schemeRef").readNullable[String](minLength[String](0) keepAnd maxLength[String](10))
    )(IfEmployer.apply _),
    (
      (JsPath \ "name").writeNullable[String] and
        (JsPath \ "address").writeNullable[IfAddress] and
        (JsPath \ "districtNumber").writeNullable[String] and
        (JsPath \ "schemeRef").writeNullable[String]
    )(unlift(IfEmployer.unapply))
  )
}
