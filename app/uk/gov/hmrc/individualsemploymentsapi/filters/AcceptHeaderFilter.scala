/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.filters

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import play.api.mvc.Results.BadRequest
import play.api.mvc.{Filter, RequestHeader, Result}
import uk.gov.hmrc.individualsemploymentsapi.util.RequestHeaderUtils.getVersion
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.Future

class AcceptHeaderFilter @Inject()(val mat: Materializer) extends Filter {

  private val acceptHeaderRegex = "application/vnd\\.hmrc\\.(.*)\\+json".r
  implicit val erFormats = Json.format[ErrorResponse]

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    val version = getVersion(rh)
    if(version.equals("P1.0")) f(rh)
    else Future.successful(BadRequest(Json.toJson(ErrorResponse(BAD_REQUEST, s"Invalid accept header version: $version"))))
  }
}
