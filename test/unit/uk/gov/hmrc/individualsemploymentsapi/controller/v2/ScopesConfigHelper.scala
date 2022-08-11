/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsemploymentsapi.controller.v2

import play.api.Configuration

trait ScopesConfigHelper {

  val mockScopesConfig = Configuration(
    (s"api-config.scopes.test-scope.fields", List("A", "B", "C")),
    (s"api-config.endpoints.internal.paye.endpoint", "/individuals/employments/paye?matchId=<matchId>{&startDate,endDate}"),
    (s"api-config.endpoints.internal.paye.title", "Get an individual's PAYE employment data"),
    (s"api-config.endpoints.internal.paye.fields", Seq("A", "B", "C")),
    (s"api-config.fields.A", "employments/employer/name"),
    (s"api-config.fields.B", "employments/employer/districtNumber"),
    (s"api-config.fields.C", "employments/employer/schemeRef")
  )
}
