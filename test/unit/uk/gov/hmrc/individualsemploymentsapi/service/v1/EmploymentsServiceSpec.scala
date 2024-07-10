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

package unit.uk.gov.hmrc.individualsemploymentsapi.service.v1

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Format
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{DesConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.domain
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequencyCode.{DesPayFrequency, M1}
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment, DesPayment}
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.Employment
import uk.gov.hmrc.individualsemploymentsapi.service.v1.{CacheService, LiveEmploymentsService}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.Intervals

import java.time.LocalDate
import java.time.LocalDate.parse
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class EmploymentsServiceSpec extends SpecBase with Intervals with MockitoSugar with BeforeAndAfterEach {

  trait Fixture {
    val individualsMatchingApiConnector: IndividualsMatchingApiConnector = mock[IndividualsMatchingApiConnector]
    val desConnector: DesConnector = mock[DesConnector]
    // can't mock function with by-value argument
    private val stubCache = new CacheService(null, null)(null) {
      override def get[T: Format](cacheId: String, functionToCache: => Future[T]): Future[T] =
        functionToCache
    }
    val liveEmploymentsService = new LiveEmploymentsService(individualsMatchingApiConnector, desConnector, 0, stubCache)
  }

  private val matchId = UUID.randomUUID()
  private val nino = Nino("AB123456C")
  private val ninoMatch = domain.NinoMatch(matchId, nino)
  private val interval = toInterval("2016-01-01", "2017-03-01")

  implicit val hc: HeaderCarrier = new HeaderCarrier

  "Live Employments Service paye function based on match id" should {

    "return empty list of employments when no employments exists for the given matchId" in new Fixture {
      when(individualsMatchingApiConnector.resolve(matchId)).thenReturn(successful(ninoMatch))
      when(desConnector.fetchEmployments(nino, interval)).thenReturn(successful(Seq.empty))
      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq.empty
    }

    "return list of employments sorted by payment date when employments exists for the given matchId" in new Fixture {
      private val employmentEndingJanuary = aDesEmployment(leavingDate = Some(parse("2017-01-28")))
      private val employmentEndingMarch = aDesEmployment(leavingDate = Some(parse("2017-03-28")))
      private val employmentWithLastPaymentInFebruary =
        aDesEmployment(
          leavingDate = None,
          payments = Seq(DesPayment(parse("2016-12-28"), 10), DesPayment(parse("2017-02-28"), 10))
        )

      when(individualsMatchingApiConnector.resolve(matchId)).thenReturn(successful(ninoMatch))
      when(desConnector.fetchEmployments(nino, interval))
        .thenReturn(
          successful(Seq(employmentEndingJanuary, employmentEndingMarch, employmentWithLastPaymentInFebruary))
        )

      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq(
        Employment.from(employmentEndingMarch).get,
        Employment.from(employmentWithLastPaymentInFebruary).get,
        Employment.from(employmentEndingJanuary).get
      )
    }

    "return the employments sorted by last payment date when an employment exists with no payments" in new Fixture {
      private val anEmployment = aDesEmployment(leavingDate = Some(LocalDate.parse("2017-01-01")))
      private val employmentWithNoPayments = aDesEmployment(leavingDate = None, payments = Nil)

      when(individualsMatchingApiConnector.resolve(matchId)).thenReturn(Future.successful(ninoMatch))
      when(desConnector.fetchEmployments(nino, interval))
        .thenReturn(Future.successful(Seq(anEmployment, employmentWithNoPayments)))

      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq(
        Employment.from(employmentWithNoPayments).get,
        Employment.from(anEmployment).get
      )
    }

    "retry once if the employments lookup returns a 503" in new Fixture {
      private val someEmployment = aDesEmployment()

      when(individualsMatchingApiConnector.resolve(matchId)).thenReturn(Future.successful(ninoMatch))

      when(desConnector.fetchEmployments(nino, interval))
        .thenReturn(Future.failed(UpstreamErrorResponse("""¯\_(ツ)_/¯""", 503, 503)))
        .thenReturn(Future.successful(Seq(someEmployment)))

      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq(Employment.from(someEmployment).get)
      verify(desConnector, times(2)).fetchEmployments(any(), any())(any(), any())
    }
  }

  private def aDesEmployment(
    employerName: Option[String] = Some("Acme Inc"),
    employerAddress: Option[DesAddress] = Some(
      DesAddress(
        line1 = Some("Acme House"),
        line2 = Some("23 Acme Street"),
        line3 = Some("Richmond"),
        line4 = Some("Surrey"),
        line5 = Some("UK"),
        postalCode = Some("AI22 9LL")
      )
    ),
    districtNumber: Option[String] = Some("123"),
    schemeReference: Option[String] = Some("AI45678"),
    startDate: Option[LocalDate] = Some(parse("2016-01-01")),
    leavingDate: Option[LocalDate] = Some(parse("2020-02-29")),
    frequency: Option[DesPayFrequency] = Some(M1),
    payments: Seq[DesPayment] = Seq.empty
  ) =
    DesEmployment(
      payments,
      employerName,
      employerAddress,
      districtNumber,
      schemeReference,
      startDate,
      leavingDate,
      frequency
    )
}
