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

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.BaseSpec
import play.api.libs.json.Json.parse
import play.api.test.Helpers._
import scalaj.http.{Http, HttpResponse}

class SandboxEmploymentsControllerSpec extends BaseSpec {
  private val employmentsScope = "read:individuals-employments"

  feature("View individuals root (hateoas) entry point is accessible") {

    scenario("missing match id") {
      When("the root entry point to the API is invoked with a missing match id")
      val response = invokeEndpoint(s"$serviceUrl/sandbox")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(response, BAD_REQUEST, """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId is required"
          }
        """)
    }

    scenario("malformed match id") {
      When("the root entry point to the API is invoked with a malformed match id")
      val response = invokeEndpoint(s"$serviceUrl/sandbox?matchId=malformed-match-id-value")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(
        response,
        BAD_REQUEST,
        """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId format is invalid"
          }
        """
      )
    }

    scenario("invalid match id") {

      When("the root entry point to the API is invoked with an invalid match id")
      val response = invokeEndpoint(s"$serviceUrl/sandbox?matchId=0a184ef3-fd75-4d4d-b6a3-f886cc39a366")

      Then("the response status should be 404 (not found)")
      assertResponseIs(
        response,
        NOT_FOUND,
        """
          {
             "code" : "NOT_FOUND",
             "message" : "The resource can not be found"
          }
        """
      )
    }

    scenario("valid request to the sandbox implementation") {
      When("I request the root entry point to the API")
      val response = invokeEndpoint(s"$serviceUrl/sandbox?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430")

      Then("The response status should be 200 (ok)")
      assertResponseIs(
        response,
        OK,
        """
          {
             "_links":{
               "paye":{
                 "href":"/individuals/employments/paye?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430{&fromDate,toDate}",
                 "title":"Get an individual's PAYE employment data"
               },
               "self":{
                 "href":"/individuals/employments/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"
               }
             }
           }
        """
      )
    }

  }

  feature("Paye endpoint") {

    scenario("missing match id") {
      When("the root entry point to the API is invoked with a missing match id")
      val response = invokeEndpoint(s"$serviceUrl/sandbox/paye")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(response, BAD_REQUEST, """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId is required"
          }
        """)
    }

    scenario("malformed match id") {
      When("the root entry point to the API is invoked with a malformed match id")
      val response = invokeEndpoint(s"$serviceUrl/sandbox/paye?matchId=malformed-match-id-value")

      Then("the response status should be 400 (bad request)")
      assertResponseIs(
        response,
        BAD_REQUEST,
        """
          {
             "code" : "INVALID_REQUEST",
             "message" : "matchId format is invalid"
          }
        """
      )
    }

    scenario("invalid match id") {
      When("the root entry point to the API is invoked with an invalid match id")
      val response =
        invokeEndpoint(s"$serviceUrl/sandbox/paye?matchId=0a184ef3-fd75-4d4d-b6a3-f886cc39a366&fromDate=2017-01-01")

      Then("the response status should be 404 (not found)")
      assertResponseIs(
        response,
        NOT_FOUND,
        """
          {
             "code" : "NOT_FOUND",
             "message" : "The resource can not be found"
          }
        """
      )
    }

    scenario("valid request to the sandbox implementation") {
      When("I request the root entry point to the API")
      val response =
        invokeEndpoint(s"$serviceUrl/sandbox/paye?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430&fromDate=2017-01-01")

      Then("The response status should be 200 (ok)")
      assertResponseIs(
        response,
        OK,
        """{
          "_links":{
            "self":{
              "href":"/individuals/employments/paye?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430&fromDate=2017-01-01"
            }
          },
          "employments": [{
            "startDate":"2017-01-02",
            "endDate":"2017-03-01",
            "payFrequency":"FORTNIGHTLY",
            "employer":{
              "payeReference":"123/DI45678",
              "name":"Disney",
              "address":{
                "line1":"Friars House",
                "line2":"Campus Way",
                "line3":"New Street",
                "line4":"Sometown",
                "line5":"Old County",
                "postcode":"TF22 3BC"}
              }
            }]
          }""".stripMargin
      )
    }
  }

  private def invokeEndpoint(endpoint: String) =
    Http(endpoint)
      .timeout(10000, 10000)
      .headers(requestHeaders(acceptHeaderVP2))
      .asString

  private def assertResponseIs(
    httpResponse: HttpResponse[String],
    expectedResponseCode: Int,
    expectedResponseBody: String) = {
    httpResponse.code shouldBe expectedResponseCode
    parse(httpResponse.body) shouldBe parse(expectedResponseBody)
  }

}
