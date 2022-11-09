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

package uk.gov.hmrc.individualsemploymentsapi.domain.v2

object PayFrequency extends Enumeration {

  type PayFrequency = Value
  val WEEKLY, FORTNIGHTLY, FOUR_WEEKLY, ONE_OFF, IRREGULAR, CALENDAR_MONTHLY, QUARTERLY, BI_ANNUALLY, ANNUALLY = Value

  private val ifConversionMap = Map(
    "W1" -> WEEKLY,
    "W2" -> FORTNIGHTLY,
    "W4" -> FOUR_WEEKLY,
    "IO" -> ONE_OFF,
    "IR" -> IRREGULAR,
    "M1" -> CALENDAR_MONTHLY,
    "M3" -> QUARTERLY,
    "M6" -> BI_ANNUALLY,
    "MA" -> ANNUALLY
  )

  def from(ifValue: String): Option[Value] = ifConversionMap.get(ifValue)

}
