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

package utils

import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.{IfAddress, IfEmployer, IfEmployment, IfEmploymentDetail, IfPayment}

trait EmploymentsHelper {

  val address = IfAddress(
    Some("line1"),
    Some("line2"),
    Some("line3"),
    Some("line4"),
    Some("line5"),
    Some("postcode")
  )

  val employer = IfEmployer(
    name = Some("Name"),
    address = Some(address),
    districtNumber = Some("ABC"),
    schemeRef = Some("ABC")
  )

  val employmentDetail = IfEmploymentDetail(
    startDate = Some("2001-12-31"),
    endDate = Some("2002-05-12"),
    payFrequency = Some("W2"),
    payrollId = Some("12341234"),
    address = Some(address))

  val payment = IfPayment(
    date = Some("2001-12-31"),
    ytdTaxablePay = Some(162081.23),
    paidTaxablePay = Some(112.75),
    paidNonTaxOrNICPayment = Some(123123.32),
    week = Some(52),
    month = Some(12)
  )

  val employment = IfEmployment(
    employer = Some(employer),
    employment = Some(employmentDetail),
    payments = Some(Seq(payment))
  )

  def createValidEmployment() =
    IfEmployment(
      employer = Some(employer),
      employment = Some(employmentDetail),
      payments = Some(Seq(payment))
    )

}
