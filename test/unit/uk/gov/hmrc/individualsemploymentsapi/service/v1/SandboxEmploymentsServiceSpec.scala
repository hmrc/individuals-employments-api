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

package unit.uk.gov.hmrc.individualsemploymentsapi.service

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.domain.Employment
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.{Employments, sandboxMatchId}
import uk.gov.hmrc.individualsemploymentsapi.service.v1.SandboxEmploymentsService
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.Intervals

class SandboxEmploymentsServiceSpec extends SpecBase with Intervals {

  val sandboxEmploymentsService = new SandboxEmploymentsService
  implicit val hc = new HeaderCarrier

  "Sandbox employments service paye function" should {

    "return employments for the entire available history ordered by date descending" in {
      val res = await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-01-01", "2017-03-01")))
      val expected = Seq(Employment.from(Employments.disney).get, Employment.from(Employments.acme).get)

      res shouldBe expected
    }

    "return employments for a limited period" in {
      val res = await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-01-01", "2016-07-01")))

      res shouldBe Employment.from(Employments.acme).toSeq
    }

    "return correct employments when range includes a period of no payments" in {
      val res = await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-04-30", "2017-02-15")))
      val expected = Seq(Employment.from(Employments.disney).get, Employment.from(Employments.acme).get)

      res shouldBe expected
    }

    "return no employments when the individual has no employment for a given period" in {
      await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-08-01", "2016-09-01"))) shouldBe Nil
    }

  }

}
