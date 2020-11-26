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

import play.api.libs.json.{JsError, Json}
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.{IfAddress, IfEmployer, IfEmployment, IfEmploymentDetail, IfEmployments, IfPayment}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.UnitSpec

class IfEmploymentSpec extends UnitSpec {
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

  "Employment" should {
    "write to JSON successfully" when {
      "employments is valid" in {
        val result = Json.toJson(employment).validate[IfEmployment]
        result.isSuccess shouldBe true
      }
    }

    "read from JSON successfully" in {
      val employmentJson: String =
        """
          |{
          |  "employer" : {
          |    "name" : "Name",
          |    "address" : {
          |      "line1" : "line1",
          |      "line2" : "line2",
          |      "line3" : "line3",
          |      "line4" : "line4",
          |      "line5" : "line5",
          |      "postcode" : "postcode"
          |    },
          |    "districtNumber" : "ABC",
          |    "schemeRef" : "ABC"
          |  },
          |  "employment" : {
          |    "startDate" : "2001-12-31",
          |    "endDate" : "2002-05-12",
          |    "payFrequency" : "W2",
          |    "payrollId" : "12341234",
          |    "address" : {
          |      "line1" : "line1",
          |      "line2" : "line2",
          |      "line3" : "line3",
          |      "line4" : "line4",
          |      "line5" : "line5",
          |      "postcode" : "postcode"
          |    }
          |  },
          |  "payments" : [ {
          |    "date" : "2001-12-31",
          |    "ytdTaxablePay" : 162081.23,
          |    "paidTaxablePay" : 112.75,
          |    "paidNonTaxOrNICPayment" : 123123.32,
          |    "week" : 52,
          |    "month" : 12
          |  } ]
          |}
          |""".stripMargin

      val result = Json.parse(employmentJson).validate[IfEmployment]
      result.isSuccess shouldBe true
    }
  }
}
