/*
 * Copyright 2019 HM Revenue & Customs
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

package component.uk.gov.hmrc.individualsemploymentsapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import component.uk.gov.hmrc.individualsemploymentsapi.controller.MockHost
import play.api.http.HeaderNames
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.auth.core.Enrolment

object AuthStub extends MockHost(22000) {

  def willAuthorizePrivilegedAuthToken(authBearerToken: String, scope: String): StubMapping = {
    mock.register(post("/auth/authorise")
      .withRequestBody(equalToJson(privilegedAuthority(scope).toString()))
      .withHeader(AUTHORIZATION, equalTo(authBearerToken))
      .willReturn(okJson(Json.obj("internalId" -> "some-id").toString)))
  }

  def willNotAuthorizePrivilegedAuthToken(authBearerToken: String, scope: String): StubMapping = {
    mock.register(post(urlEqualTo("/auth/authorise"))
      .withRequestBody(equalToJson(privilegedAuthority(scope).toString()))
      .withHeader(AUTHORIZATION, equalTo(authBearerToken))
      .willReturn(
        unauthorized()
          .withHeader(HeaderNames.WWW_AUTHENTICATE, """MDTP detail="Bearer token is missing or not authorized"""")))
  }

  private def privilegedAuthority(scope: String) = Json.obj(
    "authorise" -> Json.arr(Json.toJson(Enrolment(scope))),
    "retrieve" -> JsArray()
  )
}
