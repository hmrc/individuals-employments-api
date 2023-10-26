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

package unit.uk.gov.hmrc.individualsemploymentsapi.controller.v2

import org.joda.time.{Interval, LocalDate}
import org.mockito.ArgumentMatchers.{any, refEq, eq => eqTo}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments, InsufficientEnrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.AuditHelper
import uk.gov.hmrc.individualsemploymentsapi.controller.v2.EmploymentsController
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.{IfAddress, IfEmployer, IfEmployment, IfEmploymentDetail}
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment
import uk.gov.hmrc.individualsemploymentsapi.domain.{NinoMatch, PayFrequencyCode}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{EmploymentsService, ScopesHelper, ScopesService}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.AuthHelper

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class EmploymentsControllerSpec extends SpecBase with AuthHelper with MockitoSugar {

  protected val ifEmploymentExample = IfEmployment(
    employer = Some(
      IfEmployer(
        name = Some("Acme"),
        address = Some(IfAddress(
          line1 = Some("Acme Inc Building"),
          line2 = Some("Acme Inc Campus"),
          line3 = Some("Acme Street"),
          line4 = Some("AcmeVille"),
          line5 = Some("Acme State"),
          postcode = Some("AI22 9LL")
        ))
      )),
    employment = Some(
      IfEmploymentDetail(
        startDate = Some(new LocalDate(2016, 1, 1).toString()),
        endDate = Some(new LocalDate(2016, 6, 30).toString()),
        payFrequency = Some(PayFrequencyCode.W4.toString),
        payrollId = Some("payroll-id"),
        address = Some(
          IfAddress(
            line1 = Some("Employment House"),
            line2 = Some("Employment Street"),
            line3 = Some("Employment Town"),
            line4 = None,
            line5 = None,
            postcode = Some("AA11 1AA")
          ))
      )
    ),
    payments = None,
    employerRef = Some("247/A1987CB")
  )

  val sampleMatchIdString = "57072660-1df9-4aeb-b4ea-cd2d7f96e430"
  val sampleMatchId = UUID.fromString(sampleMatchIdString)

  trait Setup extends ScopesConfigHelper {

    val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val validCorrelationHeader = ("CorrelationId", sampleCorrelationId)

    val controllerComponent = fakeApplication.injector.instanceOf[ControllerComponents]
    val mockEmploymentsService = mock[EmploymentsService]

    implicit lazy val ec = fakeApplication.injector.instanceOf[ExecutionContext]
    lazy val scopeService: ScopesService = new ScopesService(mockScopesConfig)
    lazy val scopesHelper: ScopesHelper = new ScopesHelper(scopeService)
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val employmentsController = new EmploymentsController(
      mockEmploymentsService,
      scopeService,
      scopesHelper,
      mockAuthConnector,
      auditHelper,
      controllerComponent)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    given(mockAuthConnector.authorise(eqTo(Enrolment("test-scope")), refEq(Retrievals.allEnrolments))(any(), any()))
      .willReturn(Future.successful(Enrolments(Set(Enrolment("test-scope")))))
  }

  "Root" should {
    val randomMatchId = UUID.randomUUID()

    "return a 404 (not found) when a match id does not match live data" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult =
        employmentsController.root(randomMatchId.toString)(FakeRequest().withHeaders(validCorrelationHeader))

      status(eventualResult) shouldBe NOT_FOUND
      contentAsJson(eventualResult) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )

      verify(employmentsController.auditHelper, times(1)).auditApiFailure(any(), any(), any(), any(), any())(any())
    }

    "Return an invalid request when missing a CorrelationId" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult =
        employmentsController.root(randomMatchId.toString)(FakeRequest())

      status(eventualResult) shouldBe BAD_REQUEST
      contentAsJson(eventualResult) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )

      verify(employmentsController.auditHelper, times(1))
        .auditApiFailure(any(), any(), any(), any(), any())(any())

    }

    "Return an invalid request with an invalid CorrelationId" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult =
        employmentsController.root(randomMatchId.toString)(FakeRequest().withHeaders("CorrelationId" -> "FOO"))

      status(eventualResult) shouldBe BAD_REQUEST
      contentAsJson(eventualResult) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )

      verify(employmentsController.auditHelper, times(1))
        .auditApiFailure(any(), any(), any(), any(), any())(any())

    }

    "return a 200 (ok) when a match id matches live data" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(NinoMatch(randomMatchId, Nino("AB123456C"))))

      val eventualResult =
        employmentsController.root(randomMatchId.toString)(FakeRequest().withHeaders(validCorrelationHeader))

      status(eventualResult) shouldBe OK
      contentAsJson(eventualResult) shouldBe Json.obj(
        "_links" -> Json.obj(
          "paye" -> Json.obj(
            "href"  -> s"/individuals/employments/paye?matchId=$randomMatchId{&startDate,endDate}",
            "title" -> "Get an individual's PAYE employment data"
          ),
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/?matchId=$randomMatchId"
          )
        )
      )

      verify(employmentsController.auditHelper, times(1))
        .auditApiResponse(any(), any(), any(), any(), any(), any())(any())

      verify(employmentsController.auditHelper, times(1)).auditAuthScopes(any(), any(), any())(any())
    }

    "fail with status 401 when the bearer token does not have enrolment test-scope" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(InsufficientEnrolments()))

      val result = employmentsController.root(randomMatchId.toString)(FakeRequest().withHeaders(validCorrelationHeader))

      status(result) shouldBe UNAUTHORIZED
      verifyNoInteractions(mockEmploymentsService)

      verify(employmentsController.auditHelper, times(1))
        .auditApiFailure(any(), any(), any(), any(), any())(any())
    }

    "fail with status 500 when an unknown exception is thrown" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new Exception("Test Exception")))

      val result = employmentsController.root(randomMatchId.toString)(FakeRequest().withHeaders(validCorrelationHeader))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      verifyNoInteractions(mockEmploymentsService)

      verify(employmentsController.auditHelper, times(1))
        .auditApiFailure(any(), any(), any(), any(), any())(any())
    }
  }

  "Employments controller paye function" should {

    val fromDate = new LocalDate("2017-03-02").toDateTimeAtStartOfDay
    val toDate = new LocalDate("2017-05-31").toDateTimeAtStartOfDay
    val interval = new Interval(fromDate, toDate)

    "return 404 (not found) for an invalid matchId" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      val invalidMatchId = UUID.randomUUID()

      when(mockEmploymentsService.paye(eqTo(invalidMatchId), eqTo(interval), any(), any(), any())(any(), any()))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult =
        employmentsController.paye(invalidMatchId.toString, interval, None)(
          FakeRequest().withHeaders(validCorrelationHeader))

      status(eventualResult) shouldBe NOT_FOUND

      contentAsJson(eventualResult) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )

      verify(employmentsController.auditHelper, times(1)).auditApiFailure(any(), any(), any(), any(), any())(any())
    }

    "return 200 OK" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      val matchId = UUID.randomUUID()

      when(mockEmploymentsService.paye(eqTo(matchId), eqTo(interval), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(Employment.create(ifEmploymentExample).get)))

      val res =
        employmentsController.paye(matchId.toString, interval, None)(FakeRequest().withHeaders(validCorrelationHeader))

      status(res) shouldBe OK

      contentAsJson(res) shouldBe Json.obj(
        "_links" -> Json.obj(
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/paye?matchId=$matchId&fromDate=2017-03-02"
          )
        ),
        "employments" -> Json.arr(
          Json.obj(
            "startDate"    -> "2016-01-01",
            "endDate"      -> "2016-06-30",
            "payFrequency" -> "FOUR_WEEKLY",
            "employer" -> Json.obj(
              "payeReference" -> "247/A1987CB",
              "name"          -> "Acme",
              "address" -> Json.obj(
                "line1"    -> "Acme Inc Building",
                "line2"    -> "Acme Inc Campus",
                "line3"    -> "Acme Street",
                "line4"    -> "AcmeVille",
                "line5"    -> "Acme State",
                "postcode" -> "AI22 9LL"
              )
            )
          )
        )
      )

      verify(employmentsController.auditHelper, times(1))
        .auditApiResponse(any(), any(), any(), any(), any(), any())(any())

      verify(employmentsController.auditHelper, times(1)).auditAuthScopes(any(), any(), any())(any())
    }

    "fail with status 401 when the bearer token does not have enrolment read:individuals-employments-paye" in new Setup {

      Mockito.reset(employmentsController.auditHelper)

      when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(InsufficientEnrolments()))

      val result =
        employmentsController.paye(sampleMatchId.toString, interval, None)(
          FakeRequest().withHeaders(validCorrelationHeader))

      status(result) shouldBe UNAUTHORIZED
      verifyNoInteractions(mockEmploymentsService)

      verify(employmentsController.auditHelper, times(1)).auditApiFailure(any(), any(), any(), any(), any())(any())
    }

  }
}
