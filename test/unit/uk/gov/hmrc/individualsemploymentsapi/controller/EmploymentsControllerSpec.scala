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

package unit.uk.gov.hmrc.individualsemploymentsapi.controller

import java.util.UUID

import org.joda.time.{Interval, LocalDate}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{verifyZeroInteractions, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.controller.{
  LiveEmploymentsController,
  SandboxEmploymentsController
}
import uk.gov.hmrc.individualsemploymentsapi.domain.{Employment, NinoMatch}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.{
  Employments,
  sandboxMatchId
}
import uk.gov.hmrc.individualsemploymentsapi.service.{
  LiveEmploymentsService,
  SandboxEmploymentsService
}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.{SpecBase, UnitSpec}

import scala.concurrent.Future

class EmploymentsControllerSpec extends SpecBase with MockitoSugar {

  val controllerComponent =
    fakeApplication.injector.instanceOf[ControllerComponents]

  trait Setup {
    val mockSandboxEmploymentsService = mock[SandboxEmploymentsService]
    val mockLiveEmploymentsService = mock[LiveEmploymentsService]
    val mockAuthConnector = mock[AuthConnector]
    val hmctsClientId = "hmctsClientId"

    val sandboxEmploymentsController = new SandboxEmploymentsController(
      mockSandboxEmploymentsService,
      mockAuthConnector,
      hmctsClientId,
      controllerComponent)
    val liveEmploymentsController = new LiveEmploymentsController(
      mockLiveEmploymentsService,
      mockAuthConnector,
      hmctsClientId,
      controllerComponent)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    when(mockAuthConnector.authorise(any(), eqTo(EmptyRetrieval))(any(), any()))
      .thenReturn(Future.successful(()))
  }

  "Root" should {
    val randomMatchId = UUID.randomUUID()

    "return a 404 (not found) when a match id does not match live data" in new Setup {
      when(
        mockLiveEmploymentsService.resolve(eqTo(randomMatchId))(
          any[HeaderCarrier]))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult =
        liveEmploymentsController.root(randomMatchId)(FakeRequest())

      status(eventualResult) shouldBe NOT_FOUND
      contentAsJson(eventualResult) shouldBe Json.obj(
        "code" -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    "return a 200 (ok) when a match id matches live data" in new Setup {
      when(
        mockLiveEmploymentsService.resolve(eqTo(randomMatchId))(
          any[HeaderCarrier]))
        .thenReturn(
          Future.successful(NinoMatch(randomMatchId, Nino("AB123456C"))))
      val eventualResult =
        liveEmploymentsController.root(randomMatchId)(FakeRequest())
      status(eventualResult) shouldBe OK
      contentAsJson(eventualResult) shouldBe Json.obj(
        "_links" -> Json.obj(
          "paye" -> Json.obj(
            "href" -> s"/individuals/employments/paye?matchId=$randomMatchId{&fromDate,toDate}",
            "title" -> "View individual's employments"
          ),
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/?matchId=$randomMatchId"
          )
        )
      )
    }

    "fail with status 401 when the bearer token does not have enrolment read:individuals-employments" in new Setup {
      when(
        mockAuthConnector.authorise(
          eqTo(Enrolment("read:individuals-employments")),
          eqTo(EmptyRetrieval))(any(), any()))
        .thenReturn(Future.failed(InsufficientEnrolments()))

      val result = liveEmploymentsController.root(randomMatchId)(FakeRequest())

      status(result) shouldBe UNAUTHORIZED
      verifyZeroInteractions(mockLiveEmploymentsService)
    }

    "not require bearer token authentication for sandbox" in new Setup {
      when(
        mockSandboxEmploymentsService.resolve(eqTo(randomMatchId))(
          any[HeaderCarrier]))
        .thenReturn(
          Future.successful(NinoMatch(randomMatchId, Nino("AB123456C"))))

      val result =
        sandboxEmploymentsController.root(randomMatchId)(FakeRequest())

      status(result) shouldBe OK
      verifyZeroInteractions(mockAuthConnector)
    }
  }

  "Employments controller paye function" should {

    val fromDate = new LocalDate("2017-03-02").toDateTimeAtStartOfDay
    val toDate = new LocalDate("2017-05-31").toDateTimeAtStartOfDay
    val interval = new Interval(fromDate, toDate)

    "return 404 (not found) for an invalid matchId" in new Setup {
      val invalidMatchId = UUID.randomUUID()
      when(mockLiveEmploymentsService.paye(eqTo(invalidMatchId),
                                           eqTo(interval))(any()))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult =
        liveEmploymentsController.paye(invalidMatchId, interval)(FakeRequest())
      status(eventualResult) shouldBe NOT_FOUND
      contentAsJson(eventualResult) shouldBe Json.obj(
        "code" -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    "return 200 OK with payroll ID and employee address when the X-Client-Id header is set to the HMCTS client ID" in new Setup {
      val matchId = UUID.randomUUID()

      when(
        mockLiveEmploymentsService.paye(eqTo(matchId), eqTo(interval))(any()))
        .thenReturn(
          Future.successful(Seq(Employment.from(Employments.acme).get)))

      val res = liveEmploymentsController.paye(matchId, interval)(
        FakeRequest().withHeaders("X-Client-Id" -> hmctsClientId))
      status(res) shouldBe OK

      contentAsJson(res) shouldBe Json.obj(
        "_links" -> Json.obj(
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/paye?matchId=$matchId&fromDate=2017-03-02"
          )
        ),
        "employments" -> Json.arr(
          Json.obj(
            "startDate" -> "2016-01-01",
            "endDate" -> "2016-06-30",
            "employer" -> Json.obj(
              "payeReference" -> "123/AI45678",
              "name" -> "Acme",
              "address" -> Json.obj(
                "line1" -> "Acme Inc Building",
                "line2" -> "Acme Inc Campus",
                "line3" -> "Acme Street",
                "line4" -> "AcmeVille",
                "line5" -> "Acme State",
                "postcode" -> "AI22 9LL"
              )
            ),
            "payFrequency" -> "FOUR_WEEKLY",
            "employeeAddress" -> Json.obj(
              "line1" -> "Employee's House",
              "line2" -> "Employee Street",
              "line3" -> "Employee Town",
              "postcode" -> "AA11 1AA"
            ),
            "payrollId" -> "payroll-id"
          )
        )
      )
    }

    "return 200 OK without payroll ID and employee address when the X-Client-Id header is not set to the HMCTS client ID" in new Setup {
      val matchId = UUID.randomUUID()

      when(
        mockLiveEmploymentsService.paye(eqTo(matchId), eqTo(interval))(any()))
        .thenReturn(
          Future.successful(Seq(Employment.from(Employments.acme).get)))

      val res = liveEmploymentsController.paye(matchId, interval)(
        FakeRequest().withHeaders("X-Client-Id" -> "not-hmcts"))
      status(res) shouldBe OK

      contentAsJson(res) shouldBe Json.obj(
        "_links" -> Json.obj(
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/paye?matchId=$matchId&fromDate=2017-03-02"
          )
        ),
        "employments" -> Json.arr(
          Json.obj(
            "startDate" -> "2016-01-01",
            "endDate" -> "2016-06-30",
            "employer" -> Json.obj(
              "payeReference" -> "123/AI45678",
              "name" -> "Acme",
              "address" -> Json.obj(
                "line1" -> "Acme Inc Building",
                "line2" -> "Acme Inc Campus",
                "line3" -> "Acme Street",
                "line4" -> "AcmeVille",
                "line5" -> "Acme State",
                "postcode" -> "AI22 9LL"
              )
            ),
            "payFrequency" -> "FOUR_WEEKLY"
          )
        )
      )
    }

    "return 200 OK without payroll ID and employee address when the X-Client-Id header is not set" in new Setup {
      val matchId = UUID.randomUUID()

      when(
        mockLiveEmploymentsService.paye(eqTo(matchId), eqTo(interval))(any()))
        .thenReturn(
          Future.successful(Seq(Employment.from(Employments.acme).get)))

      val res = liveEmploymentsController.paye(matchId, interval)(FakeRequest())
      status(res) shouldBe OK

      contentAsJson(res) shouldBe Json.obj(
        "_links" -> Json.obj(
          "self" -> Json.obj(
            "href" -> s"/individuals/employments/paye?matchId=$matchId&fromDate=2017-03-02"
          )
        ),
        "employments" -> Json.arr(
          Json.obj(
            "startDate" -> "2016-01-01",
            "endDate" -> "2016-06-30",
            "employer" -> Json.obj(
              "payeReference" -> "123/AI45678",
              "name" -> "Acme",
              "address" -> Json.obj(
                "line1" -> "Acme Inc Building",
                "line2" -> "Acme Inc Campus",
                "line3" -> "Acme Street",
                "line4" -> "AcmeVille",
                "line5" -> "Acme State",
                "postcode" -> "AI22 9LL"
              )
            ),
            "payFrequency" -> "FOUR_WEEKLY"
          )
        )
      )
    }

    "fail with status 401 when the bearer token does not have enrolment read:individuals-employments-paye" in new Setup {
      when(
        mockAuthConnector.authorise(
          eqTo(Enrolment("read:individuals-employments-paye")),
          eqTo(EmptyRetrieval))(any(), any()))
        .thenReturn(Future.failed(InsufficientEnrolments()))

      val result =
        liveEmploymentsController.paye(sandboxMatchId, interval)(FakeRequest())

      status(result) shouldBe UNAUTHORIZED
      verifyZeroInteractions(mockLiveEmploymentsService)
    }

    "not require bearer token authentication" in new Setup {
      when(
        mockSandboxEmploymentsService.paye(eqTo(sandboxMatchId),
                                           eqTo(interval))(any()))
        .thenReturn(
          Future.successful(Seq(Employment.from(Employments.acme),
                                Employment.from(Employments.disney)).flatten))

      val eventualResult =
        sandboxEmploymentsController.paye(sandboxMatchId, interval)(
          FakeRequest())

      status(eventualResult) shouldBe OK
      verifyZeroInteractions(mockAuthConnector)
    }

  }

}
