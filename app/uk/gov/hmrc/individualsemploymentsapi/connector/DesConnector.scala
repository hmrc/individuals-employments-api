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

package uk.gov.hmrc.individualsemploymentsapi.connector

import javax.inject.{Inject, Singleton}
import org.joda.time.Interval
import play.api.Configuration
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.individualsemploymentsapi.config.ConfigSupport
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesEmployment, DesEmployments}
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject()(override val config: Configuration, http: HttpClient) extends ServicesConfig with ConfigSupport {

  private val serviceUrl = baseUrl("des")
  private val desBearerToken = config.getString("microservice.services.des.authorization-token") getOrElse (throw new RuntimeException("DES authorization token must be defined"))
  private val desEnvironment = config.getString("microservice.services.des.environment") getOrElse (throw new RuntimeException("DES environment must be defined"))

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
