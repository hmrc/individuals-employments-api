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

import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.util.matching.Regex

case class IfId(nino: Option[String], trn: Option[String])

object IfId {

  val ninoPattern: Regex =
    "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\\s]?$".r

  val trnPattern: Regex = "^[0-9]{8}$".r

  implicit val idFormat: Format[IfId] = Format(
    (
      (JsPath \ "nino").readNullable[String](pattern(ninoPattern, "InvalidNino")) and
        (JsPath \ "trn").readNullable[String](pattern(trnPattern, "InvalidTrn"))
    )(IfId.apply _),
    (
      (JsPath \ "nino").writeNullable[String] and
        (JsPath \ "trn").writeNullable[String]
    )(unlift(IfId.unapply))
  )
}
