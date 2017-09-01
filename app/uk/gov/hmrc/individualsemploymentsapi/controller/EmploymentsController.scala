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

package uk.gov.hmrc.individualsemploymentsapi.controller

import java.util.UUID
import javax.inject.Singleton

import play.api.hal.Hal._
import play.api.hal.HalLink
import play.api.mvc.hal._
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.error.Recovery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

abstract class EmploymentsController extends Controller with Recovery {

  def root(matchId: UUID) = Action.async {
    resolve(matchId) map { _ =>
      val payeLink = HalLink("paye", s"/individuals/employments/paye/match/$matchId{?fromDate,toDate}", title = Option("View individual's employments"))
      val selfLink = HalLink("self", s"/individuals/employments/match/$matchId")
      Ok(links(payeLink, selfLink))
    } recover recovery
  }

  protected def resolve(matchId: UUID): Future[Nino]

}

@Singleton
class SandboxEmploymentsController extends EmploymentsController {

  import uk.gov.hmrc.individualsemploymentsapi.sandbox.SandboxData._

  override protected def resolve(matchId: UUID) = if (matchId.equals(sandboxMatchId)) successful(sandboxNino) else failed(new MatchNotFoundException)

}
