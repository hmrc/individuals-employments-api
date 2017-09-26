/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.domain

import org.joda.time.LocalDate
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequency.PayFrequency
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment}

case class Employment(startDate: Option[LocalDate], endDate: Option[LocalDate], employer: Option[Employer], payFrequency: Option[PayFrequency])

object Employment {

  def from(desEmployment: DesEmployment): Option[Employment] = {

    def address(desAddress: Option[DesAddress]) = {
      desAddress map { address =>
        Address(address.line1, address.line2, address.line3, address.line4, address.line5, address.postalCode)
      }
    }

    val startDate = desEmployment.employmentStartDate
    val endDate = desEmployment.employmentLeavingDate
    val employer = Employer.create(desEmployment.employerPayeReference, desEmployment.employerName, address(desEmployment.employerAddress))
    val payFrequency = desEmployment.employmentPayFrequency.flatMap(PayFrequency.from)

    (startDate, endDate, employer, payFrequency) match {
      case (None, None, None, None) => None
      case _ => Some(Employment(startDate, endDate, employer, payFrequency))
    }
  }

}
