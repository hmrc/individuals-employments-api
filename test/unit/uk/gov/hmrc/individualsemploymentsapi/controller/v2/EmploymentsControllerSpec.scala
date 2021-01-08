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

package unit.uk.gov.hmrc.individualsemploymentsapi.controller.v2

import java.util.UUID

import org.joda.time.{Interval, LocalDate}
import org.mockito.BDDMockito.`given`
import play.api.test.Helpers._
import org.mockito.Matchers.{any, refEq, eq => eqTo}
import org.mockito.Mockito.{verifyZeroInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments, InsufficientEnrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.controller.v2.{LiveEmploymentsController, SandboxEmploymentsController}
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.v2.SandboxData._
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{LiveEmploymentsService, SandboxEmploymentsService, ScopesHelper, ScopesService}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.AuthHelper

import scala.concurrent.{ExecutionContext, Future}

class EmploymentsControllerSpec extends SpecBase with AuthHelper with MockitoSugar {

  trait Setup extends ScopesConfigHelper {

    val controllerComponent = fakeApplication.injector.instanceOf[ControllerComponents]
    val mockSandboxEmploymentsService = mock[SandboxEmploymentsService]
    val mockLiveEmploymentsService = mock[LiveEmploymentsService]

    implicit lazy val ec = fakeApplication.injector.instanceOf[ExecutionContext]
    lazy val scopeService: ScopesService = new ScopesService(mockScopesConfig)
    lazy val scopesHelper: ScopesHelper = new ScopesHelper(scopeService)
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val hmctsClientId = "hmctsClientId"

    val sandboxEmploymentsController = new SandboxEmploymentsController(
      mockSandboxEmploymentsService,
      scopeService,
      scopesHelper,
      mockAuthConnector,
      hmctsClientId,
      controllerComponent)

    val liveEmploymentsController = new LiveEmploymentsController(
      mockLiveEmploymentsService,
      scopeService,
      scopesHelper,
      mockAuthConnector,
      hmctsClientId,
      controllerComponent)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    given(mockAuthConnector.authorise(eqTo(Enrolment("test-scope")), refEq(Retrievals.allEnrolments))(any(), any()))
      .willReturn(Future.successful(Enrolments(Set(Enrolment("test-scope")))))
  }

  "Root" should {
    val randomMatchId = UUID.randomUUID()

    "return a 404 (not found) when a match id does not match live data" in new Setup {

      when(mockLiveEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult = liveEmploymentsController.root(randomMatchId)(FakeRequest())

      status(eventualResult) shouldBe NOT_FOUND
      contentAsJson(eventualResult) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    "return a 200 (ok) when a match id matches live data" in new Setup {

      when(mockLiveEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(NinoMatch(randomMatchId, Nino("AB123456C"))))

      val eventualResult = liveEmploymentsController.root(randomMatchId)(FakeRequest())

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
    }

    "fail with status 401 when the bearer token does not have enrolment test-scope" in new Setup {

      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(InsufficientEnrolments()))

      val result = liveEmploymentsController.root(randomMatchId)(FakeRequest())

      status(result) shouldBe UNAUTHORIZED
      verifyZeroInteractions(mockLiveEmploymentsService)
    }

    "not require bearer token authentication for sandbox" in new Setup {

      when(mockSandboxEmploymentsService.resolve(eqTo(randomMatchId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(NinoMatch(randomMatchId, Nino("AB123456C"))))

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

      when(mockLiveEmploymentsService.paye(eqTo(invalidMatchId), eqTo(interval), any(), any())(any()))
        .thenReturn(Future.failed(new MatchNotFoundException))

      val eventualResult = liveEmploymentsController.paye(invalidMatchId, interval)(FakeRequest())

      status(eventualResult) shouldBe NOT_FOUND

      contentAsJson(eventualResult) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    "return 200 OK" in new Setup {
      val matchId = UUID.randomUUID()

      when(mockLiveEmploymentsService.paye(eqTo(matchId), eqTo(interval), any(), any())(any()))
        .thenReturn(Future.successful(Seq(Employment.create(Employments.acme).get)))

      val res =
        liveEmploymentsController.paye(matchId, interval)(FakeRequest())

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
              "payeReference" -> "123/AI45678",
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
    }

    "fail with status 401 when the bearer token does not have enrolment read:individuals-employments-paye" in new Setup {

      when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(InsufficientEnrolments()))

      val result = liveEmploymentsController.paye(sandboxMatchId, interval)(FakeRequest())

      status(result) shouldBe UNAUTHORIZED
      verifyZeroInteractions(mockLiveEmploymentsService)
    }

    "not require bearer token authentication" in new Setup {

      when(
        mockSandboxEmploymentsService.paye(eqTo(sandboxMatchId), eqTo(interval), eqTo("paye"), eqTo(Seq("test-scope")))(
          any()))
        .thenReturn(Future.successful(Seq(Employments.acme, Employments.disney).map(Employment.create).map(_.get)))

      val eventualResult =
        sandboxEmploymentsController.paye(sandboxMatchId, interval)(FakeRequest())

      status(eventualResult) shouldBe OK
      verifyZeroInteractions(mockAuthConnector)
    }
  }
}
