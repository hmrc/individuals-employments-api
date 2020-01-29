/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import org.joda.time.DateTime
import play.api.mvc.{ControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.http.{HeaderCarrier, TooManyRequestException}
import uk.gov.hmrc.individualsemploymentsapi.controller.Environment.SANDBOX
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses._
import uk.gov.hmrc.individualsemploymentsapi.util.Dates._
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class CommonController @Inject()(cc: ControllerComponents) extends BackendController(cc) {

  private def getQueryParam[T](name: String)(implicit request: Request[T]) = request.queryString.get(name).flatMap(_.headOption)

  private[controller] def urlWithInterval[T](url: String, fromDate: DateTime)(implicit request: Request[T]) = {
    val urlWithFromDate = s"$url&fromDate=${toFormattedLocalDate(fromDate)}"
    getQueryParam("toDate") map (toDate => s"$urlWithFromDate&toDate=$toDate") getOrElse urlWithFromDate
  }

  private[controller] def recovery: PartialFunction[Throwable, Result] = {
    case _: MatchNotFoundException   => ErrorNotFound.toHttpResponse
    case e: AuthorisationException   => ErrorUnauthorized(e.getMessage).toHttpResponse
    case _: TooManyRequestException  => ErrorTooManyRequests.toHttpResponse
    case e: IllegalArgumentException => ErrorInvalidRequest(e.getMessage).toHttpResponse
  }

}

trait PrivilegedAuthentication extends AuthorisedFunctions {

  val environment: String

  def requiresPrivilegedAuthentication(scope: String)(body: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    if (environment == SANDBOX) body
    else authorised(Enrolment(scope))(body)
  }
}

object Environment {
  val SANDBOX = "SANDBOX"
  val PRODUCTION = "PRODUCTION"
}