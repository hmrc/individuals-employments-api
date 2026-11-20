/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.connector

import play.api.mvc.RequestHeader
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.config.AppConfig
import uk.gov.hmrc.individualsemploymentsapi.domain.ip.IpEmployment
import uk.gov.hmrc.individualsemploymentsapi.util.Interval

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IntegrationPlatformRouter @Inject() (config: AppConfig, desConnector: DesConnector, hipConnector: HipConnector) {

  def fetchEmployments(nino: Nino, interval: Interval)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[Seq[IpEmployment]] =
    if (config.useHipApi) hipConnector.fetchEmployments(nino, interval)
    else
      desConnector.fetchEmployments(nino, interval)
}
