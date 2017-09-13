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

package component.uk.gov.hmrc.individualsemploymentsapi.controller

import java.util.UUID

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.{AuthStub, BaseSpec, IndividualsMatchingApiStub}
import play.api.libs.json.Json.parse
import play.api.test.Helpers._

import scalaj.http.{Http, HttpResponse}

class LiveEmploymentsControllerSpec extends BaseSpec {

  feature("Root (hateoas) entry point is accessible") {

    val matchId = UUID.randomUUID().toString

    def invokeRootEndpoint = Http(s"$serviceUrl/?matchId=$matchId").timeout(10000, 10000).headers(requestHeaders()).asString

    def assertResponseIs(httpResponse: HttpResponse[String], expectedResponseCode: Int, expectedResponseBody: String) = {
      httpResponse.code shouldBe expectedResponseCode
      parse(httpResponse.body) shouldBe parse(expectedResponseBody)
    }

    scenario("invalid token") {
      Given("an invalid token")

      When("the root entry point to the API is invoked")
      val response = invokeRootEndpoint

      Then("the response status should be 401 (unauthorized)")
      assertResponseIs(response, UNAUTHORIZED,
        """
          {
             "code" : "UNAUTHORIZED",
             "message" : "Bearer token is missing or not authorized"
          }
        """)
    }

    scenario("invalid match id") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken)

      When("the root entry point to the API is invoked with an invalid match id")
      val response = invokeRootEndpoint

      Then("the response status should be 404 (not found)")
      assertResponseIs(response, NOT_FOUND,
        """
          {
             "code" : "NOT_FOUND",
             "message" : "The resource can not be found"
          }
        """)
    }

    scenario("valid request to the live implementation") {
      Given("a valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken)

      And("a valid record in the matching API")
      IndividualsMatchingApiStub.willRespondWith(matchId, OK,
        """
          {
            "matchId" : "951dcf9f-8dd1-44e0-91d5-cb772c8e8e5e",
            "nino" : "AB123456C"
          }
        """)

      When("the root entry point to the API is invoked with a valid match id")
      val response = invokeRootEndpoint

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
                "href":"/individuals/employments?matchId=$matchId"
              }
            }
          }
        """)
    }

  }

}
