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

package component.uk.gov.hmrc.individualsemploymentsapi.v2

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
        .headers(requestHeaders(acceptHeaderVP2))
        .asString

      Then("The response status should be 200 (OK)")
      response.code shouldBe INTERNAL_SERVER_ERROR
      response.body shouldBe "{\"statusCode\":500,\"message\":\"NOT_IMPLEMENTED\"}"
    }

  }

}
