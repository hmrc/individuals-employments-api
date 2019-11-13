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

package uk.gov.hmrc.individualsemploymentsapi.service

import java.util.UUID

import javax.inject.{Inject, Named, Singleton}
import org.joda.time.{Interval, LocalDate}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{DesConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.controller.CustomExceptions.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.domain.des.DesEmployment
import uk.gov.hmrc.individualsemploymentsapi.domain.{Employment, Individual, NinoMatch}
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

trait EmploymentsService {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def paye(matchId: UUID, interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]]
}

@Singleton
class SandboxEmploymentsService extends EmploymentsService {

  import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.Individuals.find
  import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData._

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] = {
    if (matchId.equals(sandboxMatchId)) successful(NinoMatch(sandboxMatchId, sandboxNino)) else failed(MatchNotFoundException)
  }

  override def paye(matchId: UUID, interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]] = paye(find(matchId), interval)

  private def paye(maybeIndividual: Option[Individual], interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]] = {
    maybeIndividual match {
      case Some(i) =>
        val employments = i.employments.flatMap(Employment.from)
        val employmentsWithinInterval = employments.filter { e =>
          e.startDate.forall(d => d.toDateTimeAtStartOfDay.isBefore(interval.getEnd)) &&
            e.endDate.forall(d => d.toDateTimeAtStartOfDay.isAfter(interval.getStart))
        }

        Future.successful(employmentsWithinInterval.sortBy(_.endDate.getOrElse(interval.getEnd.toLocalDate)).reverse)
      case None => Future.failed(MatchNotFoundException)
    }
  }
}

@Singleton
class LiveEmploymentsService @Inject()(individualsMatchingApiConnector: IndividualsMatchingApiConnector,
                                       desConnector: DesConnector,
                                       @Named("retryDelay") retryDelay: Int,
                                       cacheService: CacheService) extends EmploymentsService {

  private def sortByLeavingDateOrLastPaymentDate(interval: Interval) = { e: DesEmployment =>
    e.employmentLeavingDate.getOrElse(interval.getEnd.toLocalDate)
  }

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] = individualsMatchingApiConnector.resolve(matchId)

  override def paye(matchId: UUID, interval: Interval)(implicit hc: HeaderCarrier): Future[Seq[Employment]] =
    resolve(matchId).flatMap { ninoMatch =>
      cacheService.get(s"$matchId-${interval.getStart}-${interval.getEnd}",
        withRetry {
          desConnector.fetchEmployments(ninoMatch.nino, interval)
        }
      ).map { employments =>
        employments.sortBy(sortByLeavingDateOrLastPaymentDate(interval)).reverse flatMap Employment.from
      }
    }

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, _) => Thread.sleep(retryDelay); body
  }
}
