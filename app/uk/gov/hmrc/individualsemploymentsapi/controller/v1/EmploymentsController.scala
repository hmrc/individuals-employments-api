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

package uk.gov.hmrc.individualsemploymentsapi.controller.v1

import play.api.hal.Hal._
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.individualsemploymentsapi.controller.v1.Environment.{PRODUCTION, SANDBOX}
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.Employment
import uk.gov.hmrc.individualsemploymentsapi.service.v1.{EmploymentsService, LiveEmploymentsService, SandboxEmploymentsService}
import uk.gov.hmrc.individualsemploymentsapi.util.Interval
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

import java.time.LocalDate
import java.util.UUID
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

abstract class EmploymentsController(employmentsService: EmploymentsService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends CommonController(cc) with PrivilegedAuthentication {

  val hmctsClientId: String

  def root(matchId: UUID): Action[AnyContent] = Action.async { implicit request =>
    requiresPrivilegedAuthentication("read:individuals-employments") {
      employmentsService.resolve(matchId) map { _ =>
        val payeLink = HalLink(
          "paye",
          s"/individuals/employments/paye?matchId=$matchId{&fromDate,toDate}",
          title = Option("View individual's employments")
        )
        val selfLink = HalLink("self", s"/individuals/employments/?matchId=$matchId")
        Ok(links(payeLink, selfLink))
      }
    }.recover(recovery)
  }

  def paye(matchId: UUID, interval: Interval): Action[AnyContent] = Action.async { implicit request =>
    val cutoff = 1800 // To be confirmed by the business - DLS-9957 failed release
    requiresPrivilegedAuthentication("read:individuals-employments-paye") {
      if (interval.getStart isBefore LocalDate.parse(s"$cutoff-01-01").atStartOfDay()) {
        Future.successful(BadRequest(s"Cannot query dates before $cutoff"))
      } else {
        employmentsService.paye(matchId, interval).map { employments =>
          val selfLink =
            HalLink("self", urlWithInterval(s"/individuals/employments/paye?matchId=$matchId", interval.getStart))
          val filtered = filterPayrollData(employments)
          val employmentsJsObject = Json.obj("employments" -> Json.toJson(filtered))
          Ok(state(employmentsJsObject) ++ selfLink)
        }
      }
    }.recover(recovery)
  }

  // Home Office and HMCTS want to use the same endpoint,
  // but HO aren't authorised to view payroll IDs or employee addresses
  // so this filters fields based on client ID
  private def filterPayrollData(employments: Seq[Employment])(implicit request: Request[AnyContent]): Seq[Employment] =
    request.headers.get("X-Client-ID") match {
      case Some(clientId) if clientId == hmctsClientId => employments
      case Some(_) => employments.map(_.copy(payrollId = None, employeeAddress = None))
      case None =>
        logger.warn("Missing X-Client-Id header")
        employments.map(_.copy(payrollId = None, employeeAddress = None))
    }

}

@Singleton
class SandboxEmploymentsController @Inject() (
  sandboxEmploymentsService: SandboxEmploymentsService,
  val authConnector: AuthConnector,
  @Named("hmctsClientId") val hmctsClientId: String,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends EmploymentsController(sandboxEmploymentsService, cc) {

  override val environment: String = SANDBOX
}

@Singleton
class LiveEmploymentsController @Inject() (
  liveEmploymentsService: LiveEmploymentsService,
  val authConnector: AuthConnector,
  @Named("hmctsClientId") val hmctsClientId: String,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends EmploymentsController(liveEmploymentsService, cc) {

  override val environment: String = PRODUCTION
}
