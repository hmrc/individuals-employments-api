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

package uk.gov.hmrc.individualsemploymentsapi.service.v1

import java.time.{LocalDate, LocalTime}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{DesConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.domain
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesEmployment, Individual}
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.Employment
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.util.Interval
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

import java.util.UUID
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.Future.{failed, successful}
import scala.concurrent.{ExecutionContext, Future}

trait EmploymentsService {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def paye(matchId: UUID, interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]]
}

@Singleton
class SandboxEmploymentsService extends EmploymentsService {

  import uk.gov.hmrc.individualsemploymentsapi.sandbox.v1.SandboxData.Individuals.find
  import uk.gov.hmrc.individualsemploymentsapi.sandbox.v1.SandboxData._

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    if (matchId.equals(sandboxMatchId)) successful(domain.NinoMatch(sandboxMatchId, sandboxNino))
    else failed(new MatchNotFoundException)

  override def paye(matchId: UUID, interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    paye(find(matchId), interval)

  private def paye(maybeIndividual: Option[Individual], interval: Interval): Future[Seq[Employment]] =
    maybeIndividual match {
      case Some(i) =>
        val employments = i.employments.flatMap(Employment.from)
        val employmentsWithinInterval = employments.filter { e =>
          e.startDate.forall(_.atTime(LocalTime.MIN).isBefore(interval.getEnd)) &&
          e.endDate.forall(_.atTime(LocalTime.MIN).isAfter(interval.getStart))
        }

        Future.successful(employmentsWithinInterval.sortBy(_.endDate.getOrElse(interval.getEnd.toLocalDate)).reverse)
      case None => Future.failed(new MatchNotFoundException)
    }
}

@Singleton
class LiveEmploymentsService @Inject()(
  individualsMatchingApiConnector: IndividualsMatchingApiConnector,
  desConnector: DesConnector,
  @Named("retryDelay") retryDelay: Int,
  cacheService: CacheService)(implicit ec: ExecutionContext)
    extends EmploymentsService {

  private def sortByLeavingDateOrLastPaymentDate(interval: Interval) = { e: DesEmployment =>
    e.employmentLeavingDate.getOrElse(interval.getEnd.toLocalDate)
  }

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    individualsMatchingApiConnector.resolve(matchId)

  override def paye(matchId: UUID, interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    resolve(matchId).flatMap { ninoMatch =>
      cacheService
        .get(s"$matchId-${interval.getStart}-${interval.getEnd}", withRetry {
          desConnector.fetchEmployments(ninoMatch.nino, interval)
        })
        .map { employments =>
          employments.sortBy(sortByLeavingDateOrLastPaymentDate(interval)).reverse flatMap Employment.from
        }
    }

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case UpstreamErrorResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
