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

package unit.uk.gov.hmrc.individualsemploymentsapi.domain.v2

import org.joda.time.LocalDate
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequencyCode
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.{IfAddress, IfEmployer, IfEmployment, IfEmploymentDetail, IfPayment}
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.{Address, Employer, Employment, PayFrequency, Payment}

class EmploymentSpec extends FlatSpec with Matchers {

  "Employment" should "derive itself from an instance of IF employment" in {
    val ifPayment = IfPayment(
      Some("2016-01-01"),
      None,
      Some(21.21),
      None,
      None,
      None
    )
    val ifEmployment = IfEmployment(
      employer = Some(
        IfEmployer(
          name = Some("Acme Inc"),
          address = Some(
            IfAddress(
              line1 = Some("line 1"),
              postcode = Some("AB1 2CD")
            )),
          districtNumber = Some("123"),
          schemeRef = Some("AB12345")
        )),
      employment = Some(
        IfEmploymentDetail(
          startDate = Some(new LocalDate(2016, 1, 1).toString()),
          endDate = Some(new LocalDate(2016, 12, 31).toString()),
          payFrequency = Some(PayFrequencyCode.M1.toString),
          payrollId = None,
          address = None
        )),
      payments = Some(Seq(ifPayment)),
      employerRef = Some("247/A1987CB")
    )

    val employment = Employment.create(
      Some(new LocalDate(2016, 1, 1)),
      Some(new LocalDate(2016, 12, 31)),
      Some(PayFrequency.withName("CALENDAR_MONTHLY")),
      Employer.create(
        Some(EmpRef("247", "A1987CB")),
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
      ),
      Some(Seq(Payment.create(ifPayment)).flatten)
    )

    Employment.create(ifEmployment) shouldBe employment
  }

  it should "handle the edge case where IF employment is empty" in {
    val ifEmployment = IfEmployment(None, None, None, None)
    Employment.create(ifEmployment) shouldBe None
  }

}
