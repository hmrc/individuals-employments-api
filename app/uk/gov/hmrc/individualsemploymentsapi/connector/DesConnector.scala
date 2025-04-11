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

package uk.gov.hmrc.individualsemploymentsapi.connector

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesEmployment, DesEmployments}
import uk.gov.hmrc.individualsemploymentsapi.util.Interval
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject() (servicesConfig: ServicesConfig, http: HttpClientV2) {

  val logger: Logger = Logger(getClass)

  private val serviceUrl = servicesConfig.baseUrl("des")
  private val desBearerToken = servicesConfig.getString("microservice.services.des.authorization-token")
  private val desEnvironment = servicesConfig.getString("microservice.services.des.environment")

  def fetchEmployments(nino: Nino, interval: Interval)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[DesEmployment]] = {

    val fromDate = interval.getStart.toLocalDate
    val toDate = interval.getEnd.toLocalDate
    val employmentsUrl = s"$serviceUrl/individuals/nino/$nino/employments/income?from=$fromDate&to=$toDate"

    http
      .get(url"$employmentsUrl")
      .setHeader(HeaderNames.authorisation -> s"Bearer $desBearerToken")
      .setHeader("Environment" -> desEnvironment)
      .setHeader("Source" -> "MDTP")
      .execute[DesEmployments]
      .map(_.employments)
      .recoverWith {
        case UpstreamErrorResponse(_, 404, _, _) => Future.successful(Seq.empty)
        case UpstreamErrorResponse(msg, 429, _, _) =>
          logger.warn(s"DES Rate limited: $msg")
          Future.failed(new TooManyRequestException(msg))
      }
  }
}
