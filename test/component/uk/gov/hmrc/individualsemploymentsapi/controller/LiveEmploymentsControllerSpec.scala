/*
 * Copyright 2018 HM Revenue & Customs
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

package component.uk.gov.hmrc.individualsemploymentsapi.controller

import java.util.UUID

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.{AuthStub, BaseSpec, DesStub, IndividualsMatchingApiStub}
import play.api.libs.json.Json.parse
import play.api.test.Helpers._
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesEmployment, DesEmployments}

import scalaj.http.{Http, HttpResponse}

class LiveEmploymentsControllerSpec extends BaseSpec {

  private val matchId = UUID.randomUUID().toString
  private val nino = "AB123456C"
  private val employmentsScope = "read:individuals-employments"
  private val payeEmploymentsScope = "read:individuals-employments-paye"

  feature("Root (hateoas) entry point is accessible") {

    scenario("invalid token") {
      Given("an invalid token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, employmentsScope)

      When("the root entry point to the API is invoked")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 401 (unauthorized)")
      assertResponseIs(response, UNAUTHORIZED,
        """
          {
             "code" : "UNAUTHORIZED",
             "message" : "Bearer token is missing or not authorized"
          }
        """)
    }

    scenario("missing match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope)

      When("the root entry point to the API is invoked with a missing match id")
      val response = invokeEndpoint(serviceUrl)

      Then("the response status should be 400 (bad request)")
      assertResponseIs(response, BAD_REQUEST,
        """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId is required"
          }
        """)
    }

    scenario("malformed match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope)

      When("the root entry point to the API is invoked with a malformed match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=malformed-match-id-value")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(response, BAD_REQUEST,
        """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId format is invalid"
          }
        """)
    }

    scenario("invalid match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope)

      When("the root entry point to the API is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 404 (not found)")
      assertResponseIs(response, NOT_FOUND,
        """
          {
             "code" : "NOT_FOUND",
             "message" : "The resource can not be found"
          }
        """)
    }

    scenario("valid request to the live root endpoint implementation") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, employmentsScope)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.willRespondWith(matchId, OK,
        s"""
          {
            "matchId" : "$matchId",
            "nino" : "$nino"
          }
        """)

      When("the root entry point to the API is invoked with a valid match id")
      val response = invokeEndpoint(s"$serviceUrl/?matchId=$matchId")

      Then("the response status should be 200 (ok)")
      assertResponseIs(response, OK,
        s"""
          {
            "_links":{
              "paye":{
                "href":"/individuals/employments/paye?matchId=$matchId{&fromDate,toDate}",
                "title":"View individual's employments"
              },
              "self":{
                "href":"/individuals/employments/?matchId=$matchId"
              }
            }
          }
        """)
    }

  }

  feature("Paye endpoint") {

    val fromDate = "2017-01-01"
    val toDate = "2017-09-25"

    scenario("invalid token") {
      Given("an invalid token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope)

      When("the paye endpoint is invoked")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 401 (unauthorized)")
      assertResponseIs(response, UNAUTHORIZED,
        """
          {
             "code" : "UNAUTHORIZED",
             "message" : "Bearer token is missing or not authorized"
          }
        """)
    }

    scenario("missing match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope)

      When("the paye endpoint is invoked with a missing match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(response, BAD_REQUEST,
        """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId is required"
          }
        """)
    }

    scenario("malformed match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope)

      When("the paye endpoint is invoked with a malformed match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=malformed-match-id-value&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(response, BAD_REQUEST,
        """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId format is invalid"
          }
        """)
    }

    scenario("invalid match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope)

      When("the paye endpoint is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 404 (not found)")
      assertResponseIs(response, NOT_FOUND,
        """
          {
             "code" : "NOT_FOUND",
             "message" : "The resource can not be found"
          }
        """)
    }

    scenario("valid request to the live paye endpoint implementation") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, payeEmploymentsScope)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.willRespondWith(matchId, OK,
        s"""
          {
            "matchId" : "$matchId",
            "nino" : "$nino"
          }
        """)

      And("DES will return employments for the NINO")
      DesStub.searchEmploymentIncomeForPeriodReturns(nino, fromDate, toDate, DesEmployments(Seq(DesEmployment(Seq.empty, Some("employer name")))))

      When("the paye endpoint is invoked with a valid match id")
      val response = invokeEndpoint(s"$serviceUrl/paye?matchId=$matchId&fromDate=$fromDate&toDate=$toDate")

      Then("the response status should be 200 (ok)")
      assertResponseIs(response, OK,
        s"""
           {
             "_links":{
               "self":{
                 "href":"/individuals/employments/paye?matchId=$matchId&fromDate=2017-01-01&toDate=2017-09-25"
               }
             },
             "employments":[
               {
                 "employer":{
                   "name":"employer name"
                 }
               }
             ]
           }
        """)
    }

  }

  private def invokeEndpoint(endpoint: String) = Http(endpoint).timeout(10000, 10000).headers(requestHeaders()).asString

  private def assertResponseIs(httpResponse: HttpResponse[String], expectedResponseCode: Int, expectedResponseBody: String) = {
    httpResponse.code shouldBe expectedResponseCode
    parse(httpResponse.body) shouldBe parse(expectedResponseBody)
  }

}
