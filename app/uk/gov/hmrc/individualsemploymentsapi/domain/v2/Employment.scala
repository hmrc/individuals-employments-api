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

package uk.gov.hmrc.individualsemploymentsapi.domain.v2

import play.api.libs.json.{Format, JsPath}
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployment
import play.api.libs.functional.syntax._

case class Employment(employer: Option[Employer], employment: Option[EmploymentDetail])

object Employment {

  implicit val format: Format[Employment] = Format(
    (
      (JsPath \ "employer").readNullable[Employer] and
        (JsPath \ "employment").readNullable[EmploymentDetail]
    )(Employment.apply _),
    (
      (JsPath \ "employer").writeNullable[Employer] and
        (JsPath \ "employment").writeNullable[EmploymentDetail]
    )(unlift(Employment.unapply))
  )

  def create(employer: Option[Employer], employment: Option[EmploymentDetail]): Option[Employment] =
    (employer, employment) match {
      case (None, None) => None
      case _            => Some(new Employment(employer, employment))
    }

  def create(ifEmployment: IfEmployment): Option[Employment] = {

    val employer: Option[Employer] = Employer.create(ifEmployment)

    val employmentDetail: Option[EmploymentDetail] = EmploymentDetail.create(ifEmployment)

    (employer, employmentDetail) match {
      case (None, None) => None
      case _            => Some(new Employment(employer, employmentDetail))
    }
  }
}
