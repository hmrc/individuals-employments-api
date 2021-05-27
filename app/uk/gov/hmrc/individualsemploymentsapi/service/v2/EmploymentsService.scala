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

package uk.gov.hmrc.individualsemploymentsapi.service.v2

import java.util.UUID

import javax.inject.{Inject, Named, Singleton}
import org.joda.time.{Interval, LocalDate}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{IfConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.Individual
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.v2.SandboxData.Individuals.find
import uk.gov.hmrc.individualsemploymentsapi.sandbox.v2.SandboxData.{sandboxMatchId, sandboxNino}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.{failed, successful}

trait EmploymentsService {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def paye(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])
          (implicit hc: HeaderCarrier, request: RequestHeader): Future[Seq[Employment]]
}

@Singleton
class SandboxEmploymentsService extends EmploymentsService {

  override def resolve(matchId: UUID)
                      (implicit hc: HeaderCarrier): Future[NinoMatch] =
    if (matchId.equals(sandboxMatchId)) successful(NinoMatch(sandboxMatchId, sandboxNino))
    else failed(new MatchNotFoundException)

  override def paye(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])
                   (implicit hc: HeaderCarrier, request: RequestHeader): Future[Seq[Employment]] =
    paye(find(matchId), interval)

  private def paye(maybeIndividual: Option[Individual], interval: Interval)
                  (implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    maybeIndividual match {
      case Some(i) =>
        val employments: Seq[Option[Employment]] = i.employments.map(Employment.create)
        val filtered: Seq[Employment] = employments
          .filter(_.isDefined)
          .map(_.get)
          .filter { e =>
            e.startDate.exists(_.toDateTimeAtStartOfDay.isBefore(interval.getEnd)) &&
            e.endDate.exists(_.toDateTimeAtStartOfDay.isAfter(interval.getStart))
          }
          .sortBy(e => e.startDate.getOrElse(interval.getEnd.toLocalDate))
          .reverse
        Future.successful(filtered)
      case None => Future.failed(new MatchNotFoundException)
    }
}

@Singleton
class LiveEmploymentsService @Inject()(
  individualsMatchingApiConnector: IndividualsMatchingApiConnector,
  ifConnector: IfConnector,
  scopesHelper: ScopesHelper,
  scopeFilterVerificationService: ScopeFilterVerificationService,
  @Named("retryDelay") retryDelay: Int,
  cacheService: CacheService)(implicit val ec: ExecutionContext)
    extends EmploymentsService {

  private def sortByLeavingDateOrLastPaymentDate(interval: Interval) = { e: Employment =>
    e.endDate.getOrElse(interval.getEnd.toLocalDate)
  }

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    individualsMatchingApiConnector.resolve(matchId)

  override def paye(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])
                   (implicit hc: HeaderCarrier, request: RequestHeader): Future[Seq[Employment]] =
    resolve(matchId).flatMap {
      ninoMatch =>
        val fieldsQuery = scopeFilterVerificationService.getQueryStringForDefinedScopes(scopes.toList, endpoint, request)
        cacheService
          .get(
            cacheId = CacheId(matchId, interval, fieldsQuery),
            functionToCache = withRetry {
              ifConnector.fetchEmployments(
                ninoMatch.nino,
                interval,
                Option(fieldsQuery).filter(_.nonEmpty),
                matchId.toString
              )
            }
          )
          .map {
            _.map(Employment.create).filter(_.isDefined).map(_.get)
          }
          .map {
            _.sortBy(sortByLeavingDateOrLastPaymentDate(interval)).reverse
          }
    }

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
