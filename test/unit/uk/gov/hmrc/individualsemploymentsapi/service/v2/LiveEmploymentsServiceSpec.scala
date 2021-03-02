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

package unit.uk.gov.hmrc.individualsemploymentsapi.service.v2

import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Format
import uk.gov.hmrc.individualsemploymentsapi.connector.{IfConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{CacheIdBase, CacheService, LiveEmploymentsService, ScopesHelper}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.Intervals
import java.util.UUID

import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class LiveEmploymentsServiceSpec extends SpecBase with Intervals {

  trait Setup {

    implicit val ec = scala.concurrent.ExecutionContext.global

    val mockCacheService = new CacheService(null, null)(null) {
      override def get[T: Format](cacheId: CacheIdBase,functionToCache: => Future[T])(implicit hc: HeaderCarrier) =
        functionToCache
    }

    val mockIndividualsMatchingApiConnector = mock[IndividualsMatchingApiConnector]
    val mockIfConnector = mock[IfConnector]
    val mockScopesHelper = mock[ScopesHelper]

    val matchId = UUID.randomUUID()

    val liveEmploymentsService = new LiveEmploymentsService(
      mockIndividualsMatchingApiConnector,
      mockIfConnector,
      mockScopesHelper,
      retryDelay = 0,
      mockCacheService
    )
  }

}
