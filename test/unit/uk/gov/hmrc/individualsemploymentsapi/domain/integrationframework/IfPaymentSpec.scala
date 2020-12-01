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

package unit.uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework

import play.api.libs.json.{JsNumber, Json}
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfPayment
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfPayment.paymentAmountValidator
import unit.uk.gov.hmrc.individualsemploymentsapi.util.UnitSpec

class IfPaymentSpec extends UnitSpec {

  val payment = IfPayment(
    date = Some("2001-12-31"),
    ytdTaxablePay = Some(162081.23),
    paidTaxablePay = Some(112.75),
    paidNonTaxOrNICPayment = Some(123123.32),
    week = Some(52),
    month = Some(12)
  )

  "Payment" should {
    "write to JSON successfully" in {
      val result = Json.toJson(payment).validate[IfPayment]
      result.isSuccess shouldBe true
    }

    "read successfully" when {

      "JSON is complete" in {
        val paymentJson = """{
                            |  "date" : "2001-12-31",
                            |  "ytdTaxablePay" : 162081.23,
                            |  "paidTaxablePay" : 112.75,
                            |  "paidNonTaxOrNICPayment" : 123123.32,
                            |  "week" : 52,
                            |  "month" : 12
                            |}""".stripMargin

        val result = Json.parse(paymentJson).validate[IfPayment]
        result.isSuccess shouldBe true
        result.get shouldBe payment
      }

      "JSON is incomplete" in {
        val paymentJson = """{
                            |  "date" : "2001-12-31",
                            |  "ytdTaxablePay" : 162081.23,
                            |  "month" : 12
                            |}""".stripMargin

        val result = Json.parse(paymentJson).validate[IfPayment]
        result.isSuccess shouldBe true
      }
    }

    "fail validation" when {

      "week is below 1" in {
        val result = Json.toJson(payment.copy(week = Some(0))).validate[IfPayment]
        result.isError shouldBe true
      }

      "week is above 56" in {
        val result = Json.toJson(payment.copy(week = Some(57))).validate[IfPayment]
        result.isError shouldBe true
      }

      "month is below 1" in {
        val result = Json.toJson(payment.copy(month = Some(0))).validate[IfPayment]
        result.isError shouldBe true
      }

      "month is above 12" in {
        val result = Json.toJson(payment.copy(month = Some(13))).validate[IfPayment]
        result.isError shouldBe true
      }
    }
  }

  "paymentAmountValidator" should {

    "validate successfully" when {

      "value is larger than min value" in {
        val result = JsNumber(IfPayment.minValue + 1.0).validate[Double](paymentAmountValidator)
        result.isSuccess shouldBe true
      }

      "value is smaller than max value" in {
        val result = JsNumber(IfPayment.maxValue - 1.0).validate[Double](paymentAmountValidator)
        result.isSuccess shouldBe true
      }
    }

    "fail validation" when {

      "not a multiple of 0.01" in {
        val result = JsNumber(123.4312123123123).validate[Double](paymentAmountValidator)
        result.isError shouldBe true
      }

      "value is smaller than min value" in {
        val result = JsNumber(IfPayment.minValue - 1.0).validate[Double](paymentAmountValidator)
        result.isError shouldBe true
      }

      "value is larger than max value" in {
        val result = JsNumber(IfPayment.maxValue + 1.0).validate[Double](paymentAmountValidator)
        result.isError shouldBe true
      }
    }
  }
}
