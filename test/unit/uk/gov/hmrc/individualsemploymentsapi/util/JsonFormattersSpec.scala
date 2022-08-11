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

package unit.uk.gov.hmrc.individualsemploymentsapi.util

import java.util.UUID

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.ErrorInvalidRequest
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

class JsonFormattersSpec extends AnyWordSpec with Matchers {
  "Error invalid request format" should {
    "read correctly" in {
      val result = Json.fromJson[ErrorInvalidRequest](Json.parse(
        """
          |{
          |    "code":"INVALID_REQUEST",
          |    "message" : "test"
          |}
          |""".stripMargin))

      result.get shouldBe ErrorInvalidRequest("test")
    }
    "write correctly to Json" in {
      val error = ErrorInvalidRequest("test")
      val result = Json.toJson(error)

      result shouldBe Json.parse(
        """
          |{
          |    "code":"INVALID_REQUEST",
          |    "message" : "test"
          |}
          |""".stripMargin)
    }
  }

  "UUID formatter" should {
    "read correctly" in {
      val response = Json.fromJson[UUID](JsString("98ee1988-2101-4922-a3c7-d2ca5f0a394e"))
      response.get shouldBe UUID.fromString("98ee1988-2101-4922-a3c7-d2ca5f0a394e")
    }
    "write correctly" in {
      val result = Json.toJson(UUID.fromString("98ee1988-2101-4922-a3c7-d2ca5f0a394e"))
      result shouldBe JsString("98ee1988-2101-4922-a3c7-d2ca5f0a394e")
    }
  }
}
