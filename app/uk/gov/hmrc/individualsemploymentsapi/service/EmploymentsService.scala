/*
 * Copyright 2017 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}

import org.joda.time.{Interval, LocalDate}
import uk.gov.hmrc.individualsemploymentsapi.connector.IndividualsMatchingApiConnector
import uk.gov.hmrc.individualsemploymentsapi.domain.{Employment, Individual, NinoMatch}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

trait EmploymentsService {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def paye(matchId: UUID, interval: Interval): Future[Seq[Employment]]

}

@Singleton
class SandboxEmploymentsService extends EmploymentsService {

  import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.Individuals.find
  import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData._

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier) = if (matchId.equals(sandboxMatchId)) successful(NinoMatch(sandboxMatchId, sandboxNino)) else failed(new MatchNotFoundException)

  override def paye(matchId: UUID, interval: Interval) = paye(find(matchId), interval)

  private def paye(maybeIndividual: Option[Individual], interval: Interval): Future[Seq[Employment]] = {

    def lastEmploymentPaymentDate(individual: Individual, employment: Employment): LocalDate =
      individual.income.filter(p => p.employerPayeReference == employment.employer.flatMap(_.payeReference)).map(_.paymentDate).max

    maybeIndividual match {
      case Some(individual) =>
        val employerPayeReferences = individual.income.filter(_.isPaidWithin(interval)) map (_.employerPayeReference)
        val employments = individual.employments filter (e => employerPayeReferences.contains(e.employer flatMap (_.payeReference)))
        successful(employments.sortBy(e => e.endDate getOrElse lastEmploymentPaymentDate(individual, e)).reverse)
      case None => failed(new MatchNotFoundException)
    }
  }

}

@Singleton
class LiveEmploymentsService @Inject()(individualsMatchingApiConnector: IndividualsMatchingApiConnector) extends EmploymentsService {

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier) = individualsMatchingApiConnector.resolve(matchId)

  override def paye(matchId: UUID, interval: Interval): Future[Seq[Employment]] = throw new UnsupportedOperationException

}
