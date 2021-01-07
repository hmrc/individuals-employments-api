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

package uk.gov.hmrc.individualsemploymentsapi.sandbox.v2

import java.util.UUID

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequencyCode
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.{IfAddress, IfEmployer, IfEmployment, IfEmploymentDetail, IfPayment, Individual}

object SandboxData {

  val sandboxMatchIdString = "57072660-1df9-4aeb-b4ea-cd2d7f96e430"
  val sandboxMatchId = UUID.fromString(sandboxMatchIdString)

  val sandboxNinoString = "NA000799C"
  val sandboxNino = Nino(sandboxNinoString)

  object Employments {

    val acme = IfEmployment(
      employer = Some(
        IfEmployer(
          name = Some("Acme"),
          address = Some(IfAddress(
            line1 = Some("Acme Inc Building"),
            line2 = Some("Acme Inc Campus"),
            line3 = Some("Acme Street"),
            line4 = Some("AcmeVille"),
            line5 = Some("Acme State"),
            postcode = Some("AI22 9LL")
          )),
          districtNumber = Some("123"),
          schemeRef = Some("AI45678")
        )),
      employment = Some(
        IfEmploymentDetail(
          startDate = Some(new LocalDate(2016, 1, 1).toString()),
          endDate = Some(new LocalDate(2016, 6, 30).toString()),
          payFrequency = Some(PayFrequencyCode.W4.toString),
          payrollId = Some("payroll-id"),
          address = Some(
            IfAddress(
              line1 = Some("Employment House"),
              line2 = Some("Employment Street"),
              line3 = Some("Employment Town"),
              line4 = None,
              line5 = None,
              postcode = Some("AA11 1AA")
            ))
        )
      ),
      payments = None
    )

    val disney = IfEmployment(
      payments = None,
      employer = Some(
        IfEmployer(
          name = Some("Disney"),
          address = Some(
            IfAddress(
              line1 = Some("Friars House"),
              line2 = Some("Campus Way"),
              line3 = Some("New Street"),
              line4 = Some("Sometown"),
              line5 = Some("Old County"),
              postcode = Some("TF22 3BC")
            )),
          districtNumber = Some("123"),
          schemeRef = Some("DI45678")
        )
      ),
      employment = Some(
        IfEmploymentDetail(
          startDate = Some("2017-01-02"),
          endDate = Some("2017-03-01"),
          payFrequency = Some(PayFrequencyCode.W2.toString),
          payrollId = Some("another-payroll-id"),
          address = Some(
            IfAddress(
              line1 = None,
              line2 = None,
              line3 = None,
              line4 = None,
              line5 = None,
              postcode = None
            )
          )
        )
      )
    )
  }

  object Individuals {

    val amanda = Individual(
      sandboxMatchId,
      sandboxNinoString,
      Seq(Employments.acme, Employments.disney)
    )

    val individuals = Seq(amanda)

    def find(matchId: UUID) = individuals.find(_.matchId == matchId)

  }

}
