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

package uk.gov.hmrc.individualsemploymentsapi.domain.v2

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath}
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfPayment

case class Payment(paymentDate: Option[LocalDate], taxablePayment: Option[Double]) {}

object Payment {

  implicit val paymentFormat: Format[Payment] = Format(
    (
      (JsPath \ "date").readNullable[LocalDate] and
        (JsPath \ "paidTaxablePay").readNullable[Double]
    )(Payment.apply _),
    (
      (JsPath \ "date").writeNullable[LocalDate] and
        (JsPath \ "paidTaxablePay").writeNullable[Double]
    )(unlift(Payment.unapply))
  )

  def create(ifPayment: IfPayment): Option[Payment] = {
    val paymentDate = ifPayment.date.map(s => LocalDate.parse(s))
    val taxeablePayment = ifPayment.paidTaxablePay

    (paymentDate, taxeablePayment) match {
      case (None, None) => None
      case _            => Some(new Payment(paymentDate, taxeablePayment))
    }
  }
}
