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

package uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{maxLength, minLength, pattern}
import play.api.libs.json.{Format, JsPath, Json}

import scala.util.matching.Regex

case class IfEmploymentDetail(
  startDate: Option[String],
  endDate: Option[String],
  payFrequency: Option[String],
  payrollId: Option[String],
  address: Option[IfAddress]
)

object IfEmploymentDetail {

  private def datePattern: Regex =
    ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
      "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
      "(0[1-9]|1[0-9]|2[0-8])))$").r

  private val payFrequencyPattern: Regex =
    "^(W1|W2|W4|M1|M3|M6|MA|IO|IR)$".r

  implicit val employmentDetailFormat: Format[IfEmploymentDetail] = Format(
    (
      (JsPath \ "startDate").readNullable[String](using pattern(datePattern, "Date format is incorrect")) and
        (JsPath \ "endDate").readNullable[String](using pattern(datePattern, "Date format is incorrect")) and
        (JsPath \ "payFrequency").readNullable[String](using
          pattern(payFrequencyPattern, "Pay frequency must be one of: W1, W2, W4, M1, M3, M6, MA, IO, IR")
        ) and
        (JsPath \ "payrollId").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "address").readNullable[IfAddress]
    )(IfEmploymentDetail.apply),
    Json.writes[IfEmploymentDetail]
  )
}
