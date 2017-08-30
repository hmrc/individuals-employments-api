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

import org.scalatestplus.play._
import play.api.http.Status.OK
import play.api.libs.json.Json.parse
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.controller.EmploymentsController
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException

import scala.concurrent.Future.{failed, successful}

class EmploymentsControllerSpec extends PlaySpec with Results {

  "Employments controller hateoas function" should {

    "return a 404 (not found) when a match id cannot be resolved" in {
      val eventualResult = new EmploymentsController {
        override protected def resolve(matchId: UUID) = failed(new MatchNotFoundException)
      }.hateoas(UUID.fromString("322049c9-ffcf-4483-992b-48bf48010a71")).apply(FakeRequest())
      status(eventualResult) mustBe NOT_FOUND
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "code":"NOT_FOUND",
            "message":"The resource can not be found"
          }
        """)
    }

    "return a 200 (ok) when a match id cannot be resolved" in {
      val eventualResult = new EmploymentsController {
        override protected def resolve(matchId: UUID) = successful(Nino("AB654321C"))
      }.hateoas(UUID.fromString("322049c9-ffcf-4483-992b-48bf48010a71")).apply(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsJson(eventualResult) mustBe parse(
        """
          {
            "_links":{
              "paye":{
                "href":"/individuals/employments/paye/match/322049c9-ffcf-4483-992b-48bf48010a71{?fromDate,toDate}",
                "title":"View individual's employments"
              },
              "self":{
                "href":"/individuals/employments/match/322049c9-ffcf-4483-992b-48bf48010a71"
              }
            }
          }
        """)
    }

  }

}
