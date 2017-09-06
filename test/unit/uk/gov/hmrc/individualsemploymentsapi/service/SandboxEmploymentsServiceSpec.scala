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

package unit.uk.gov.hmrc.individualsemploymentsapi.service

import java.util.UUID

import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.{Employments, sandboxMatchId}
import uk.gov.hmrc.individualsemploymentsapi.service.SandboxEmploymentsService
import uk.gov.hmrc.play.test.UnitSpec
import unit.uk.gov.hmrc.individualsemploymentsapi.util.Intervals

class SandboxEmploymentsServiceSpec extends UnitSpec with Intervals {

  val sandboxEmploymentsService = new SandboxEmploymentsService

  "Sandbox employments service paye function" should {

    "return employments for the entire available history ordered by date descending" in {
      await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-01-01", "2017-03-01"))) shouldBe Seq(Employments.disney, Employments.acme)
    }

    "return employments for a limited period" in {
      await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-01-01", "2016-07-01"))) shouldBe List(Employments.acme)
    }

    "return correct employments when range includes a period of no payments" in {
      await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-04-30", "2017-02-15"))) shouldBe Seq(Employments.disney, Employments.acme)
    }

    "return no employments when the individual has no employment for a given period" in {
      await(sandboxEmploymentsService.paye(sandboxMatchId, toInterval("2016-08-01", "2016-09-01"))) shouldBe List.empty
    }

    "throw not found exception when no individual exists for the given matchId" in {
      a[MatchNotFoundException] should be thrownBy {
        await(sandboxEmploymentsService.paye(UUID.randomUUID(), toInterval("2016-01-01", "2018-03-01")))
      }
    }

  }

}
