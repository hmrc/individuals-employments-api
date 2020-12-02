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

package uk.gov.hmrc.individualsemploymentsapi.service.v2

import javax.inject.{Inject, Named, Singleton}
import uk.gov.hmrc.http.{Upstream5xxResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{DesConnector, IndividualsMatchingApiConnector}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait EmploymentsServiceV2 {}

@Singleton
class SandboxEmploymentsServiceV2 extends EmploymentsServiceV2 {

  // TODO - to implement when we wire up the endpoints (See V1 for reference)

}

@Singleton
class LiveEmploymentsServiceV2 @Inject()(
  individualsMatchingApiConnector: IndividualsMatchingApiConnector,
  ifConnector: DesConnector, // TODO - replace with IfConnector
  @Named("retryDelay") retryDelay: Int,
  cacheService: CacheServiceV2)
    extends EmploymentsServiceV2 {

  // TODO - to implement when we wire up the endpoints (See V1 for reference)

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
