/*
 * Copyright 2017 HM Revenue & Customs
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

package component.uk.gov.hmrc.individualsemploymentsapi

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.{AuthStub, _}
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.Json.parse
import play.api.test.Helpers._

import scalaj.http.{Http, HttpResponse}

class VersioningSpec extends BaseSpec {

  private val sandboxMatchEndpointWithSandboxMatchId = "/sandbox/match?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"

  feature("Versioning") {

    scenario("Requests without an accept header default to version 1") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken)

      When(s"A request to $sandboxMatchEndpointWithSandboxMatchId is made without an accept header")
      val response = invokeWithHeaders(sandboxMatchEndpointWithSandboxMatchId, AUTHORIZATION -> authToken)

      Then("The response status should be 404")
      response.code shouldBe NOT_FOUND
    }

    scenario("Requests with an accept header version 1") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken)

      When(s"A request to $sandboxMatchEndpointWithSandboxMatchId is made with an accept header for version 1")
      val response = invokeWithHeaders(sandboxMatchEndpointWithSandboxMatchId, AUTHORIZATION -> authToken, acceptHeaderV1)

      Then("The response status should be 404")
      response.code shouldBe NOT_FOUND
    }

    scenario("Requests with an accept header version P1") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken)

      When(s"A request to $sandboxMatchEndpointWithSandboxMatchId is made with an accept header for version P1")
      val response = invokeWithHeaders(sandboxMatchEndpointWithSandboxMatchId, AUTHORIZATION -> authToken, acceptHeaderVP1)

      Then("The response status should be 200")
      response.code shouldBe OK

      Then("And the response body should be for api version P1")
      parse(response.body) shouldBe parse(
        """
          {
            "_links":{
              "paye":{
                "href":"/individuals/employments/paye/match?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430{&fromDate,toDate}",
                "title":"View individual's employments"
              },
              "self":{
                "href":"/individuals/employments/match?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"
              }
            }
          }
        """)
    }
  }

  private def invokeWithHeaders(urlPath: String, headers: (String, String)*): HttpResponse[String] =
    Http(s"$serviceUrl$urlPath").headers(headers).asString
}
