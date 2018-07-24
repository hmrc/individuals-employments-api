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

package uk.gov.hmrc.individualsemploymentsapi.sandbox

import java.util.UUID

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.domain._
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment, DesPayFrequency, DesPayment}

object SandboxData {

  val sandboxMatchIdString = "57072660-1df9-4aeb-b4ea-cd2d7f96e430"
  val sandboxMatchId = UUID.fromString(sandboxMatchIdString)

  val sandboxNinoString = "NA000799C"
  val sandboxNino = Nino(sandboxNinoString)

  object Employments {
    val acme = DesEmployment(
      Seq(
        DesPayment(new LocalDate(2016, 1, 28), 0),
        DesPayment(new LocalDate(2016, 2, 28), 0),
        DesPayment(new LocalDate(2016, 3, 28), 0),
        DesPayment(new LocalDate(2016, 4, 28), 0),
        DesPayment(new LocalDate(2016, 5, 28), 0)
      ),
      Some("Acme"),
      Some(DesAddress(line1 = "Acme Inc Building", line2 = Some("Acme Inc Campus"), line3 = Some("Acme Street"), line4 = Some("AcmeVille"), line5 = Some("Acme State"), postalCode = Some("AI22 9LL"))),
      Some("123"),
      Some("AI45678"),
      Some(new LocalDate(2016, 1, 1)),
      Some(new LocalDate(2016, 6, 30)),
      Some(DesPayFrequency.W4),
      Some(DesAddress("Employee's House", Some("Employee Street"), Some("Employee Town"), None, None, Some("AA11 1AA"))),
      Some("payroll-id")
    )
    val disney = DesEmployment(
      Seq(
        DesPayment(new LocalDate(2017, 2, 19), 0),
        DesPayment(new LocalDate(2017, 2, 28), 0)
      ),
      Some("Disney"),
      Some(DesAddress(line1 = "Friars House", line2 = Some("Campus Way"), line3 = Some("New Street"), line4 = Some("Sometown"), line5 = Some("Old County"), postalCode = Some("TF22 3BC"))),
      Some("123"),
      Some("DI45678"),
      Some(parse("2017-01-02")),
      Some(parse("2017-03-01")),
      Some(DesPayFrequency.W2),
      Some(DesAddress("", Some(""), Some(""), Some(""), Some(""), Some(""))),
      Some("another-payroll-id")
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
