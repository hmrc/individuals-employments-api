/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{Format, JsPath}
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters.EnumJson
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfEmployment
import play.api.libs.functional.syntax._
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.PayFrequency.PayFrequency

case class Employment(
  startDate: Option[LocalDate],
  endDate: Option[LocalDate],
  payFrequency: Option[PayFrequency],
  employer: Option[Employer])

object Employment {

  implicit val payFrequencyFormat: Format[PayFrequency] = EnumJson.enumFormat(PayFrequency)

  implicit val format: Format[Employment] = Format(
    (
      (JsPath \ "startDate").readNullable[LocalDate] and
        (JsPath \ "endDate").readNullable[LocalDate] and
        (JsPath \ "payFrequency").readNullable[PayFrequency] and
        (JsPath \ "employer").readNullable[Employer]
    )(Employment.apply _),
    (
      (JsPath \ "startDate").writeNullable[LocalDate] and
        (JsPath \ "endDate").writeNullable[LocalDate] and
        (JsPath \ "payFrequency").writeNullable[PayFrequency] and
        (JsPath \ "employer").writeNullable[Employer]
    )(unlift(Employment.unapply))
  )

  def create(
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    payFrequency: Option[PayFrequency],
    employer: Option[Employer]): Option[Employment] =
    (startDate, endDate, payFrequency, employer) match {
      case (None, None, None, None) => None
      case _                        => Some(new Employment(startDate, endDate, payFrequency, employer))
    }

  def create(ifEmployment: IfEmployment): Option[Employment] = {

    val employer: Option[Employer] = Employer.create(ifEmployment)
    val startDate = ifEmployment.employment.flatMap(d => d.startDate).map(s => LocalDate.parse(s))
    val endDate = ifEmployment.employment.flatMap(d => d.endDate).map(s => LocalDate.parse(s))
    val payFrequency = ifEmployment.employment.flatMap(d => d.payFrequency).flatMap(PayFrequency.from)

    (startDate, endDate, payFrequency, employer) match {
      case (None, None, None, None) => None
      case _                        => Some(new Employment(startDate, endDate, payFrequency, employer))
    }
  }
}
