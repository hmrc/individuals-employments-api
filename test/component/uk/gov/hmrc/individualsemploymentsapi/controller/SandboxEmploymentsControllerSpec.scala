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

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.{AuthStub, BaseSpec}
import play.api.http.Status.OK

import scalaj.http.Http

class SandboxEmploymentsControllerSpec extends BaseSpec {

  feature("View individuals root (hateoas) entry point is accessible") {

    scenario("valid request to the sandbox implementation") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken)

      When("I request the root entry point to the API")
      val response = Http(s"$serviceUrl/sandbox/match?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430").headers(requestHeaders()).asString

      Then("The response status should be 200 (OK)")
      response.code shouldBe OK
      response.body shouldBe
        """{"_links":{"paye":{"href":"/individuals/employments/paye/match?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430{&fromDate,toDate}","title":"View individual's employments"},"self":{"href":"/individuals/employments/match?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"}}}"""
    }
  }

}
