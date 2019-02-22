/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsemploymentsapi.domain

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequency
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequency._
import uk.gov.hmrc.individualsemploymentsapi.domain.des.DesPayFrequency._

class PayFrequencySpec extends FlatSpec with Matchers {

  "Pay frequency" should "derive itself from an instance of des pay frequency" in new TableDrivenPropertyChecks {
    val fixtures = Table(
      ("des pay frequency", "hmrc pay frequency"),
      (W1, WEEKLY),
      (W2, FORTNIGHTLY),
      (W4, FOUR_WEEKLY),
      (IO, ONE_OFF),
      (IR, IRREGULAR),
      (M1, CALENDAR_MONTHLY),
      (M3, QUARTERLY),
      (M6, BI_ANNUALLY),
      (MA, ANNUALLY)
    )

    fixtures foreach { case (desPayFrequency, hmrcPayFrequency) =>
      PayFrequency.from(desPayFrequency) shouldBe Some(hmrcPayFrequency)
    }
  }

}
