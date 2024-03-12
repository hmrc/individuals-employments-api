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

import java.time.{LocalDate, LocalDateTime, LocalTime}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.ValidationException

import java.time.format.DateTimeFormatter

case class Interval(fromDate: LocalDateTime, toDate: LocalDateTime) {
  def getStart: LocalDateTime = fromDate
  def getEnd: LocalDateTime = toDate

  override def toString: String =
    s"${fromDate.format(Dates.jsonFormat)}/${toDate.format(Dates.jsonFormat)}"
}

object Dates {

  val localDatePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val jsonFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  private val desDataInceptionDate = LocalDate.parse("2013-03-31")

  def toFormattedLocalDate(date: LocalDateTime): String = localDatePattern.format(date)

  def toInterval(fromDate: LocalDate, toDate: LocalDate): Interval =
    if (fromDate.isBefore(desDataInceptionDate)) throw new ValidationException("fromDate earlier than 31st March 2013")
    else Interval(fromDate.atTime(LocalTime.MIN), toDate.atTime(0, 0, 0, 1000000))

}
