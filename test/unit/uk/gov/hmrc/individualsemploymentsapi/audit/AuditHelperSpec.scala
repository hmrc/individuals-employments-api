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

package unit.uk.gov.hmrc.individualsemploymentsapi.audit

import org.mockito.Mockito.{times, verify}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.AuditHelper
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.{EmploymentsHelper, UnitSpec}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.models.{ApiFailureResponseEventModel, ApiResponseEventModel, IntegrationFrameworkApiResponseEventModel, ScopesAuditEventModel}
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployments
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment

import scala.concurrent.ExecutionContext.Implicits.global

class AuditHelperSpec extends UnitSpec with MockitoSugar with EmploymentsHelper {

  implicit val hc = HeaderCarrier()

  val nino = "CS700100A"
  val correlationId = "test"
  val scopes = "test"
  val matchId = "80a6bb14-d888-436e-a541-4000674c60aa"
  val applicationId = "80a6bb14-d888-436e-a541-4000674c60bb"
  val request = FakeRequest().withHeaders("X-Application-Id" -> applicationId)
  val ifApiResponse = IfEmployments(List(createValidEmployment))
  val apiResponse = Seq(Employment.create(ifApiResponse.employments.head).get)
  val ifUrl =
    s"host/individuals/employments/paye/nino/$nino?startDate=2019-01-01&endDate=2020-01-01&fields=some(vals(val1),val2)"
  val endpoint = "/test"

  val auditConnector = mock[AuditConnector]

  val auditHelper = new AuditHelper(auditConnector)

  "Auth helper" should {

    "auditAuthScopes" in {

      Mockito.reset(auditConnector)

      val captor = ArgumentCaptor.forClass(classOf[ScopesAuditEventModel])

      auditHelper.auditAuthScopes(matchId, scopes, request)

      verify(auditConnector, times(1)).sendExplicitAudit(eqTo("AuthScopesAuditEvent"),
        captor.capture())(any(), any(), any())

      val capturedEvent = captor.getValue
      capturedEvent.asInstanceOf[ScopesAuditEventModel].apiVersion shouldEqual "2.0"
      capturedEvent.asInstanceOf[ScopesAuditEventModel].matchId shouldEqual matchId
      capturedEvent.asInstanceOf[ScopesAuditEventModel].scopes shouldBe scopes
      capturedEvent.asInstanceOf[ScopesAuditEventModel].applicationId shouldBe applicationId

    }

    "auditApiResponse" in {

      Mockito.reset(auditConnector)

      val captor = ArgumentCaptor.forClass(classOf[ApiResponseEventModel])

      auditHelper.auditApiResponse(correlationId, matchId, scopes, request, endpoint, Some(apiResponse))

      verify(auditConnector, times(1)).sendExplicitAudit(eqTo("ApiResponseEvent"),
        captor.capture())(any(), any(), any())

      val capturedEvent = captor.getValue
      capturedEvent.asInstanceOf[ApiResponseEventModel].matchId shouldEqual matchId
      capturedEvent.asInstanceOf[ApiResponseEventModel].correlationId shouldEqual Some(correlationId)
      capturedEvent.asInstanceOf[ApiResponseEventModel].scopes shouldBe scopes
      capturedEvent.asInstanceOf[ApiResponseEventModel].returnLinks shouldBe endpoint
      capturedEvent.asInstanceOf[ApiResponseEventModel].employments shouldBe Some(apiResponse)
      capturedEvent.asInstanceOf[ApiResponseEventModel].applicationId shouldBe applicationId

    }

    "auditApiFailure" in {

      Mockito.reset(auditConnector)

      val msg = "Something went wrong"

      val captor = ArgumentCaptor.forClass(classOf[ApiFailureResponseEventModel])

      auditHelper.auditApiFailure(Some(correlationId), matchId, request, "/test", msg)

      verify(auditConnector, times(1)).sendExplicitAudit(eqTo("ApiFailureEvent"),
        captor.capture())(any(), any(), any())

      val capturedEvent = captor.getValue
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].matchId shouldEqual matchId
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].correlationId shouldEqual Some(correlationId)
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].requestUrl shouldEqual endpoint
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].response shouldEqual msg
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].applicationId shouldBe applicationId
    }

    "auditIfApiResponse" in {

      Mockito.reset(auditConnector)

      val captor = ArgumentCaptor.forClass(classOf[IntegrationFrameworkApiResponseEventModel])

      auditHelper.auditIfApiResponse(correlationId, matchId, request, ifUrl, ifApiResponse)

      verify(auditConnector, times(1)).sendExplicitAudit(eqTo("IntegrationFrameworkApiResponseEvent"),
        captor.capture())(any(), any(), any())

      val capturedEvent = captor.getValue
      capturedEvent.asInstanceOf[IntegrationFrameworkApiResponseEventModel].matchId shouldEqual matchId
      capturedEvent.asInstanceOf[IntegrationFrameworkApiResponseEventModel].correlationId shouldEqual correlationId
      capturedEvent.asInstanceOf[IntegrationFrameworkApiResponseEventModel].requestUrl shouldBe ifUrl
      capturedEvent.asInstanceOf[IntegrationFrameworkApiResponseEventModel].integrationFrameworkEmployments shouldBe ifApiResponse
      capturedEvent.asInstanceOf[IntegrationFrameworkApiResponseEventModel].applicationId shouldBe applicationId

    }

    "auditIfApiFailure" in {

      Mockito.reset(auditConnector)

      val msg = "Something went wrong"

      val captor = ArgumentCaptor.forClass(classOf[ApiFailureResponseEventModel])

      auditHelper.auditIfApiFailure(correlationId, matchId, request, ifUrl, msg)

      verify(auditConnector, times(1)).sendExplicitAudit(eqTo("IntegrationFrameworkApiFailureEvent"), captor.capture())(any(), any(), any())

      val capturedEvent = captor.getValue
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].matchId shouldEqual matchId
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].correlationId shouldEqual Some(correlationId)
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].requestUrl shouldEqual ifUrl
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].response shouldEqual msg
      capturedEvent.asInstanceOf[ApiFailureResponseEventModel].applicationId shouldBe applicationId

    }

  }

}
