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

package unit.uk.gov.hmrc.individualsemploymentsapi.service.v2
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{ScopesHelper, ScopesService}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.UnitSpec
import unit.uk.gov.hmrc.individualsemploymentsapi.service.ScopesConfig

class ScopesHelperSpec
  extends UnitSpec
    with ScopesConfig
    with BeforeAndAfterEach {

  "Scopes helper" should {

    val scopesService = new ScopesService(mockConfig) {
      override lazy val apiConfig = mockApiConfig
    }

    val scopesHelper = new ScopesHelper(scopesService)

    "return replaced employerRef" in {
      val employerRef = "247%2FZT6767895A"
      val result =
        scopesHelper.getQueryStringWithParameterisedFilters(List(mockScope8), mockEndpoint4, employerRef)
      result shouldBe s"employer(employerAddress(line1,line2,line3),employerDistrictNumber,employerName,employerSchemeReference),payments&filter=employerRef eq '${employerRef}'"
    }

    "return correct query string" in {
      val result =
        scopesHelper.getQueryStringFor(List(mockScope2), mockEndpoint1)
      result shouldBe "employer(employerAddress(line1,line2,line3),employerDistrictNumber,employerName,employerSchemeReference),payments"
    }

    "generate Hal response" in {

      val mockData = Json.obj(
        "employments" -> Json.obj(
          "field1" -> Json.toJson("value1"),
          "field2" -> Json.toJson("value2")
        )
      )

      val response = scopesHelper.getHalResponse(
        endpoint = mockEndpoint1,
        scopes = List(mockScope1),
        data = Option(mockData)
      )

      response.links.links.size shouldBe 2

      response.links.links.exists(halLink =>
        halLink.rel == mockEndpoint1 && halLink.href == "/a/b/c?matchId=<matchId>{&fromDate,toDate}") shouldBe true

      response.links.links.exists(halLink =>
        halLink.rel == "self" && halLink.href == "/a/b/c?matchId=<matchId>{&fromDate,toDate}") shouldBe true

      val response2 = scopesHelper.getHalResponse(
        endpoint = mockEndpoint2,
        scopes = List(mockScope4, mockScope5),
        data = Option(mockData)
      )

      response2.links.links.size shouldBe 3

      response2.links.links.exists(halLink =>
        halLink.rel == mockEndpoint2 && halLink.href == "/a/b/d?matchId=<matchId>{&fromDate,toDate}") shouldBe true

      response2.links.links.exists(halLink =>
        halLink.rel == mockEndpoint3 && halLink.href == "/a/b/e?matchId=<matchId>{&fromDate,toDate}") shouldBe true

      response2.links.links.exists(halLink =>
        halLink.rel == "self" && halLink.href == "/a/b/d?matchId=<matchId>{&fromDate,toDate}") shouldBe true

    }
  }
}
