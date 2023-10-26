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

package unit.uk.gov.hmrc.individualsemploymentsapi.service.v2

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.individualsemploymentsapi.service.v2.CacheId
import utils.Intervals

import java.util.UUID

class CacheIdSpec extends AnyWordSpec with Matchers with Intervals {

  private val interval = toInterval("2016-01-01", "2017-03-01")
  private val uuid = UUID.fromString("fcb6218d-0f90-4c5d-bb58-6b128d30ac04");

  "Cache Id" should {
    "Generate string correctly" in {
      val id = CacheId(uuid, interval, "ABC")
      id.toString shouldBe "fcb6218d-0f90-4c5d-bb58-6b128d30ac04-2016-01-01T00:00:00.000Z-2017-03-01T00:00:00.000Z-ABC"
    }
  }
}
