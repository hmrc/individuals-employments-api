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
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmploymentsService @Inject()(
  individualsMatchingApiConnector: IndividualsMatchingApiConnector,
  ifConnector: IfConnector,
  scopesHelper: ScopesHelper,
  scopesService: ScopesService,
  @Named("retryDelay") retryDelay: Int,
  cacheService: CacheService)(implicit val ec: ExecutionContext) {

  private implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  private val endpoints = List("paye")

  private def sortByLeavingDateOrLastPaymentDate(interval: Interval) = { e: Employment =>
    e.endDate.getOrElse(interval.getEnd.toLocalDate)
  }

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    individualsMatchingApiConnector.resolve(matchId)

  def paye(matchId: UUID, interval: Interval, payeReference: Option[String], endpoint: String, scopes: Iterable[String])
                   (implicit hc: HeaderCarrier, request: RequestHeader): Future[Seq[Employment]] =
    resolve(matchId).flatMap {
      ninoMatch =>
        val params = payeReference.map(s => ("payeReference", s)).toMap
        val fieldsQuery       = scopesHelper.getParameterisedQueryStringFor(scopes.toList, endpoint, params)
        val fieldKeys         = scopesService.getValidFieldsForCacheKey(scopes.toList, endpoints)
        cacheService
          .get(
            cacheId = CacheId(matchId, interval, fieldKeys, payeReference),
            fallbackFunction = withRetry {
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
