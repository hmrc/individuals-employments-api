/*
 * Copyright 2026 HM Revenue & Customs
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

import org.mockito.Mockito.{times, verify}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.config.AppConfig
import uk.gov.hmrc.individualsemploymentsapi.connector.{DesConnector, HipConnector, IntegrationPlatformRouter}
import uk.gov.hmrc.individualsemploymentsapi.util.Interval
import unit.uk.gov.hmrc.individualsemploymentsapi.util.UnitSpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

class IntegrationPlatformRouterSpec extends UnitSpec with MockitoSugar with Matchers {

  val mockDesConnector = mock[DesConnector]
  val mockHipConnector = mock[HipConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val rh: RequestHeader = FakeRequest()
  implicit val ec: ExecutionContext = ExecutionContext.global

  "Integration Platform Router" should {
    "invoke the HIP connector when the 6077 feature flag is enabled" in {
      val subject: IntegrationPlatformRouter = createSubject("feature-flags.hip.api-6077" -> "true")
      val nino = Nino("JA868142A")
      val interval = Interval(LocalDateTime.now().minusDays(1), LocalDateTime.now())

      subject.fetchEmployments(nino, interval)

      verify(mockHipConnector, times(1)).fetchEmployments(nino, interval)
    }

    "invoke the DES connector when the 6077 feature flag is disabled" in {
      val subject: IntegrationPlatformRouter = createSubject("feature-flags.hip.api-6077" -> "false")
      val nino = Nino("JA868142A")
      val interval = Interval(LocalDateTime.now().minusDays(1), LocalDateTime.now())

      subject.fetchEmployments(nino, interval)

      verify(mockDesConnector, times(1)).fetchEmployments(nino, interval)
    }
  }

  private def createSubject(conf: (String, String)) = {
    def fakeApplication(): Application = GuiceApplicationBuilder().configure(conf).build()
    val appConfig = fakeApplication().injector.instanceOf[AppConfig]
    val subject = new IntegrationPlatformRouter(appConfig, mockDesConnector, mockHipConnector)
    subject
  }
}
