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

package unit.uk.gov.hmrc.individualsemploymentsapi.service.v1

import org.joda.time.LocalDate
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesEmployment, DesEmployments, DesPayment}

class DesEmploymentSpec extends AnyWordSpec with Matchers {

  "Des employments" should {
    "map over correctly to employments" in {
      val payments: Seq[DesPayment] = Seq(
        DesPayment(LocalDate.parse("2018-12-1"),11.11, Some(2), Some(3))
      )
      val desEmployment = DesEmployment(payments)

      val result = DesEmployments.toPayments(desEmployment)

      val first = result.head
      first.paymentDate shouldBe LocalDate.parse("2018-12-1")
      first.taxablePayment shouldBe 11.11
      first.weekPayNumber shouldBe Some(2)
      first.monthPayNumber shouldBe Some(3)
    }
  }

}
