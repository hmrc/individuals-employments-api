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

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{ScopeFilterVerificationService, ScopesHelper, ScopesService}
import unit.uk.gov.hmrc.individualsemploymentsapi.service.ScopesConfig
import utils.UnitSpec

class ScopeFilterVerificationServiceSpec extends UnitSpec with ScopesConfig {
  val scopesService = new ScopesService(mockConfig) {
    override lazy val apiConfig = mockApiConfig
  }

  val scopeFilterVerificationService = new ScopeFilterVerificationService(scopesService)

  "should return True if all required query parameters are present" in {
    val requestHeader = FakeRequest("GET", "/?employerRef='3287654321'")
    val result = scopeFilterVerificationService.verify(List(mockScope8), mockEndpoint4, requestHeader)

    result shouldBe true
  }

  "should return False if any required query parameters are not present" in {
    val requestHeader = FakeRequest("GET", "/")
    val result = scopeFilterVerificationService.verify(List(mockScope8), mockEndpoint4, requestHeader)

    result shouldBe false
  }

  "should return True if query parameters are not required" in {
    val requestHeader = FakeRequest("GET", "/")
    val result = scopeFilterVerificationService.verify(List(mockScope2), mockEndpoint1, requestHeader)

    result shouldBe true
  }
}
