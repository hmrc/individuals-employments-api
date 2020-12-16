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

package unit.uk.gov.hmrc.individualsemploymentsapi.service

import java.util.UUID

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Format
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.{DesConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.domain._
import uk.gov.hmrc.individualsemploymentsapi.domain.des.DesPayFrequency.{DesPayFrequency, M1}
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment, DesPayment}
import uk.gov.hmrc.individualsemploymentsapi.service.v1.{CacheService, LiveEmploymentsService}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.Intervals
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class LiveEmploymentsServiceSpec extends SpecBase with Intervals with MockitoSugar with BeforeAndAfterEach {

  private val individualsMatchingApiConnector =
    mock[IndividualsMatchingApiConnector]
  private val desConnector = mock[DesConnector]

  // can't mock function with by-value argument
  private val stubCache = new CacheService(null, null)(null) {
    override def get[T: Format](cacheId: String, functionToCache: => Future[T])(implicit hc: HeaderCarrier) =
      functionToCache
  }

  private val liveEmploymentsService =
    new LiveEmploymentsService(individualsMatchingApiConnector, desConnector, 0, stubCache)

  private val matchId = UUID.randomUUID()
  private val nino = Nino("AB123456C")
  private val ninoMatch = NinoMatch(matchId, nino)
  private val interval = toInterval("2016-01-01", "2017-03-01")

  implicit val hc: HeaderCarrier = new HeaderCarrier

  override def beforeEach() {
    Mockito.reset(individualsMatchingApiConnector, desConnector)
  }

  "Live Employments Service paye function based on match id" should {

    "return empty list of employments when no employments exists for the given matchId" in {
      mockIndividualsMatchingApiConnectorToReturn(successful(ninoMatch))
      mockDesConnectorToReturn(successful(Seq.empty))
      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq.empty
    }

    "return list of employments sorted by payment date when employments exists for the given matchId" in {
      val employmentEndingJanuary =
        aDesEmployment(leavingDate = Some(parse("2017-01-28")))
      val employmentEndingMarch =
        aDesEmployment(leavingDate = Some(parse("2017-03-28")))
      val employmentWithLastPaymentInFebruary =
        aDesEmployment(
          leavingDate = None,
          payments = Seq(DesPayment(parse("2016-12-28"), 10), DesPayment(parse("2017-02-28"), 10)))

      mockIndividualsMatchingApiConnectorToReturn(successful(ninoMatch))
      mockDesConnectorToReturn(
        successful(Seq(employmentEndingJanuary, employmentEndingMarch, employmentWithLastPaymentInFebruary)))

      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq(
        Employment.from(employmentEndingMarch).get,
        Employment.from(employmentWithLastPaymentInFebruary).get,
        Employment.from(employmentEndingJanuary).get)
    }

    "return the employments sorted by last payment date when an employment exists with no payments" in {
      val anEmployment =
        aDesEmployment(leavingDate = Some(LocalDate.parse("2017-01-01")))
      val employmentWithNoPayments =
        aDesEmployment(leavingDate = None, payments = Nil)

      mockIndividualsMatchingApiConnectorToReturn(Future.successful(ninoMatch))
      mockDesConnectorToReturn(Future.successful(Seq(anEmployment, employmentWithNoPayments)))

      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq(
        Employment.from(employmentWithNoPayments).get,
        Employment.from(anEmployment).get
      )
    }

    "retry once if the employments lookup returns a 503" in {
      val someEmployment = aDesEmployment()

      mockIndividualsMatchingApiConnectorToReturn(Future.successful(ninoMatch))

      when(desConnector.fetchEmployments(nino, interval))
        .thenReturn(Future.failed(Upstream5xxResponse("""¯\_(ツ)_/¯""", 503, 503)))
        .thenReturn(Future.successful(Seq(someEmployment)))

      await(liveEmploymentsService.paye(matchId, interval)) shouldBe Seq(Employment.from(someEmployment).get)
      verify(desConnector, times(2)).fetchEmployments(any(), any())(any(), any())
    }

  }

  private def mockIndividualsMatchingApiConnectorToReturn(eventualNinoMatch: Future[NinoMatch]) =
    when(individualsMatchingApiConnector.resolve(matchId))
      .thenReturn(eventualNinoMatch)

  private def mockDesConnectorToReturn(eventualDesEmployments: Future[Seq[DesEmployment]]) =
    when(desConnector.fetchEmployments(nino, interval))
      .thenReturn(eventualDesEmployments)

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
      )),
    districtNumber: Option[String] = Some("123"),
    schemeReference: Option[String] = Some("AI45678"),
    startDate: Option[LocalDate] = Some(parse("2016-01-01")),
    leavingDate: Option[LocalDate] = Some(parse("2020-02-29")),
    frequency: Option[DesPayFrequency] = Some(M1),
    payments: Seq[DesPayment] = Seq.empty) =
    DesEmployment(
      payments,
      employerName,
      employerAddress,
      districtNumber,
      schemeReference,
      startDate,
      leavingDate,
      frequency)

  private def anEmployer(
    payeReference: String = "123/AI45678",
    name: Option[String] = Some("Acme Inc"),
    address: Option[Address] = anAddress()) =
    Employer(Some(EmpRef.fromIdentifiers(payeReference)), name, address)

  private def anAddress(
    line1: Option[String] = Some("Acme House"),
    line2: Option[String] = Some("23 Acme Street"),
    line3: Option[String] = Some("Richmond"),
    line4: Option[String] = Some("Surrey"),
    line5: Option[String] = Some("UK"),
    postcode: Option[String] = Some("AI22 9LL")): Option[Address] =
    Some(Address(line1, line2, line3, line4, line5, postcode))
}
