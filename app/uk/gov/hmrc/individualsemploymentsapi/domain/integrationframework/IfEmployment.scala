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
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath}

case class IfEmployment(
  employer: Option[IfEmployer],
  employment: Option[IfEmploymentDetail],
  payments: Option[Seq[IfPayment]])

object IfEmployment {
  implicit val employmentFormat: Format[IfEmployment] = Format(
    (
      (JsPath \ "employer").readNullable[IfEmployer] and
        (JsPath \ "employment").readNullable[IfEmploymentDetail] and
        (JsPath \ "payments").readNullable[Seq[IfPayment]]
    )(IfEmployment.apply _),
    (
      (JsPath \ "employer").writeNullable[IfEmployer] and
        (JsPath \ "employment").writeNullable[IfEmploymentDetail] and
        (JsPath \ "payments").writeNullable[Seq[IfPayment]]
    )(unlift(IfEmployment.unapply))
  )
}
