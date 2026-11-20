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

package uk.gov.hmrc.individualsemploymentsapi.domain.ip

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.Payment

case class IpEmployments(employments: Seq[IpEmployment])

object IpEmployments {
  implicit val ipEmploymentsJsonFormat: Format[IpEmployments] = Json.format[IpEmployments]

  def toPayments(ipEmployment: IpEmployment): Seq[Payment] =
    ipEmployment.payments map { payment =>
      Payment(
        payment.totalPayInPeriod,
        payment.paymentDate,
        ipEmployment.employerPayeReference,
        payment.monthPayNumber,
        payment.weekPayNumber
      )
    }
}
