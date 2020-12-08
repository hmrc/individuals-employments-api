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

package uk.gov.hmrc.individualsemploymentsapi.domain.v2

import org.joda.time.LocalDate
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters.EnumJson
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployment
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.PayFrequency.PayFrequency

case class EmploymentDetail(
  startDate: Option[LocalDate],
  endDate: Option[LocalDate],
  payFrequency: Option[PayFrequency])

object EmploymentDetail {

  implicit val payFrequencyFormat: Format[PayFrequency] = EnumJson.enumFormat(PayFrequency)

  implicit val format: Format[EmploymentDetail] = Format(
    (
      (JsPath \ "startDate").readNullable[LocalDate] and
        (JsPath \ "endDate").readNullable[LocalDate] and
        (JsPath \ "payFrequency").readNullable[PayFrequency]
    )(EmploymentDetail.apply _),
    (
      (JsPath \ "startDate").writeNullable[LocalDate] and
        (JsPath \ "endDate").writeNullable[LocalDate] and
        (JsPath \ "payFrequency").writeNullable[PayFrequency]
    )(unlift(EmploymentDetail.unapply))
  )

  def create(
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    payFrequency: Option[PayFrequency]): Option[EmploymentDetail] =
    (startDate, endDate, payFrequency) match {
      case (None, None, None) => None
      case _                  => Some(new EmploymentDetail(startDate, endDate, payFrequency))
    }

  def create(ifEmployment: IfEmployment): Option[EmploymentDetail] = {

    val startDate = ifEmployment.employment.flatMap(d => d.startDate).map(s => LocalDate.parse(s))
    val endDate = ifEmployment.employment.flatMap(d => d.endDate).map(s => LocalDate.parse(s))
    val payFrequency = ifEmployment.employment.flatMap(d => d.payFrequency).flatMap(PayFrequency.frowm)

    (startDate, endDate, payFrequency) match {
      case (None, None, None) => None
      case _                  => EmploymentDetail.create(startDate, endDate, payFrequency)
    }
  }

}
