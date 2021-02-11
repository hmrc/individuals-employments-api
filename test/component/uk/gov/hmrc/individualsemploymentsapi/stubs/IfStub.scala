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

package component.uk.gov.hmrc.individualsemploymentsapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import component.uk.gov.hmrc.individualsemploymentsapi.controller.MockHost
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployments

object IfStub extends MockHost(22004) {

  def searchEmploymentIncomeForPeriodReturns(
    nino: String,
    fromDate: String,
    toDate: String,
    ifEmployments: IfEmployments) =
    mock.register(
      get(urlPathEqualTo(s"/individuals/employment/nino/$nino"))
        .withQueryParam("startDate", equalTo(fromDate))
        .withQueryParam("endDate", equalTo(toDate))
        .withQueryParam(
          "fields",
          equalTo(
            "employments(employer(address(line1,line2,line3,line4,line5,postcode),districtNumber,name,schemeRef),employment(endDate,startDate))")
        )
        .willReturn(aResponse().withStatus(OK).withBody(Json.toJson(ifEmployments).toString())))

  def saCustomResponse(nino: String, status: Int, fromDate: String, toDate: String, response: JsValue) =
    mock.register(
      get(urlPathEqualTo(s"/individuals/employment/nino/$nino"))
        .withQueryParam("startDate", equalTo(fromDate))
        .withQueryParam("endDate", equalTo(toDate))
        .withQueryParam("fields", equalTo("employments(employer(address(line1,line2,line3,line4,line5,postcode),districtNumber,name,schemeRef),employment(endDate,startDate))"))
        .willReturn(aResponse().withStatus(status).withBody(Json.toJson(response.toString()).toString())))


  def enforceRateLimit(nino: String, fromDate: String, toDate: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/individuals/employment/nino/$nino"))
        .withQueryParam("startDate", equalTo(fromDate))
        .withQueryParam("endDate", equalTo(toDate))
        .withQueryParam(
          "fields",
          equalTo(
            "employments(employer(address(line1,line2,line3,line4,line5,postcode),districtNumber,name,schemeRef),employment(endDate,startDate))")
        )
        .willReturn(aResponse().withStatus(TOO_MANY_REQUESTS)))

}
