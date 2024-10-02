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

package unit.uk.gov.hmrc.individualsemploymentsapi.connector

import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.IndividualsMatchingApiConnector
import uk.gov.hmrc.individualsemploymentsapi.domain
import unit.uk.gov.hmrc.individualsemploymentsapi.util.{ConnectorSupport, UnitSpec, WireMockMethods}

import java.util.UUID

class IndividualsMatchingApiConnectorSpec
    extends UnitSpec with ConnectorSupport with WireMockMethods with BeforeAndAfterEach {
  override def serviceId: String = "individuals-matching-api"

  trait Fixture {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    protected val individualsMatchingApiConnector: IndividualsMatchingApiConnector =
      app.injector.instanceOf[IndividualsMatchingApiConnector]
  }

  "Matching API connector resolve function should" should {
    val matchId = UUID.randomUUID()

    def stubWithResponseStatus(responseStatus: Int, body: String = ""): Unit =
      when(GET, s"/match-record/$matchId").thenReturn(responseStatus, body)

    "fail when upstream service fails" in new Fixture {
      stubWithResponseStatus(INTERNAL_SERVER_ERROR)
      a[UpstreamErrorResponse] should be thrownBy {
        await(individualsMatchingApiConnector.resolve(matchId))
      }
    }

    "return a nino match when upstream service call succeeds" in new Fixture {
      stubWithResponseStatus(
        OK,
        s"""
          {
            "matchId":"${matchId.toString}",
            "nino":"AB123456C"
          }
        """
      )
      await(individualsMatchingApiConnector.resolve(matchId)) shouldBe domain.NinoMatch(matchId, Nino("AB123456C"))
    }
  }
}
