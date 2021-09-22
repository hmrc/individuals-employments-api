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

package unit.uk.gov.hmrc.individualsemploymentsapi.service.v2

import java.util.UUID

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.joda.time.{Interval, LocalDate}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{verify, verifyNoMoreInteractions}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.cache.v2.{CacheRepositoryConfiguration, ShortLivedCache}
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{CacheId, CacheIdBase, CacheService}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class CacheServiceSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val cacheId = TestCacheId("foo")
  val cachedValue = TestClass("cached value")
  val newValue = TestClass("new value")

  trait Setup {
    val mockClient = mock[ShortLivedCache]
    val mockCacheConfig = mock[CacheRepositoryConfiguration]
    implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    lazy val cacheService = new CacheService(mockClient, mockCacheConfig)

    given(mockCacheConfig.cacheEnabled).willReturn(true)
  }

  "cacheService.get" should {

    "return the cached value for a given id and key" in new Setup {

      given(mockClient.fetchAndGetEntry[TestClass](eqTo(cacheId.id))(any()))
        .willReturn(Future.successful(Some(cachedValue)))

      await(cacheService.get[TestClass](cacheId, Future.successful(newValue))) shouldBe cachedValue

    }

    "cache the result of the fallback function when no cached value exists for a given id and key" in new Setup {

      given(mockClient.fetchAndGetEntry[TestClass](eqTo(cacheId.id))(any()))
        .willReturn(Future.successful(None))

      await(cacheService.get[TestClass](cacheId, Future.successful(newValue))) shouldBe newValue
      verify(mockClient).cache[TestClass](eqTo(cacheId.id), eqTo(newValue))(any())

    }

    "ignore the cache when caching is not enabled" in new Setup {

      given(mockCacheConfig.cacheEnabled).willReturn(false)

      await(cacheService.get[TestClass](cacheId, Future.successful(newValue))) shouldBe newValue
      verifyNoMoreInteractions(mockClient)

    }
  }

  "CacheId" should {

    "produce a cache id based on matchId and scopes" in {

      val matchId = UUID.randomUUID()
      val fromDateString = "2017-03-02"
      val toDateString = "2017-05-31"

      val interval = new Interval(
        new LocalDate(fromDateString).toDateTimeAtStartOfDay,
        new LocalDate(toDateString).toDateTimeAtStartOfDay)

      val fields = "ABDFH"

      CacheId(matchId, interval, fields).id shouldBe
        s"$matchId-${interval.getStart}-${interval.getEnd}-ABDFH"

    }

    "produce cacheId based on matchid, scopes, and employer ref" in {

      val matchId = UUID.randomUUID()
      val fromDateString = "2017-03-02"
      val toDateString = "2017-05-31"

      val interval = new Interval(
        new LocalDate(fromDateString).toDateTimeAtStartOfDay,
        new LocalDate(toDateString).toDateTimeAtStartOfDay)

      val fields = "ABDFH"

      val filters = Some("employerRef")

      val encodedFilter = BaseEncoding.base64().encode(filters.get.getBytes(Charsets.UTF_8))

      CacheId(matchId, interval, fields, filters).id shouldBe
        s"$matchId-${interval.getStart}-${interval.getEnd}-ABDFH-$encodedFilter"
    }

  }
}

case class TestCacheId(id: String) extends CacheIdBase

case class TestClass(value: String)

object TestClass {

  implicit val format: OFormat[TestClass] = Json.format[TestClass]

}
