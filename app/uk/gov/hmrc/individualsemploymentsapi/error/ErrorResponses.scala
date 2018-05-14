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

package uk.gov.hmrc.individualsemploymentsapi.error

import play.api.http.Status.{NOT_FOUND, _}
import play.api.libs.json.Json.toJson
import play.api.mvc.Results.Status
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters._

object ErrorResponses {

  sealed abstract class ErrorResponse(val httpStatusCode: Int, val errorCode: String, val message: String) {

    def toHttpResponse = Status(httpStatusCode)(toJson(this))
  }

  case object ErrorNotFound extends ErrorResponse(NOT_FOUND, "NOT_FOUND", "The resource can not be found")

  case object ErrorUnauthorized extends ErrorResponse(UNAUTHORIZED, "UNAUTHORIZED", "Bearer token is missing or not authorized")

  case object ErrorInternalServer extends ErrorResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Failed to process request")

  case class ErrorInvalidRequest(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)

  class ValidationException(message: String) extends RuntimeException(message)

  case object MatchNotFoundException extends RuntimeException

}
