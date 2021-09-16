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

package unit.uk.gov.hmrc.individualsemploymentsapi.util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.individualsemploymentsapi.util.UuidValidator

class UuidValidatorSpec extends AnyWordSpec with Matchers {

  private val invalidUuid = "0-0-0-0-0"
  private val validUuid = "a1c15e8f-b119-4121-bc08-6baf2af45099"

  "Return true on a a valid UUID" in {
    UuidValidator.validate(validUuid) shouldBe true
  }

  "Return false on invalid UUID" in {
    UuidValidator.validate(invalidUuid) shouldBe false
  }

}
