/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.domain.des

import uk.gov.hmrc.individualsemploymentsapi.domain.v1.Payment

case class DesEmployments(employments: Seq[DesEmployment])

object DesEmployments {
  def toPayments(desEmployment: DesEmployment): Seq[Payment] =
    desEmployment.payments map { payment =>
      Payment(
        payment.totalPayInPeriod,
        payment.paymentDate,
        desEmployment.employerPayeReference,
        payment.monthPayNumber,
        payment.weekPayNumber)
    }
}
