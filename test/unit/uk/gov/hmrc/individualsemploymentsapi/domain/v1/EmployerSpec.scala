/*
 * Copyright 2023 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsemploymentsapi.domain.v1

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.{Address, Employer}

class EmployerSpec extends AnyFlatSpec with Matchers {

  "Employer" should "derive itself from some parameters" in {
    val someEmpRef = Some(EmpRef("123", "AB45678"))
    val someName = Some("name")
    val someAddress = Some(
      Address(
        line1 = Some("line1"),
        line2 = None,
        line3 = None,
        line4 = None,
        line5 = None,
        postcode = Some("AB1 2CD")
      ))
    val employer = Employer(someEmpRef, someName, someAddress)
    Employer.create(someEmpRef, someName, someAddress) shouldBe Some(employer)
  }

  it should "handle the edge case where parameters are empty" in {
    Employer.create(None, None, None) shouldBe None
  }

}
