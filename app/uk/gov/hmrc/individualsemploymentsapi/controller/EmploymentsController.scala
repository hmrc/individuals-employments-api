/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.controller

import java.util.UUID

import javax.inject.{Inject, Singleton}
import org.joda.time.Interval
import play.api.hal.Hal._
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.libs.json.Json.{obj, toJson}
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.individualsemploymentsapi.config.ServiceAuthConnector
import uk.gov.hmrc.individualsemploymentsapi.controller.Environment.{PRODUCTION, SANDBOX}
import uk.gov.hmrc.individualsemploymentsapi.service.{EmploymentsService, LiveEmploymentsService, SandboxEmploymentsService}
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

import scala.concurrent.ExecutionContext.Implicits.global

abstract class EmploymentsController(employmentsService: EmploymentsService) extends CommonController with PrivilegedAuthentication {

  def root(matchId: UUID) = Action.async { implicit request =>
    requiresPrivilegedAuthentication("read:individuals-employments") {
      employmentsService.resolve(matchId) map { _ =>
        val payeLink = HalLink("paye", s"/individuals/employments/paye?matchId=$matchId{&fromDate,toDate}", title = Option("View individual's employments"))
        val selfLink = HalLink("self", s"/individuals/employments/?matchId=$matchId")
        Ok(links(payeLink, selfLink))
      } recover recovery
    }
  }

  def paye(matchId: UUID, interval: Interval) = Action.async { implicit request =>
    requiresPrivilegedAuthentication("read:individuals-employments-paye") {
      employmentsService.paye(matchId, interval) map { employments =>
        val selfLink = HalLink("self", urlWithInterval(s"/individuals/employments/paye?matchId=$matchId", interval.getStart))
        val employmentsJsObject = obj("employments" -> toJson(employments))
        Ok(state(employmentsJsObject) ++ selfLink)
      }
    } recover recovery
  }

  def payroll(matchId: UUID, interval: Interval): Action[AnyContent] = Action.async { implicit request =>
    requiresPrivilegedAuthentication("read:individuals-employments-payroll") {
      employmentsService.payroll(matchId, interval) map { payrolls =>
        val selfLink = HalLink("self", urlWithInterval(s"/individuals/employments/paye/payroll?matchId=$matchId", interval.getStart))
        val payrollJsObject = Json.obj("payroll" -> Json.toJson(payrolls))
        Ok(state(payrollJsObject) ++ selfLink)
      }
    }
  }

}

@Singleton
class SandboxEmploymentsController @Inject()(sandboxEmploymentsService: SandboxEmploymentsService, val authConnector: ServiceAuthConnector)
  extends EmploymentsController(sandboxEmploymentsService) {
  override val environment = SANDBOX
}

@Singleton
class LiveEmploymentsController @Inject()(liveEmploymentsService: LiveEmploymentsService, val authConnector: ServiceAuthConnector)
  extends EmploymentsController(liveEmploymentsService) {
  override val environment = PRODUCTION
}
