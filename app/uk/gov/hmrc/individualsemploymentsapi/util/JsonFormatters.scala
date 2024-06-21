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

package uk.gov.hmrc.individualsemploymentsapi.util

import play.api.libs.json._
import uk.gov.hmrc.individualsemploymentsapi.domain.PayFrequencyCode
import uk.gov.hmrc.individualsemploymentsapi.domain.des._
import uk.gov.hmrc.individualsemploymentsapi.domain.v1.{Address, Employer, Employment, PayFrequency}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.{ErrorInvalidRequest, ErrorResponse}

import java.util.UUID
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Try}

object JsonFormatters {

  implicit val addressJsonFormat: OFormat[Address] = Json.format[Address]
  implicit val employerJsonFormat: OFormat[Employer] = Json.format[Employer]
  implicit val payFrequencyJsonFormat: Format[PayFrequency.Value] = EnumJson.enumFormat(PayFrequency)
  implicit val employmentJsonFormat: OFormat[Employment] = Json.format[Employment]

  implicit val errorResponseWrites: Writes[ErrorResponse] = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }

  implicit val errorInvalidRequestFormat: Format[ErrorInvalidRequest] = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val uuidJsonFormat: Format[UUID] = new Format[UUID] {
    override def writes(uuid: UUID): JsString = JsString(uuid.toString)

    override def reads(json: JsValue): JsSuccess[UUID] = JsSuccess(UUID.fromString(json.asInstanceOf[JsString].value))
  }

  implicit val desAddressJsonFormat: OFormat[DesAddress] = Json.format[DesAddress]
  implicit val desPaymentJsonFormat: OFormat[DesPayment] = Json.format[DesPayment]
  implicit val desEmploymentPayFrequencyJsonFormat: Format[PayFrequencyCode.Value] =
    EnumJson.enumFormat(PayFrequencyCode)
  implicit val desEmploymentJsonFormat: OFormat[DesEmployment] = Json.format[DesEmployment]
  implicit val desEmploymentsJsonFormat: OFormat[DesEmployments] = Json.format[DesEmployments]

  object EnumJson {

    private class InvalidEnumException(className: String, input: String)
        extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")

    private def enumReads[E <: Enumeration](anEnum: E): Reads[E#Value] = new Reads[E#Value] {
      def reads(json: JsValue): JsResult[E#Value] = json match {
        case JsString(s) =>
          Try(JsSuccess(anEnum.withName(s))) recoverWith { case _: NoSuchElementException =>
            Failure(new InvalidEnumException(anEnum.getClass.getSimpleName, s))
          } get
        case _ => JsError("String value expected")
      }
    }

    implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
      def writes(v: E#Value): JsValue = JsString(v.toString)
    }

    implicit def enumFormat[E <: Enumeration](anEnum: E): Format[E#Value] =
      Format(enumReads(anEnum), enumWrites)
  }
}
