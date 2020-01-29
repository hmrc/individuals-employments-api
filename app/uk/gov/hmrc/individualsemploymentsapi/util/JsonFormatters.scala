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

package uk.gov.hmrc.individualsemploymentsapi.util

import java.util.UUID

import play.api.libs.json._
import uk.gov.hmrc.individualsemploymentsapi.domain._
import uk.gov.hmrc.individualsemploymentsapi.domain.des._
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.{ErrorInvalidRequest, ErrorResponse}
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

import scala.util.{Failure, Try}

object JsonFormatters {

  implicit val ninoMatchJsonFormat = Json.format[NinoMatch]
  implicit val addressJsonFormat = Json.format[Address]
  implicit val employerJsonFormat = Json.format[Employer]
  implicit val payFrequencyJsonFormat = EnumJson.enumFormat(PayFrequency)
  implicit val employmentJsonFormat = Json.format[Employment]

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }

  implicit val errorInvalidRequestFormat = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val uuidJsonFormat = new Format[UUID] {
    override def writes(uuid: UUID) = JsString(uuid.toString)

    override def reads(json: JsValue) = JsSuccess(UUID.fromString(json.asInstanceOf[JsString].value))
  }

  implicit val desAddressJsonFormat = Json.format[DesAddress]
  implicit val desPaymentJsonFormat = Json.format[DesPayment]
  implicit val desEmploymentPayFrequencyJsonFormat = EnumJson.enumFormat(DesPayFrequency)
  implicit val desEmploymentJsonFormat = Json.format[DesEmployment]
  implicit val desEmploymentsJsonFormat = Json.format[DesEmployments]

  object EnumJson {

    class InvalidEnumException(className: String, input: String) extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")

    def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
      def reads(json: JsValue): JsResult[E#Value] = json match {
        case JsString(s) =>
          Try(JsSuccess(enum.withName(s))) recoverWith {
            case _: NoSuchElementException => Failure(new InvalidEnumException(enum.getClass.getSimpleName, s))
          } get
        case _ => JsError("String value expected")
      }
    }

    implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
      def writes(v: E#Value): JsValue = JsString(v.toString)
    }

    implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
      Format(enumReads(enum), enumWrites)
    }

  }

}
