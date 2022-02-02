/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.audit.v2

import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.models.{ApiFailureResponseEventModel, ApiResponseEventModel, IntegrationFrameworkApiResponseEventModel, ScopesAuditEventModel}
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployments
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Employment
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import javax.inject.Inject

import scala.concurrent.ExecutionContext

class AuditHelper @Inject()(auditConnector: AuditConnector)
                           (implicit ec: ExecutionContext) {

  def auditApiResponse(correlationId: String,
                       matchId: String,
                       scopes: String,
                       request: RequestHeader,
                       selfLink: String,
                       employments: Option[Seq[Employment]])
                      (implicit hc: HeaderCarrier) =
    auditConnector.sendExplicitAudit(
      "ApiResponseEvent",
      ApiResponseEventModel(
        deviceId = hc.deviceID.getOrElse("-"),
        input = s"Request to ${request.path}",
        method = request.method.toUpperCase,
        userAgent = request.headers.get("User-Agent").getOrElse("-"),
        apiVersion = "2.0",
        matchId = matchId,
        correlationId = Some(correlationId),
        request.headers.get("X-Application-ID").getOrElse("-"),
        scopes,
        returnLinks = selfLink,
        employments = employments
      )
    )

  def auditApiFailure(correlationId: Option[String],
                      matchId: String,
                      request: RequestHeader,
                      requestUrl: String,
                      msg: String)
                     (implicit hc: HeaderCarrier) =
    auditConnector.sendExplicitAudit(
      "ApiFailureEvent",
      ApiFailureResponseEventModel(
        deviceId = hc.deviceID.getOrElse("-"),
        input = s"Request to ${request.path}",
        method = request.method.toUpperCase,
        userAgent = request.headers.get("User-Agent").getOrElse("-"),
        apiVersion = "2.0",
        matchId = matchId,
        correlationId = correlationId,
        request.headers.get("X-Application-ID").getOrElse("-"),
        requestUrl,
        msg
      )
    )

  def auditIfApiResponse(correlationId: String,
                         matchId: String,
                         request: RequestHeader,
                         requestUrl: String,
                         ifEmployments: IfEmployments)
                        (implicit hc: HeaderCarrier) =
    auditConnector.sendExplicitAudit(
      "IntegrationFrameworkApiResponseEvent",
      IntegrationFrameworkApiResponseEventModel(
        deviceId = hc.deviceID.getOrElse("-"),
        input = s"Request to ${request.path}",
        method = request.method.toUpperCase,
        userAgent = request.headers.get("User-Agent").getOrElse("-"),
        apiVersion = "2.0",
        matchId = matchId,
        correlationId = correlationId,
        request.headers.get("X-Application-ID").getOrElse("-"),
        requestUrl = requestUrl,
        integrationFrameworkEmployments = ifEmployments
      )
    )

  def auditIfApiFailure(correlationId: String,
                        matchId: String,
                        request: RequestHeader,
                        requestUrl: String,
                        msg: String)
                       (implicit hc: HeaderCarrier) =
    auditConnector.sendExplicitAudit(
      "IntegrationFrameworkApiFailureEvent",
      ApiFailureResponseEventModel(
        deviceId = hc.deviceID.getOrElse("-"),
        input = s"Request to ${request.path}",
        method = request.method.toUpperCase,
        userAgent = request.headers.get("User-Agent").getOrElse("-"),
        apiVersion = "2.0",
        matchId = matchId,
        correlationId = Some(correlationId),
        request.headers.get("X-Application-ID").getOrElse("-"),
        requestUrl,
        msg
      )
    )

  def auditAuthScopes(matchId: String,
                      scopes:  String,
                      request: RequestHeader)
                     (implicit hc: HeaderCarrier) =
    auditConnector.sendExplicitAudit(
      "AuthScopesAuditEvent",
      ScopesAuditEventModel(
        deviceId = hc.deviceID.getOrElse("-"),
        input = s"Request to ${request.path}",
        method = request.method.toUpperCase,
        userAgent = request.headers.get("User-Agent").getOrElse("-"),
        apiVersion = "2.0",
        matchId = matchId,
        request.headers.get("X-Application-ID").getOrElse("-"),
        scopes
      )
    )
}
