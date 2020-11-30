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
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework._
import unit.uk.gov.hmrc.individualsemploymentsapi.util.UnitSpec

class IfEmployerSpec extends UnitSpec {

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

  "Employer" should {

    "write to JSON successfully" in {
      val result = Json.toJson(employer).validate[IfEmployer]
      result.isSuccess shouldBe true
    }

    "read successfully" when {

      "JSON is complete" in {

        val employerJson: String =
          """{
            |  "name" : "Name",
            |  "address" : {
            |    "line1" : "line1",
            |    "line2" : "line2",
            |    "line3" : "line3",
            |    "line4" : "line4",
            |    "line5" : "line5",
            |    "postcode" : "postcode"
            |  },
            |  "districtNumber" : "ABC",
            |  "schemeRef" : "ABC"
            |}""".stripMargin

        val result = Json.parse(employerJson).validate[IfEmployer]
        result.isSuccess shouldBe true
        result.get shouldBe employer
      }

      "JSON is incomplete" in {

        val employerJson: String =
          """{
            |  "name" : "Name",
            |  "address" : {
            |    "line1" : "line1",
            |    "line2" : "line2",
            |    "line3" : "line3",
            |    "postcode" : "postcode"
            |  }
            |}""".stripMargin

        val result = Json.parse(employerJson).validate[IfEmployer]
        result.isSuccess shouldBe true
      }
    }
  }
}
