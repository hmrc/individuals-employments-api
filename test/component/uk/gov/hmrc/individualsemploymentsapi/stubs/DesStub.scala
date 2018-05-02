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

package component.uk.gov.hmrc.individualsemploymentsapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import component.uk.gov.hmrc.individualsemploymentsapi.controller.MockHost
import play.api.http.Status
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.individualsemploymentsapi.domain.des.DesEmployments
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

object DesStub extends MockHost(22003) {

  def searchEmploymentIncomeForPeriodReturns(nino: String, fromDate: String, toDate: String, desEmployments: DesEmployments) = {
    mock.register(get(urlPathEqualTo(s"/individuals/nino/$nino/employments/income"))
      .withQueryParam("from", equalTo(fromDate))
      .withQueryParam("to", equalTo(toDate))
      .willReturn(aResponse().withStatus(OK).withBody(toJson(desEmployments).toString())))
  }

}
