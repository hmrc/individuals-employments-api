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

  feature("Query strings for 'paye' endpoint") {

    val helper: ScopesHelper = app.injector.instanceOf[ScopesHelper]

    scenario("For read:individuals-employments-laa-c1") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c1"), "paye")
      queryString shouldBe "employments(employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-laa-c2") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c2"), "paye")
      queryString shouldBe "employments(employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-laa-c3") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c3"), "paye")
      queryString shouldBe "employments(employer(name),employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-laa-c4") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-laa-c4"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-hmcts-c2") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-hmcts-c2"), "paye")
      queryString shouldBe "employments(employment(endDate))"
    }

    scenario("For read:individuals-employments-hmcts-c3") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-hmcts-c3"), "paye")
      queryString shouldBe "employments(employment(endDate))"
    }

    scenario("For read:individuals-employments-hmcts-c4") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-hmcts-c4"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),districtNumber,name,schemeRef),employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-lsani-c1") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-lsani-c1"), "paye")
      queryString shouldBe "employments(employer(name),employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-lsani-c3") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-lsani-c3"), "paye")
      queryString shouldBe "employments(employer(name),employment(endDate,startDate))"
    }

    scenario("For read:individuals-employments-nictsejo-c4") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-nictsejo-c4"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(startDate))"
    }

    scenario("For read:individuals-employments-ho-ecp-application") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-ho-ecp-application"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),employerRef,name),employment(endDate,startDate),payments(date,paidTaxablePay))"
    }
    scenario("For read:individuals-employments-ho-ecp-compliance") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-ho-ecp-compliance"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),employerRef,name),employment(endDate,startDate),payments(date,paidTaxablePay))"
    }
    scenario("For read:individuals-employments-ho-rp2-application") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-ho-rp2-application"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),employerRef,name),employment(endDate,startDate),payments(date,paidTaxablePay))&filter=employments%5B%5D/employer/employerRef eq '<employerRef>'"
    }
    scenario("For read:individuals-employments-ho-rp2-compliance") {
      val queryString = helper.getQueryStringFor(Seq("read:individuals-employments-ho-rp2-compliance"), "paye")
      queryString shouldBe "employments(employer(address(line1,line2,line3,line4,line5,postcode),employerRef,name),employment(endDate,startDate),payments(date,paidTaxablePay))&filter=employments%5B%5D/employer/employerRef eq '<employerRef>'"
    }
  }
}
