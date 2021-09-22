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

package component.uk.gov.hmrc.individualsemploymentsapi.v2

import component.uk.gov.hmrc.individualsemploymentsapi.stubs.BaseSpec
import uk.gov.hmrc.individualsemploymentsapi.service.v2.ScopesHelper

class IfQueriesSpec extends BaseSpec {

  Feature("Query strings for 'paye' endpoint") {

    val helper: ScopesHelper = app.injector.instanceOf[ScopesHelper]

    Scenario("For read:individuals-employments-laa-c1") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c1"), List("paye"))
      queryString shouldBe "employments(employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-laa-c2") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c2"), List("paye"))
      queryString shouldBe "employments(employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-laa-c3") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c3"), List("paye"))
      queryString shouldBe "employments(employer(name),employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-laa-c4") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c4"), List("paye"))
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-hmcts-c2") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-hmcts-c2"), List("paye"))
      queryString shouldBe "employments(employment(endDate))"
    }

    Scenario("For read:individuals-employments-hmcts-c3") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-hmcts-c3"), List("paye"))
      queryString shouldBe "employments(employment(endDate))"
    }

    Scenario("For read:individuals-employments-hmcts-c4") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-hmcts-c4"), List("paye"))
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-lsani-c1") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-lsani-c1"), List("paye"))
      queryString shouldBe "employments(employer(name),employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-lsani-c3") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-lsani-c3"), List("paye"))
      queryString shouldBe "employments(employer(name),employment(endDate,startDate))"
    }

    Scenario("For read:individuals-employments-nictsejo-c4") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-nictsejo-c4"), List("paye"))
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(startDate))"
    }

    Scenario("For read:individuals-employments-ho-ecp") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-ho-ecp"), List("paye"))
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))"
    }
    Scenario("For read:individuals-employments-ho-rp2") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-ho-rp2"), List("paye"))
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))&filter=employments%5B%5D/employerRef%20eq%20'<payeReference>'"
    }
  }
}
