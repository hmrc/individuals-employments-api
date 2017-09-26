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

package uk.gov.hmrc.individualsemploymentsapi.domain

import uk.gov.hmrc.domain.EmpRef

case class Employer(payeReference: Option[EmpRef], name: Option[String], address: Option[Address])

object Employer {
  def create(payeReference: Option[EmpRef], name: Option[String], address: Option[Address]): Option[Employer] = {
    (payeReference, name, address) match {
      case (None, None, None) => None
      case _ => Some(Employer(payeReference, name, address))
    }
  }
}

