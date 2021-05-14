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

package uk.gov.hmrc.individualsemploymentsapi.service.v2

import play.api.mvc.RequestHeader

import javax.inject.Inject

class ScopeFilterVerificationService @Inject()(scopesService: ScopesService) {

  val filterParameterMappings = Map(
    "M" -> "employerRef"
  )

  def verify(scopes: List[String], endpoint: String, rh: RequestHeader): Boolean = {
    val validFilters = scopesService.getValidFilterKeys(scopes, List(endpoint))

    val requiredParameters = validFilters.flatMap(f => filterParameterMappings.get(f))
    for( x <- validFilters) println(x)

    println(s"${validFilters} oioi ${requiredParameters}")
    !requiredParameters.map(p => rh.queryString.get(p)).exists(_.isEmpty)
  }
}
