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

package unit.uk.gov.hmrc.individualsemploymentsapi.connector

import org.scalatest.matchers.must.Matchers.mustBe
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.individualsemploymentsapi.connector.DesConnector
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequencyCode
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment, DesPayment}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.{ConnectorSupport, UnitSpec, WireMockMethods}
import utils.Intervals

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class DesConnectorSpec extends UnitSpec with ConnectorSupport with WireMockMethods with Intervals {
  override def serviceId: String = "des"

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val rd: RequestHeader = FakeRequest()

    protected val underTest: DesConnector = app.injector.instanceOf[DesConnector]
  }

  private val desAddress = DesAddress(
    line1 = Some("Acme House"),
    line2 = Some("23 Acme Street"),
    line3 = Some("Richmond"),
    line4 = Some("Surrey"),
    line5 = Some("UK"),
    postalCode = Some("AI22 9LL")
  )

  private val desPayments = Seq(
    DesPayment(
      paymentDate = LocalDate.parse("2016-11-28"),
      totalPayInPeriod = 100,
      weekPayNumber = None,
      monthPayNumber = Some(8)
    ),
    DesPayment(
      paymentDate = LocalDate.parse("2016-12-06"),
      totalPayInPeriod = 50,
      weekPayNumber = Some(49),
      monthPayNumber = None
    )
  )

  private val desEmployment = DesEmployment(
    employerName = Some("Acme Inc"),
    employerAddress = Some(desAddress),
    employerDistrictNumber = Some("123"),
    employerSchemeReference = Some("AI45678"),
    employmentStartDate = Some(LocalDate.parse("2016-01-01")),
    employmentLeavingDate = Some(LocalDate.parse("2016-06-30")),
    employmentPayFrequency = Some(PayFrequencyCode.M1),
    payments = desPayments
  )

  "fetchEmployments" should {
    val nino = Nino("NA000799C")
    val fromDate = "2016-01-01"
    val toDate = "2017-03-01"
    val interval = toInterval(fromDate, toDate)

    "return the employments" in new Setup {
      val responseBody = """
             {
               "employments": [
                 {
                   "employerName":"Acme Inc",
                   "employerAddress": {
                     "line1": "Acme House",
                     "line2": "23 Acme Street",
                     "line3": "Richmond",
                     "line4": "Surrey",
                     "line5": "UK",
                     "postalCode": "AI22 9LL"
                   },
                   "employerDistrictNumber": "123",
                   "employerSchemeReference": "AI45678",
                   "employmentStartDate": "2016-01-01",
                   "employmentLeavingDate": "2016-06-30",
                   "employmentPayFrequency": "M1",
                   "payments": [
                     {
                       "paymentDate": "2016-11-28",
                       "totalPayInPeriod": 100,
                       "monthPayNumber": 8
                     },
                     {
                       "paymentDate": "2016-12-06",
                       "totalPayInPeriod": 50,
                       "weekPayNumber": 49
                     }
                   ]
                 }
               ]
             }
          """
      when(GET, s"/individuals/nino/$nino/employments/income", queryParams = Map("from" -> fromDate, "to" -> toDate))
        .thenReturn(200, responseBody)

      private val result = await(underTest.fetchEmployments(nino, interval))

      result shouldBe Seq(desEmployment)
    }

    "return an empty list when there is no employments" in new Setup {
      when(
        GET,
        s"/individuals/nino/$nino/employments/income",
        queryParams = Map("from" -> "2016-01-01", "to" -> "2017-03-01")
      ).thenReturn(404)

      private val result = await(underTest.fetchEmployments(nino, interval))

      result shouldBe Seq.empty
    }

    "fail when DES returns an error" in new Setup {
      when(GET, s"/individuals/nino/$nino/employments/income").thenReturn(500)

      intercept[UpstreamErrorResponse] {
        await(underTest.fetchEmployments(nino, interval))
      }
    }

    "extractCorrelationId return header when CorrelationId is present" in new Setup {

      val request = FakeRequest().withHeaders("X-Correlation-ID" -> "188e9400-b636-4a3b-80ba-230a8c72b92a")

      val result: Seq[(String, String)] = underTest.extractCorrelationId(request)

      result mustBe Seq("X-Correlation-ID" -> "188e9400-b636-4a3b-80ba-230a8c72b92a")
    }

    "extractCorrelationId return empty Seq when CorrelationId is missing" in new Setup {
      val request = FakeRequest()

      val result: Seq[(String, String)] = underTest.extractCorrelationId(request)

      result mustBe Seq.empty
    }
  }
}
