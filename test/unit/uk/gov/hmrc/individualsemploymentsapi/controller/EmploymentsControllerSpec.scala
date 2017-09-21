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

package unit.uk.gov.hmrc.individualsemploymentsapi.controller

import java.util.UUID

import org.joda.time.{Interval, LocalDate}
import org.mockito.BDDMockito.given
import org.mockito.Matchers.{any, refEq}
import org.mockito.Mockito.{verifyZeroInteractions, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.Status.OK
import play.api.libs.json.Json.parse
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.auth.core.authorise.Enrolment
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.config.ServiceAuthConnector
import uk.gov.hmrc.individualsemploymentsapi.controller.{LiveEmploymentsController, SandboxEmploymentsController}
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.{Employments, sandboxMatchId}
import uk.gov.hmrc.individualsemploymentsapi.service.{LiveEmploymentsService, SandboxEmploymentsService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future.{failed, successful}

class EmploymentsControllerSpec extends PlaySpec with Results with MockitoSugar {

  trait Setup {
    val mockSandboxEmploymentsService = mock[SandboxEmploymentsService]
    val mockLiveEmploymentsService = mock[LiveEmploymentsService]

    val mockAuthConnector = mock[ServiceAuthConnector]

    val sandboxEmploymentsController = new SandboxEmploymentsController(mockSandboxEmploymentsService, mockAuthConnector)
    val liveEmploymentsController = new LiveEmploymentsController(mockLiveEmploymentsService, mockAuthConnector)

    implicit val hc = HeaderCarrier()

    given(mockAuthConnector.authorise(any(), refEq(EmptyRetrieval))(any())).willReturn(successful(()))
  }

  "Root" should {
    val randomMatchId = UUID.randomUUID()

    "return a 404 (not found) when a match id does not match live data" in new Setup {
      when(mockLiveEmploymentsService.resolve(refEq(randomMatchId))(any[HeaderCarrier])).thenReturn(failed(new MatchNotFoundException))

      val eventualResult = liveEmploymentsController.root(randomMatchId).apply(FakeRequest())

      status(eventualResult) mustBe NOT_FOUND
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "code":"NOT_FOUND",
            "message":"The resource can not be found"
          }
        """)
    }

    "return a 200 (ok) when a match id matches live data" in new Setup {
      when(mockLiveEmploymentsService.resolve(refEq(randomMatchId))(any[HeaderCarrier])).thenReturn(successful(NinoMatch(randomMatchId, Nino("AB123456C"))))
      val eventualResult = liveEmploymentsController.root(randomMatchId).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        s"""
          {
            "_links":{
              "paye":{
                "href":"/individuals/employments/paye?matchId=$randomMatchId{&fromDate,toDate}",
                "title":"View individual's employments"
              },
              "self":{
                "href":"/individuals/employments/?matchId=$randomMatchId"
              }
            }
          }
        """)
    }

    "fail with AuthorizedException when the bearer token does not have enrolment read:individuals-employments" in new Setup {
      given(mockAuthConnector.authorise(refEq(Enrolment("read:individuals-employments")), refEq(EmptyRetrieval))(any())).willReturn(failed(new InsufficientEnrolments()))

      intercept[InsufficientEnrolments]{await(liveEmploymentsController.root(randomMatchId).apply(FakeRequest()))}
      verifyZeroInteractions(mockLiveEmploymentsService)
    }

    "not require bearer token authentication for sandbox" in new Setup {
      when(mockSandboxEmploymentsService.resolve(refEq(randomMatchId))(any[HeaderCarrier])).thenReturn(successful(NinoMatch(randomMatchId, Nino("AB123456C"))))

      val result = sandboxEmploymentsController.root(randomMatchId).apply(FakeRequest())

      status(result) mustBe OK
      verifyZeroInteractions(mockAuthConnector)
    }
  }

  "Employments controller paye function" should {

    val fromDate = new LocalDate("2017-03-02").toDateTimeAtStartOfDay
    val toDate = new LocalDate("2017-05-31").toDateTimeAtStartOfDay
    val interval = new Interval(fromDate, toDate)

    "return 404 (not found) for an invalid matchId" in new Setup {
      val invalidMatchId = UUID.randomUUID()
      when(mockLiveEmploymentsService.paye(refEq(invalidMatchId), refEq(interval))(any())).thenReturn(failed(new MatchNotFoundException))

      val eventualResult = liveEmploymentsController.paye(invalidMatchId, interval).apply(FakeRequest())
      status(eventualResult) mustBe NOT_FOUND
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "code":"NOT_FOUND",
            "message":"The resource can not be found"
          }
        """)
    }

    "return 200 (ok) when matching succeeds and service returns employments" in new Setup {
      when(mockLiveEmploymentsService.paye(refEq(sandboxMatchId), refEq((interval)))(any())).thenReturn(successful(Seq(Employments.acme, Employments.disney)))

      val eventualResult = liveEmploymentsController.paye(sandboxMatchId, interval).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "_links":{
              "self":{
                "href":"/individuals/employments/paye?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430&fromDate=2017-03-02"
              }
            },
            "_embedded":{
              "employments":[
                {
                  "startDate":"2016-01-01",
                  "endDate":"2016-06-30",
                  "employer":{
                    "payeReference":"123/AI45678",
                    "name":"Acme",
                    "address":{
                      "line1":"Acme Inc Building",
                      "line2":"Acme Inc Campus",
                      "line3":"Acme Street",
                      "line4":"AcmeVille",
                      "line5":"Acme State",
                      "postcode":"AI22 9LL"
                    }
                  },
                  "payFrequency":"FOUR_WEEKLY"
                },
                {
                  "startDate":"2017-01-02",
                  "endDate":"2017-03-01",
                  "employer":{
                    "payeReference":"123/DI45678",
                    "name":"Disney",
                    "address":{
                      "line1":"Friars House",
                      "line2":"Campus Way",
                      "line3":"New Street",
                      "line4":"Sometown",
                      "line5":"Old County",
                      "postcode":"TF22 3BC"
                    }
                  },
                  "payFrequency":"FORTNIGHTLY"
                }
              ]
            }
          }
        """)
    }

    "fail with AuthorizedException when the bearer token does not have enrolment read:individuals-employments" in new Setup {

      given(mockAuthConnector.authorise(refEq(Enrolment("read:individuals-employments")), refEq(EmptyRetrieval))(any())).willReturn(failed(new InsufficientEnrolments()))

      intercept[InsufficientEnrolments]{await(liveEmploymentsController.paye(sandboxMatchId, interval).apply(FakeRequest()))}
      verifyZeroInteractions(mockLiveEmploymentsService)
    }

    "not require bearer token authentication" in new Setup {
      when(mockSandboxEmploymentsService.paye(refEq(sandboxMatchId), refEq((interval)))(any())).thenReturn(successful(Seq(Employments.acme, Employments.disney)))

      val eventualResult = sandboxEmploymentsController.paye(sandboxMatchId, interval).apply(FakeRequest())

      status(eventualResult) mustBe OK
      verifyZeroInteractions(mockAuthConnector)
    }

  }

}
