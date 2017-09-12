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
import org.mockito.Matchers.{any, refEq}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.Status.OK
import play.api.libs.json.Json.parse
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.controller.SandboxEmploymentsController
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData.{Employments, sandboxMatchId}
import uk.gov.hmrc.individualsemploymentsapi.service.SandboxEmploymentsService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future.{failed, successful}

class SandboxEmploymentsControllerSpec extends PlaySpec with Results with MockitoSugar {

  private val sandboxEmploymentsService = mock[SandboxEmploymentsService]
  private val sandboxEmploymentsController = new SandboxEmploymentsController(sandboxEmploymentsService)
  implicit val hc = HeaderCarrier()

  "Sandbox employments controller root function" should {

    "return a 404 (not found) when a match id does not match sandbox data" in {
      val invalidMatchId = UUID.randomUUID()
      when(sandboxEmploymentsService.resolve(refEq(invalidMatchId))(any[HeaderCarrier])).thenReturn(failed(new MatchNotFoundException))
      val eventualResult = sandboxEmploymentsController.root(invalidMatchId).apply(FakeRequest())
      status(eventualResult) mustBe NOT_FOUND
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "code":"NOT_FOUND",
            "message":"The resource can not be found"
          }
        """)
    }

    "return a 200 (ok) when a match id matches sandbox data" in {
      when(sandboxEmploymentsService.resolve(refEq(sandboxMatchId))(any[HeaderCarrier])).thenReturn(successful(NinoMatch(sandboxMatchId, Nino("AB123456C"))))
      val eventualResult = sandboxEmploymentsController.root(sandboxMatchId).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        s"""
          {
            "_links":{
              "paye":{
                "href":"/individuals/employments/paye/match/$sandboxMatchId{?fromDate,toDate}",
                "title":"View individual's employments"
              },
              "self":{
                "href":"/individuals/employments/match/$sandboxMatchId"
              }
            }
          }
        """)
    }

  }

  "Sandbox employments controller paye function" should {

    val fromDate = new LocalDate("2017-03-02").toDateTimeAtStartOfDay
    val toDate = new LocalDate("2017-05-31").toDateTimeAtStartOfDay
    val interval = new Interval(fromDate, toDate)

    "return 404 (not found) when a match id is malformed" in {
      val eventualResult = sandboxEmploymentsController.paye("malformedMatchId", interval).apply(FakeRequest())
      status(eventualResult) mustBe NOT_FOUND
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "code":"NOT_FOUND",
            "message":"The resource can not be found"
          }
        """)
    }

    "return 404 (not found) for an invalid matchId" in {
      val invalidMatchId = UUID.randomUUID()
      when(sandboxEmploymentsService.paye(invalidMatchId, interval)).thenReturn(failed(new MatchNotFoundException))

      val eventualResult = sandboxEmploymentsController.paye(invalidMatchId.toString, interval).apply(FakeRequest())
      status(eventualResult) mustBe NOT_FOUND
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "code":"NOT_FOUND",
            "message":"The resource can not be found"
          }
        """)
    }

    "return 200 (ok) when matching succeeds and service returns no employments" in {
      when(sandboxEmploymentsService.paye(sandboxMatchId, interval)).thenReturn(successful(Seq.empty))

      val eventualResult = sandboxEmploymentsController.paye(sandboxMatchId.toString, interval).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        s"""
          {
            "_links":{
              "self":{
                "href":"/individuals/employments/paye/match/$sandboxMatchId?fromDate=2017-03-02"
              }
            },
            "_embedded":{
              "employments":[]
            }
          }
        """)
    }

    "return 200 (ok) when matching succeeds and service returns employments" in {
      when(sandboxEmploymentsService.paye(sandboxMatchId, interval)).thenReturn(successful(Seq(Employments.acme, Employments.disney)))

      val eventualResult = sandboxEmploymentsController.paye(sandboxMatchId.toString, interval).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "_links":{
              "self":{
                "href":"/individuals/employments/paye/match/57072660-1df9-4aeb-b4ea-cd2d7f96e430?fromDate=2017-03-02"
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

  }

}
