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

package uk.gov.hmrc.individualsemploymentsapi.controller.v2

import org.joda.time.Interval
import play.api.hal.Hal._
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.AuditHelper
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{EmploymentsService, ScopesHelper, ScopesService}
import uk.gov.hmrc.individualsemploymentsapi.util.RequestHeaderUtils.{maybeCorrelationId, validateCorrelationId}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EmploymentsController @Inject()(
  employmentsService: EmploymentsService,
  scopeService: ScopesService,
  scopesHelper: ScopesHelper,
  val authConnector: AuthConnector,
  implicit val auditHelper: AuditHelper,
  cc: ControllerComponents)(implicit val ec: ExecutionContext)
    extends CommonController(cc) with PrivilegedAuthentication {

  def root(matchId: String): Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopeService.getAllScopes, matchId) { authScopes =>
      withValidUuid(matchId, "matchId") { matchIdUuid =>
        val correlationId = validateCorrelationId(request)

        employmentsService.resolve(matchIdUuid) map { _ =>
          val selfLink = HalLink("self", s"/individuals/employments/?matchId=$matchId")
          val response = scopesHelper.getHalLinks(matchIdUuid, None, authScopes, None) ++ selfLink

          auditHelper.auditApiResponse(
            correlationId.toString,
            matchId,
            authScopes.mkString(","),
            request,
            response.toString,
            None)

          Ok(response)

        }
      }

    } recover withAudit(maybeCorrelationId(request), matchId, "/individuals/employments")
  }

  def paye(matchId: String, interval: Interval, payeReference: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      authenticate(scopeService.getEndPointScopes("paye"), matchId.toString) { authScopes =>
        withValidUuid(matchId, "matchId") { matchIdUuid =>
          val correlationId = validateCorrelationId(request)

          employmentsService.paye(matchIdUuid, interval, payeReference, "paye", authScopes).map { employments =>
            val selfLink =
              HalLink("self", urlWithInterval(s"/individuals/employments/paye?matchId=$matchId", interval.getStart))
            val response = state(Json.obj("employments" -> Json.toJson(employments))) ++ selfLink

            auditHelper.auditApiResponse(
              correlationId.toString,
              matchId,
              authScopes.mkString(","),
              request,
              selfLink.toString,
              Some(employments))

            Ok(response)
          }
        }

      } recover withAudit(maybeCorrelationId(request), matchId.toString, "/individuals/employments/paye")
  }
}
