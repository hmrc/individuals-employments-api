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

package component.uk.gov.hmrc.individualsemploymentsapi.controller.v2

import java.util.UUID

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.{AuthStub, BaseSpec, IfStub, IndividualsMatchingApiStub}
import play.api.libs.json.Json
import play.api.test.Helpers._
import scalaj.http.Http
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.{IfEmployer, IfEmployment, IfEmployments}

class LiveEmploymentsControllerSpec extends BaseSpec {

  private val matchId = UUID.randomUUID().toString
  private val nino = "AB123456C"
  val fromDate = "2017-01-01"
  val toDate = "2017-09-25"

  private val allScopes = List(
    "read:individuals-employments-hmcts-c2",
    "read:individuals-employments-hmcts-c3",
    "read:individuals-employments-hmcts-c4",
    "read:individuals-employments-ho-ecp-application",
    "read:individuals-employments-ho-ecp-compliance",
    "read:individuals-employments-ho-rp2-application",
    "read:individuals-employments-ho-rp2-compliance",
    "read:individuals-employments-laa-c1",
    "read:individuals-employments-laa-c2",
    "read:individuals-employments-laa-c3",
    "read:individuals-employments-laa-c4",
    "read:individuals-employments-lsani-c1",
    "read:individuals-employments-lsani-c3",
    "read:individuals-employments-nictsejo-c4")

  val validData = IfEmployments(
    Seq(
      IfEmployment(
        employer = Some(
          IfEmployer(
            name = Some("employer name"),
            None,
            None,
            None
          )
        ),
        None,
        None,
        None
      )))

  feature("Root (hateoas) entry point is accessible") {

    testAuthorisation("", allScopes)

    scenario("invalid token") {
      Given("an invalid token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, allScopes)

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
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

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
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

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
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the root entry point to the API is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 404 (not found)")
      response.code shouldBe NOT_FOUND
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    scenario("valid request to the live root endpoint implementation") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId, nino)

      When("the root entry point to the API is invoked with a valid match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 200 (ok)")
      response.code shouldBe OK
      Json.parse(response.body) shouldBe Json.obj(
        "_links" -> Json.obj(
          "paye" -> Json.obj(
            "href"  -> s"/individuals/employments/paye?matchId=$matchId{&fromDate,toDate}",
            "title" -> "Get an individual's PAYE employment data"
          ),
          "self" -> Json.obj("href" -> s"/individuals/employments/?matchId=$matchId")
        )
      )
    }
  }

  feature("Paye endpoint") {

    testErrorHandling("paye", nino, allScopes)
    testAuthorisation("paye", allScopes)

    scenario("invalid token") {
      Given("an invalid token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, allScopes)

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
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

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
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

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

      val employerRef = "247%2FZT6767895A"

      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate&employerRef=$employerRef")

      Then("the response status should be 404 (not found)")
      response.code shouldBe NOT_FOUND
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    scenario("missing fromDate") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&toDate=$toDate")

      Then("the response status should be 400 (invalid request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "fromDate is required"
      )
    }

    scenario("toDate earlier than fromDate") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$toDate&toDate=$fromDate")

      Then("the response status should be 400 (invalid request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Invalid time period requested"
      )
    }

    scenario("From date requested is earlier than 31st March 2013") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=2012-01-01&toDate=$toDate")

      Then("the response status should be 400 (invalid request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "fromDate earlier than 31st March 2013"
      )
    }

    scenario("Invalid fromDate") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=20xx-01-01&toDate=$toDate")

      Then("the response status should be 400 (invalid request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "fromDate: invalid date format"
      )
    }

    scenario("Invalid toDate") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=2017-09-40")

      Then("the response status should be 400 (invalid request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "toDate: invalid date format"
      )
    }

    scenario("valid request to the live paye endpoint implementation") {

      val employerRef = "247%2FZT6767895A"

      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId, nino)

      And("IF will return employments for the NINO")
      IfStub.searchEmploymentIncomeForPeriodReturns(nino, fromDate, toDate, validData)

      When("the paye endpoint is invoked with a valid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate&employerRef=$employerRef")

      Then("the response status should be 200 (ok)")
      response.code shouldBe OK
      Json.parse(response.body) shouldBe Json.obj(
        "_links" -> Json.obj(
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/paye?matchId=$matchId&fromDate=2017-01-01&toDate=2017-09-25"
          )
        ),
        "employments" -> Json.arr(
          Json.obj(
            "employer" -> Json.obj("name" -> "employer name")
          ))
      )
    }

    scenario("the IF rate limit is exceeded") {
      val matchId = UUID.randomUUID().toString
      val nino = "AA112233B"
      val employerRef = "247%2FZT6767895A"

      Given("a valid privileged Auth Bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId, nino)

      And("IF will return an error due to rate limiting")
      IfStub.enforceRateLimit(nino, fromDate, toDate)

      When("the PAYE endpoint is invoked with a valid match ID")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate&employerRef=$employerRef")

      Then("The response status is 429 Too Many Requests")
      response.code shouldBe TOO_MANY_REQUESTS
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "TOO_MANY_REQUESTS",
        "message" -> "Rate limit exceeded"
      )
    }

    scenario("Missing paye employerRef") {
      Given("a valid privileged Auth bearer token")

      AuthStub.willAuthorizePrivilegedAuthToken(authToken, allScopes)

      When("the paye endpoint is invoked without an employerRef")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 400 (invalid request)")
      response.code shouldBe BAD_REQUEST
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "employerRef is required for the scopes you have been assigned"
      )
    }
  }


  def testAuthorisation(endpoint:String, scopes: List[String]): Unit = {


    scenario(s"user does not have valid scopes") {
      Given("A valid auth token but invalid scopes")
      AuthStub.willNotAuthorizePrivilegedAuthTokenNoScopes(authToken)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/$endpoint?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")
        .headers(requestHeaders(acceptHeaderVP2))
        .asString

      Then("The response status should be 401")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "UNAUTHORIZED",
        "message" ->"Insufficient Enrolments"
      )
    }
  }

  def testErrorHandling( endpoint: String,
                         nino: String,
                         rootScope: List[String]): Unit = {

    val invalidEmployment = IfEmployments(
      Seq(
        IfEmployment(
          employer = Some(
            IfEmployer(
              name = Some("employer name"),
              None,
              districtNumber = Some("12345"),
              None
            )
          ),
          None,
          None,
          None
        )))

    scenario(s"valid request but invalid IF response") {

      val employerRef = "247%2FZT6767895A"

      Given("A valid auth token ")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, rootScope)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId.toString, nino)

      And("IF will return invalid response")
      IfStub.searchEmploymentIncomeForPeriodReturns(nino, fromDate, toDate, invalidEmployment)

      When(
        s"I make a call to ${if (endpoint.isEmpty) "root" else endpoint} endpoint")

      val response = Http(s"$serviceUrl/${endpoint}?matchId=$matchId&fromDate=$fromDate&toDate=$toDate&employerRef=$employerRef")
        .headers(requestHeaders(acceptHeaderVP2))
        .asString

      Then("The response status should be 500 with a generic error message")
      response.code shouldBe INTERNAL_SERVER_ERROR
      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INTERNAL_SERVER_ERROR",
        "message" -> "Something went wrong.")
    }

    scenario(s"IF returns an Internal Server Error") {

      val employerRef = "247%2FZT6767895A"

      Given("A valid auth token ")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, rootScope)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId.toString, nino)

      And("IF will return Internal Server Error")
      IfStub.saCustomResponse(nino, INTERNAL_SERVER_ERROR, fromDate, toDate, Json.obj("reason" -> "Server error"))

      When(
        s"I make a call to ${if (endpoint.isEmpty) "root" else endpoint} endpoint")
      val response = Http(s"$serviceUrl/${endpoint}?matchId=$matchId&fromDate=$fromDate&toDate=$toDate&employerRef=$employerRef")
        .headers(requestHeaders(acceptHeaderVP2))
        .asString

      Then("The response status should be 500 with a generic error message")
      response.code shouldBe INTERNAL_SERVER_ERROR
      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INTERNAL_SERVER_ERROR",
        "message" -> "Something went wrong.")
    }

    scenario(s"IF returns an Bad Request Error") {

      val employerRef = "247%2FZT6767895A"

      Given("A valid auth token ")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, rootScope)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.hasMatchingRecord(matchId.toString, nino)

      And("IF will return Internal Server Error")
      IfStub.saCustomResponse(nino, UNPROCESSABLE_ENTITY, fromDate,  toDate, Json.obj("reason" ->
        "There are 1 or more unknown data items in the 'fields' query string"))

      When(
        s"I make a call to ${if (endpoint.isEmpty) "root" else endpoint} endpoint")
      val response = Http(s"$serviceUrl/${endpoint}?matchId=$matchId&fromDate=$fromDate&toDate=$toDate&employerRef=$employerRef")
        .headers(requestHeaders(acceptHeaderVP2))
        .asString

      Then("The response status should be 500 with a generic error message")
      response.code shouldBe INTERNAL_SERVER_ERROR
      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INTERNAL_SERVER_ERROR",
        "message" -> "Something went wrong.")
    }

  }


  private def invokeEndpoint(endpoint: String) =
    Http(endpoint)
      .timeout(10000, 10000)
      .headers(requestHeaders(acceptHeaderVP2))
      .asString
}
