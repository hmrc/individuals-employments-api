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

package unit.uk.gov.hmrc.individualsemploymentsapi.domain.v1

import org.joda.time.LocalDate
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequencyCode
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment}
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.{Address, Employer, Employment, PayFrequency}

class EmploymentSpec extends FlatSpec with Matchers {

  "Employment" should "derive itself from an instance of des employment" in {
    val desEmployment = DesEmployment(
      Seq.empty,
      Some("Acme Inc"),
      Some(DesAddress(Some("line 1"), postalCode = Some("AB1 2CD"))),
      Some("123"),
      Some("AB12345"),
      Some(new LocalDate(2016, 1, 1)),
      Some(new LocalDate(2016, 12, 31)),
      Some(PayFrequencyCode.M1)
    )

    val employment = Employment(
      Some(new LocalDate(2016, 1, 1)),
      Some(new LocalDate(2016, 12, 31)),
      Some(
        Employer(
          Some(EmpRef("123", "AB12345")),
          Some("Acme Inc"),
          Some(
            Address(
              line1 = Some("line 1"),
              line2 = None,
              line3 = None,
              line4 = None,
              line5 = None,
              postcode = Some("AB1 2CD")
            ))
        )),
      Some(PayFrequency.withName("CALENDAR_MONTHLY")),
      None,
      None
    )

    Employment.from(desEmployment) shouldBe Some(employment)
  }

  it should "handle the edge case where des employment is empty" in {
    val desEmployment = DesEmployment(Seq.empty, None, None, None, None, None, None, None)
    Employment.from(desEmployment) shouldBe None
  }

}
