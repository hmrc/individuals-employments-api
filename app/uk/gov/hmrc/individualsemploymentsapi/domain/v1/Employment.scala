/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.domain.v1

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import uk.gov.hmrc.individualsemploymentsapi.domain.ip.IpEmployment
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.PayFrequency.PayFrequency

case class Employment(
  startDate: Option[LocalDate],
  endDate: Option[LocalDate],
  employer: Option[Employer],
  payFrequency: Option[PayFrequency],
  employeeAddress: Option[Address],
  payrollId: Option[String]
)

object Employment {
  implicit val employmentJsonFormat: OFormat[Employment] = Json.format[Employment]

  def from(ipEmployment: IpEmployment): Option[Employment] = {
    val startDate = ipEmployment.employmentStartDate
    val endDate = ipEmployment.employmentLeavingDate
    val employer = Employer.create(
      ipEmployment.employerPayeReference,
      ipEmployment.employerName,
      ipEmployment.employerAddress.map(Address.from)
    )
    val payFrequency = ipEmployment.employmentPayFrequency.flatMap(PayFrequency.from)
    val address = ipEmployment.employeeAddress.map(Address.from)
    val payrollId = ipEmployment.payrollId

    (startDate, endDate, employer, payFrequency) match {
      case (None, None, None, None) => None
      case _                        => Some(Employment(startDate, endDate, employer, payFrequency, address, payrollId))
    }
  }
}
