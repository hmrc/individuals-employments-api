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

package uk.gov.hmrc.individualsemploymentsapi.domain.v2

import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfAddress

case class Address(
  line1: Option[String],
  line2: Option[String],
  line3: Option[String],
  line4: Option[String],
  line5: Option[String],
  postcode: Option[String])

object Address {
  implicit def ifAddressToAddress(address: IfAddress): Address =
    Address(address.line1, address.line2, address.line3, address.line4, address.line5, address.postcode)

  implicit def ifAddressToAddressOpt(address: Option[IfAddress]): Option[Address] =
    address match {
      case Some(a) => Option(Address(a.line1, a.line2, a.line3, a.line4, a.line5, a.postcode))
      case _       => None
    }

}
