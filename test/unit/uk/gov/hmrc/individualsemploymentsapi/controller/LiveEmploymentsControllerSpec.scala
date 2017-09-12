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
import uk.gov.hmrc.individualsemploymentsapi.controller.LiveEmploymentsController
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.service.LiveEmploymentsService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future.{failed, successful}

class LiveEmploymentsControllerSpec extends PlaySpec with Results with MockitoSugar {

  private val liveEmploymentsService = mock[LiveEmploymentsService]
  private val liveEmploymentsController = new LiveEmploymentsController(liveEmploymentsService)
  implicit val hc = HeaderCarrier()

  "Live employments controller root function" should {

    val randomMatchId = UUID.randomUUID()

    "return a 404 (not found) when a match id does not match live data" in {
      when(liveEmploymentsService.resolve(refEq(randomMatchId))(any[HeaderCarrier])).thenReturn(failed(new MatchNotFoundException))
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

    "return a 200 (ok) when a match id matches live data" in {
      when(liveEmploymentsService.resolve(refEq(randomMatchId))(any[HeaderCarrier])).thenReturn(successful(NinoMatch(randomMatchId, Nino("AB123456C"))))
      val eventualResult = liveEmploymentsController.root(randomMatchId).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        s"""
          {
            "_links":{
              "paye":{
                "href":"/individuals/employments/paye/match/$randomMatchId{?fromDate,toDate}",
                "title":"View individual's employments"
              },
              "self":{
                "href":"/individuals/employments/match/$randomMatchId"
              }
            }
          }
        """)
    }

  }

}
