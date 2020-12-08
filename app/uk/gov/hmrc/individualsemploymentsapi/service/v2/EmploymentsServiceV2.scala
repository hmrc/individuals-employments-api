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

import java.util.UUID

import javax.inject.{Inject, Named, Singleton}
import org.joda.time.{Interval, LocalDate}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{IfConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.Individual
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployment
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.v2.SandboxData.Individuals.find
import uk.gov.hmrc.individualsemploymentsapi.sandbox.v2.SandboxData.{sandboxMatchId, sandboxNino}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

trait EmploymentsServiceV2 {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def paye(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])(
    implicit hc: HeaderCarrier): Future[Seq[Employment]]
}

@Singleton
class SandboxEmploymentsServiceV2 extends EmploymentsServiceV2 {

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    if (matchId.equals(sandboxMatchId)) successful(NinoMatch(sandboxMatchId, sandboxNino))
    else failed(new MatchNotFoundException)

  override def paye(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])(
    implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    paye(find(matchId), interval)

  private def paye(maybeIndividual: Option[Individual], interval: Interval)(
    implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    maybeIndividual match {
      case Some(i) =>
        val employments: Seq[Option[Employment]] = i.employments.map(Employment.create)
        val filtered: Seq[Employment] = employments
          .filter(_.isDefined)
          .map(_.get)
          .filter { e =>
            e.employment.exists(x => x.startDate.forall(d => d.toDateTimeAtStartOfDay.isBefore(interval.getEnd))) &&
            e.employment.exists(x => x.endDate.forall(d => d.toDateTimeAtStartOfDay.isAfter(interval.getStart)))
          }
          .sortBy(e => e.employment.flatMap(d => d.startDate).getOrElse(interval.getEnd.toLocalDate))
          .reverse
        Future.successful(filtered)
      case None => Future.failed(new MatchNotFoundException)
    }
}

@Singleton
class LiveEmploymentsServiceV2 @Inject()(
  individualsMatchingApiConnector: IndividualsMatchingApiConnector,
  ifConnector: IfConnector,
  scopesHelper: ScopesHelper,
  @Named("retryDelay") retryDelay: Int,
  cacheService: CacheServiceV2)
    extends EmploymentsServiceV2 {

  private def sortByLeavingDateOrLastPaymentDate(interval: Interval) = { e: Employment =>
    e.employment.flatMap(d => d.endDate).getOrElse(interval.getEnd.toLocalDate)
  }

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    individualsMatchingApiConnector.resolve(matchId)

  override def paye(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])(
    implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    resolve(matchId).flatMap { ninoMatch =>
      cacheService
        .get(
          cacheId = s"$matchId-${interval.getStart}-${interval.getEnd}",
          functionToCache = withRetry {
            ifConnector
              .fetchEmployments(
                ninoMatch.nino,
                interval,
                Option(scopesHelper.getQueryStringFor(scopes, endpoint)).filter(_.nonEmpty))
          }
        )
        .map { _.map(Employment.create).filter(_.isDefined).map(_.get) }
        .map { _.sortBy(sortByLeavingDateOrLastPaymentDate(interval)).reverse }
    }

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
