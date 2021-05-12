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

import org.joda.time.{Interval, LocalDate}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath, Reads}

import scala.util.matching.Regex
//import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfPayment.{datePattern, paymentAmountValidator}

case class Payment(
  paymentDate: LocalDate,
  taxablePayment: Double) {

  def isPaidWithin(interval: Interval): Boolean = interval.contains(paymentDate.toDateTimeAtStartOfDay)

}

object Payment {

  def datePattern: Regex =
    ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
      "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
      "(0[1-9]|1[0-9]|2[0-8])))$").r

  val minValue = -9999999999.99
  val maxValue = 9999999999.99

  def isMultipleOfPointZeroOne(value: Double): Boolean = (BigDecimal(value) * 100.0) % 1 == 0

  def isInRange(value: Double): Boolean = value > minValue && value < maxValue

  def paymentAmountValidator: Reads[Double] =
    verifying[Double](value => isInRange(value) && isMultipleOfPointZeroOne(value))

  implicit val paymentFormat: Format[Payment] = Format(
    (
      (JsPath \ "date").readNullable[String](pattern(datePattern, "Date format is incorrect")) and
        (JsPath \ "paidTaxablePay").readNullable[Double](paymentAmountValidator)
      )(Payment.apply _),
    (
      (JsPath \ "date").writeNullable[String] and
        (JsPath \ "paidTaxablePay").writeNullable[Double]
      )(unlift(Payment.unapply))
  )
}