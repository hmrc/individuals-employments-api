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

import play.api.Logger
import play.api.mvc.RequestHeader
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.individualsemploymentsapi.domain.ip.{IpEmployment, IpEmployments}
import uk.gov.hmrc.individualsemploymentsapi.util.Interval
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.{Base64, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait IntegrationPlatformConnector(http: HttpClientV2) {

  val logger: Logger = Logger(getClass)

  def setHeaders(): Seq[(String, String)]

  def serviceUrl: String

  def extractCorrelationId: RequestHeader => Seq[(String, String)]

  def fetchEmployments(nino: Nino, interval: Interval)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[Seq[IpEmployment]] = {

    val fromDate = interval.getStart.toLocalDate
    val toDate = interval.getEnd.toLocalDate
    val employmentsUrl = s"$serviceUrl/individuals/nino/$nino/employments/income?from=$fromDate&to=$toDate"

    http
      .get(url"$employmentsUrl")
      .transform(_.addHttpHeaders((setHeaders() ++ extractCorrelationId(request))*))
      .execute[IpEmployments]
      .map(_.employments)
      .recoverWith {
        case UpstreamErrorResponse(_, 404, _, _) => Future.successful(Seq.empty)
        case UpstreamErrorResponse(msg, 429, _, _) =>
          logger.warn(s"Integration Platform Rate limited: $msg")
          Future.failed(new TooManyRequestException(msg))
      }
  }
}

@Singleton
class HipConnector @Inject() (sc: ServicesConfig, http: HttpClientV2) extends IntegrationPlatformConnector(http) {
  override def setHeaders(): Seq[(String, String)] = Seq(
    "Authorization" ->
      s"""Basic ${Base64.getEncoder.encodeToString(
          s"${sc.getConfString("hip.clientId", "empty")}:${sc.getConfString("hip.clientSecret", "empty")}"
            .getBytes("UTF-8")
        )}""",
    "X-Receipt-Date"        -> DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS)),
    "X-Transmitting-System" -> "HIP"
  )

  override def serviceUrl: String = sc.baseUrl("hip")

  override def extractCorrelationId: RequestHeader => Seq[(String, String)] =
    val h1 = "correlationId"
    val h2 = "X-Correlation-ID"
    val getHeader = (req: RequestHeader) =>
      req.headers.toMap.keys.find(h => h == h1) orElse
        req.headers.toMap.keys.find(h => h == h2)
    req =>
      getHeader(req)
        .map(h => req.headers.get(h))
        .fold(Seq(h1 -> UUID.randomUUID().toString))(id => Seq(h1 -> id.get))
}

@Singleton
class DesConnector @Inject() (servicesConfig: ServicesConfig, http: HttpClientV2)
    extends IntegrationPlatformConnector(http) {

  private val desBearerToken = servicesConfig.getString("microservice.services.des.authorization-token")
  private val desEnvironment = servicesConfig.getString("microservice.services.des.environment")

  def setHeaders() = Seq(
    HeaderNames.authorisation -> s"Bearer $desBearerToken",
    "Environment"             -> desEnvironment,
    "Source"                  -> "MDTP"
  )

  override def serviceUrl: String = servicesConfig.baseUrl("des")

  override def extractCorrelationId: RequestHeader => Seq[(String, String)] =
    val h = "X-Correlation-ID"
    req =>
      req.headers
        .get(h)
        .fold(Seq.empty)(id => Seq(h -> id))

}
