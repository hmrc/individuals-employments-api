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

package uk.gov.hmrc.individualsemploymentsapi.service.v2

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding

import java.util.UUID
import javax.inject.Inject
import org.joda.time.Interval
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.cache.v2.{CacheConfiguration, ShortLivedCache}

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(cachingClient: ShortLivedCache, conf: CacheConfiguration)(implicit ec: ExecutionContext) {

  def get[T: Format](cacheId: CacheIdBase, functionToCache: => Future[T])(implicit hc: HeaderCarrier): Future[T] =
    if (conf.cacheEnabled) {
      cachingClient.fetchAndGetEntry[T](cacheId.id, conf.key) flatMap {
        case Some(value) => Future.successful(value)
        case None =>
          functionToCache map { res =>
            cachingClient.cache(cacheId.id, conf.key, res)
            res
          }
      }
    } else {
      functionToCache
    }
}

// Cache ID implementations
// This can then be concatenated for multiple scopes.
// Example;
// read:scope-1 =  [A, B, C]
// read:scope-2 = [D, E, F]
// The cache key (if two scopes alone) would be;
// `id + from + to +  [A, B, C, D, E, F]` Or formatted to `id-from-to-ABCDEF`
// The `fields` param is obtained with scopeService.getValidFieldsForCacheKey(scopes: List[String])

trait CacheIdBase {
  val id: String

  override def toString: String = id

  def encodeVal(toEncode: String): String =
    BaseEncoding.base64().encode(toEncode.getBytes(Charsets.UTF_8))
}

case class CacheId(matchId: UUID, interval: Interval, fields: String, empRef: Option[String] = None) extends CacheIdBase {

  val empRefKey: String = empRef match {
    case Some(value) => s"-${encodeVal(value)}"
    case None => ""
  }

  lazy val id: String = s"$matchId-${interval.getStart}-${interval.getEnd}-$fields$empRefKey"

}
