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

package component.uk.gov.hmrc.individualsemploymentsapi.v1

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.BaseSpec
import play.api.http.Status._
import play.api.libs.json.Json.parse
import scalaj.http.Http
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.sandboxMatchId

class EmploymentsSpec extends BaseSpec {
  private val payeEmploymentsScope = "read:individuals-employments-paye"

  feature("individual employments is open and accessible") {

    scenario("Valid request to the sandbox implementation") {

      When("I request individual employments for the sandbox matchId")
      val response = Http(s"$serviceUrl/sandbox/paye?matchId=$sandboxMatchId&fromDate=2016-04-01&toDate=2017-01-01")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      Then("The response status should be 200 (OK)")
      response.code shouldBe OK
      parse(response.body) shouldBe parse(s"""
          {
            "_links":{
              "self":{
                "href":"/individuals/employments/paye?matchId=$sandboxMatchId&fromDate=2016-04-01&toDate=2017-01-01"
              }
            },
            "employments":[
              {
                "startDate":"2016-01-01",
                "endDate":"2016-06-30",
                "employer":{
                  "payeReference":"123/AI45678",
                  "name":"Acme",
                  "address":{
                    "line1":"Acme Inc Building",
                    "line2":"Acme Inc Campus",
                    "line3":"Acme Street",
                    "line4":"AcmeVille",
                    "line5":"Acme State",
                    "postcode":"AI22 9LL"
                  }
                },
                "payFrequency":"FOUR_WEEKLY"
              }
            ]
          }
        """)
    }

  }

}
