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

package uk.gov.hmrc.individualsemploymentsapi.sandbox

import java.util.UUID

import org.joda.time.LocalDate.parse
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequency._
import uk.gov.hmrc.individualsemploymentsapi.domain._
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.Employers._

object SandboxData {

  val sandboxMatchIdString = "57072660-1df9-4aeb-b4ea-cd2d7f96e430"
  val sandboxMatchId = UUID.fromString(sandboxMatchIdString)

  val sandboxNinoString = "NA000799C"
  val sandboxNino = Nino(sandboxNinoString)

  object Employers {

    object Acme {
      val empRef = EmpRef.fromIdentifiers("123/AI45678")
      val address = Address("Acme Inc Building", Some("Acme Inc Campus"), Some("Acme Street"), Some("AcmeVille"), Some("Acme State"), "AI22 9LL")
      val employer = Employer(Option(empRef), Option("Acme"), Option(address))
    }

    object Disney {
      val empRef = EmpRef.fromIdentifiers("123/DI45678")
      val address = Address("Friars House", Some("Campus Way"), Some("New Street"), Some("Sometown"), Some("Old County"), "TF22 3BC")
      val employer = Employer(Option(empRef), Option("Disney"), Option(address))
    }

  }

  object Employments {
    val acme = Employment(Option(parse("2016-01-01")), Option(parse("2016-06-30")), Option(Acme.employer), Option(FOUR_WEEKLY))
    val disney = Employment(Option(parse("2017-01-02")), Option(parse("2017-03-01")), Option(Disney.employer), Option(FORTNIGHTLY))
  }

  object Individuals {

    val amanda = Individual(sandboxMatchId, sandboxNinoString,
      Seq(Employments.acme, Employments.disney),
      Seq(
        Payment(0, parse("2016-01-28"), Option(Acme.empRef)),
        Payment(0, parse("2016-02-28"), Option(Acme.empRef)),
        Payment(0, parse("2016-03-28"), Option(Acme.empRef)),
        Payment(0, parse("2016-04-28"), Option(Acme.empRef)),
        Payment(0, parse("2016-05-28"), Option(Acme.empRef)),
        Payment(0, parse("2017-02-09"), Option(Disney.empRef)),
        Payment(0, parse("2017-02-16"), Option(Disney.empRef))
      )
    )

    val individuals = Seq(amanda)

    def find(matchId: UUID) = individuals.find(_.matchId == matchId)

  }

}
