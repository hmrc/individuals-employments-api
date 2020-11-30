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
import play.api.libs.json.{Format, JsPath, Reads}

import scala.util.matching.Regex

case class IfPayment(
  date: Option[String],
  ytdTaxablePay: Option[Double],
  paidTaxablePay: Option[Double],
  paidNonTaxOrNICPayment: Option[Double],
  week: Option[Int],
  month: Option[Int])

object IfPayment {

  def datePattern: Regex =
    ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
      "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
      "(0[1-9]|1[0-9]|2[0-8])))$").r

  val payFrequencyPattern: Regex =
    "^(W1|W2|W4|M1|M3|M6|MA|IO|IR)$".r

  val minValue = -9999999999.99
  val maxValue = 9999999999.99

  def isMultipleOfPointZeroOne(value: Double): Boolean = (BigDecimal(value) * 100.0) % 1 == 0

  def isInRange(value: Double): Boolean = value > minValue && value < maxValue

  def paymentAmountValidator: Reads[Double] =
    verifying[Double](value => isInRange(value) && isMultipleOfPointZeroOne(value))

  implicit val paymentFormat: Format[IfPayment] = Format(
    (
      (JsPath \ "date").readNullable[String](pattern(datePattern, "Date format is incorrect")) and
        (JsPath \ "ytdTaxablePay").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "paidTaxablePay").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "paidNonTaxOrNICPayment").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "week").readNullable[Int](min(1) keepAnd max(56)) and
        (JsPath \ "month").readNullable[Int](min(1) keepAnd max(12))
    )(IfPayment.apply _),
    (
      (JsPath \ "date").writeNullable[String] and
        (JsPath \ "ytdTaxablePay").writeNullable[Double] and
        (JsPath \ "paidTaxablePay").writeNullable[Double] and
        (JsPath \ "paidNonTaxOrNICPayment").writeNullable[Double] and
        (JsPath \ "week").writeNullable[Int] and
        (JsPath \ "month").writeNullable[Int]
    )(unlift(IfPayment.unapply))
  )
}
