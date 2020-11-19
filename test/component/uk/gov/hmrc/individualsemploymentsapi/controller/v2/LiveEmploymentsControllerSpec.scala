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

package component.uk.gov.hmrc.individualsemploymentsapi.controller.v2

import java.util.UUID

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.{AuthStub, BaseSpec, IndividualsMatchingApiStub}
import play.api.libs.json.Json
import play.api.test.Helpers._
import scalaj.http.Http

class LiveEmploymentsControllerSpec extends BaseSpec {

  private val matchId = UUID.randomUUID().toString
  private val nino = "AB123456C"
  private val employmentsScope = "read:individuals-employments"
  private val payeEmploymentsScope = "read:individuals-employments-paye"

  feature("Root (hateoas) entry point is accessible") {

    scenario("invalid token") {
      Given("an invalid token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, employmentsScope, retrieveAll = true)

      When("the root entry point to the API is invoked")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    scenario("missing match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope, retrieveAll = true)

      When("the root entry point to the API is invoked with a missing match id")
      val response = invokeEndpoint(serviceUrl)

      Then("the response status should be 400 (bad request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "matchId is required"
      )
    }

    scenario("malformed match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope, retrieveAll = true)

      When("the root entry point to the API is invoked with a malformed match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=malformed-match-id-value")

      Then("the response status should be 400 (bad request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "matchId format is invalid"
      )
    }

    scenario("invalid match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope, retrieveAll = true)

      When("the root entry point to the API is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 404 (not found)")
      response.code shouldBe INTERNAL_SERVER_ERROR
      response.body shouldBe "{\"statusCode\":500,\"message\":\"NOT_IMPLEMENTED\"}"
    }

    scenario("valid request to the live root endpoint implementation") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope, retrieveAll = true)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId, nino)

      When("the root entry point to the API is invoked with a valid match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 500")
      response.code shouldBe INTERNAL_SERVER_ERROR
      response.body shouldBe "{\"statusCode\":500,\"message\":\"NOT_IMPLEMENTED\"}"
    }
  }

  feature("Paye endpoint") {

    val fromDate = "2017-01-01"
    val toDate = "2017-09-25"

    scenario("invalid token") {
      Given("an invalid token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope, retrieveAll = true)

      When("the paye endpoint is invoked")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    scenario("missing match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope, retrieveAll = true)

      When("the paye endpoint is invoked with a missing match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 400 (bad request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "matchId is required"
      )
    }

    scenario("malformed match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope, retrieveAll = true)

      When("the paye endpoint is invoked with a malformed match id")
      val response =
        invokeEndpoint(s"$serviceUrl/paye?matchId=malformed-match-id-value&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 400 (bad request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "matchId format is invalid"
      )
    }

    scenario("invalid match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope, retrieveAll = true)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 500")
      response.code shouldBe INTERNAL_SERVER_ERROR
      response.body shouldBe "{\"statusCode\":500,\"message\":\"NOT_IMPLEMENTED\"}"
    }

    scenario("valid request to the live paye endpoint implementation") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope, retrieveAll = true)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId, nino)

      And("IF will return employments for the NINO")
      // TODO: Fill in

      When("the paye endpoint is invoked with a valid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 500")
      response.code shouldBe INTERNAL_SERVER_ERROR
      response.body shouldBe "{\"statusCode\":500,\"message\":\"NOT_IMPLEMENTED\"}"
    }

    scenario("the IF rate limit is exceeded") {
      val matchId = UUID.randomUUID().toString
      val nino = "AA112233B"

      Given("a valid privileged Auth Bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope, retrieveAll = true)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId, nino)

      And("IF will return an error due to rate limiting")
      // TODO: Fill in

      When("the PAYE endpoint is invoked with a valid match ID")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("The response status is 500")
      response.code shouldBe INTERNAL_SERVER_ERROR
      response.body shouldBe "{\"statusCode\":500,\"message\":\"NOT_IMPLEMENTED\"}"
    }
  }

  private def invokeEndpoint(endpoint: String) =
    Http(endpoint)
      .timeout(10000, 10000)
      .headers(requestHeaders(acceptHeaderVP2))
      .asString
}