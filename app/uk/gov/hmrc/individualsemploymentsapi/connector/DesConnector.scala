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

package uk.gov.hmrc.individualsemploymentsapi.connector

import javax.inject.{Inject, Singleton}
import org.joda.time.Interval
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesEmployment, DesEmployments}
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject()(servicesConfig: ServicesConfig, http: HttpClient) {

  private val serviceUrl =servicesConfig. baseUrl("des")
  private val desBearerToken = servicesConfig.getString("microservice.services.des.authorization-token")
  private val desEnvironment = servicesConfig.getString("microservice.services.des.environment")

  def fetchEmployments(nino: Nino, interval: Interval)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[DesEmployment]] = {
    val fromDate = interval.getStart.toLocalDate
    val toDate = interval.getEnd.toLocalDate
    val header = hc.copy(authorization = Some(Authorization(s"Bearer $desBearerToken"))).withExtraHeaders("Environment" -> desEnvironment, "Source" -> "MDTP")

    val employmentsUrl = s"$serviceUrl/individuals/nino/$nino/employments/income?from=$fromDate&to=$toDate"

    http.GET[DesEmployments](employmentsUrl)(implicitly, header, ec).map(_.employments).recoverWith {
      case _: NotFoundException => Future.successful(Seq.empty)
      case Upstream5xxResponse(msg, 503, _) if msg.contains("LTM000503") /*DES's Magic error code*/ =>
        Future.failed(new TooManyRequestException(msg))
    }
  }
}
