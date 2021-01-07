/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.domain.v2

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployment

case class Employer(payeReference: Option[EmpRef], name: Option[String], address: Option[Address])

object Employer {

  implicit val addressJsonFormat: OFormat[Address] = Json.format[Address]

  implicit val format: Format[Employer] = Format(
    (
      (JsPath \ "payeReference").readNullable[EmpRef] and
        (JsPath \ "name").readNullable[String] and
        (JsPath \ "address").readNullable[Address]
    )(Employer.apply _),
    (
      (JsPath \ "payeReference").writeNullable[EmpRef] and
        (JsPath \ "name").writeNullable[String] and
        (JsPath \ "address").writeNullable[Address]
    )(unlift(Employer.unapply))
  )

  def create(payeReference: Option[EmpRef], name: Option[String], address: Option[Address]): Option[Employer] =
    (payeReference, name, address) match {
      case (None, None, None) => None
      case _                  => Some(new Employer(payeReference, name, address))
    }

  def create(ifEmployment: IfEmployment): Option[Employer] = {
    val empRef: Option[EmpRef] = for {
      districtNumber <- ifEmployment.employer.flatMap(_.districtNumber)
      schemeRef      <- ifEmployment.employer.flatMap(_.schemeRef)
    } yield EmpRef(districtNumber, schemeRef)

    val name = ifEmployment.employer.flatMap(e => e.name)
    val address = ifEmployment.employer.flatMap(e => e.address)

    (empRef, name, address) match {
      case (None, None, None) => None
      case _                  => Employer.create(empRef, name, address)
    }
  }

}
