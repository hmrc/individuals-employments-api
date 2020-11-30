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

import play.api.libs.json.Json
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmploymentDetail
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfAddress
import unit.uk.gov.hmrc.individualsemploymentsapi.util.UnitSpec

class IfEmploymentDetailSpec extends UnitSpec {

  val address = IfAddress(
    Some("line1"),
    Some("line2"),
    Some("line3"),
    Some("line4"),
    Some("line5"),
    Some("postcode")
  )

  val employmentDetail = IfEmploymentDetail(
    startDate = Some("2001-12-31"),
    endDate = Some("2002-05-12"),
    payFrequency = Some("W2"),
    payrollId = Some("12341234"),
    address = Some(address)
  )

  "EmploymentDetail" should {

    "write to JSON successfully" in {
      val result = Json.toJson(employmentDetail).validate[IfEmploymentDetail]
      result.isSuccess shouldBe true
    }

    "read successfully" when {

      "JSON is complete" in {

        val employmentDetailJson = """{
                                     |  "startDate" : "2001-12-31",
                                     |  "endDate" : "2002-05-12",
                                     |  "payFrequency" : "W2",
                                     |  "payrollId" : "12341234",
                                     |  "address" : {
                                     |    "line1" : "line1",
                                     |    "line2" : "line2",
                                     |    "line3" : "line3",
                                     |    "line4" : "line4",
                                     |    "line5" : "line5",
                                     |    "postcode" : "postcode"
                                     |  }
                                     |}""".stripMargin

        val result = Json.parse(employmentDetailJson).validate[IfEmploymentDetail]
        result.isSuccess shouldBe true
        result.get shouldBe employmentDetail
      }

      "JSON is incomplete" in {

        val employmentDetailJson = """{
                                     |  "startDate" : "2001-12-31",
                                     |  "endDate" : "2002-05-12",
                                     |  "payFrequency" : "W2"
                                     |}""".stripMargin

        val result = Json.parse(employmentDetailJson).validate[IfEmploymentDetail]
        result.isSuccess shouldBe true
      }
    }

    "fail validation" when {

      "payFrequency is not one of: W1, W2, W4, M1, M3, M6, MA, IO, IR" in {
        val result = Json.toJson(employmentDetail.copy(payFrequency = Some("XX"))).validate[IfEmploymentDetail]
        result.isError shouldBe true
      }

      "start date is invalid" in {
        val result = Json.toJson(employmentDetail.copy(startDate = Some("2020-12-50"))).validate[IfEmploymentDetail]
        result.isError shouldBe true
      }

      "end date is invalid" in {
        val result = Json.toJson(employmentDetail.copy(endDate = Some("2020-12-50"))).validate[IfEmploymentDetail]
        result.isError shouldBe true
      }
    }
  }
}
