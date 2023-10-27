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

package component.uk.gov.hmrc.individualsemploymentsapi.stubs

import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.MimeTypes.JSON

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

trait BaseSpec
    extends AnyFeatureSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with GuiceOneServerPerSuite
    with GivenWhenThen {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled"                                       -> false,
      "auditing.traceRequests"                                 -> false,
      "microservice.services.auth.port"                        -> AuthStub.port,
      "microservice.services.individuals-matching-api.port"    -> IndividualsMatchingApiStub.port,
      "microservice.services.des.port"                         -> DesStub.port,
      "microservice.services.integration-framework.port"       -> IfStub.port,
      "microservice.services.cacheable.short-lived-cache.port" -> Save4LaterStub.port,
      "run.mode"                                               -> "It"
    )
    .build()

  val timeout = Duration(5, TimeUnit.SECONDS)
  val serviceUrl = s"http://localhost:$port"
  val mocks = Seq(AuthStub, IndividualsMatchingApiStub, DesStub, IfStub, Save4LaterStub)
  val authToken = "Bearer AUTH_TOKEN"
  val acceptHeaderVP1 = ACCEPT -> "application/vnd.hmrc.P1.0+json"
  val acceptHeaderVP2 = ACCEPT -> "application/vnd.hmrc.2.0+json"
  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val validCorrelationHeader = ("CorrelationId", sampleCorrelationId)

  protected def requestHeaders(
    acceptHeader: (String, String) = acceptHeaderVP1,
    correlationHeader: (String, String) = validCorrelationHeader) =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader, correlationHeader)

  override protected def beforeEach(): Unit =
    mocks.foreach(m => if (!m.server.isRunning) m.server.start())

  override protected def afterEach(): Unit =
    mocks.foreach(_.mock.resetMappings())

  override def afterAll(): Unit =
    mocks.foreach(_.server.stop())
}
