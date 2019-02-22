/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.service

import javax.inject.Inject
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.cache.{CacheConfiguration, ShortLivedCache}

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(cachingClient: ShortLivedCache, conf: CacheConfiguration)
                            (implicit ec: ExecutionContext) {

  def get[T: Format](cacheId: String, functionToCache: => Future[T])(implicit hc: HeaderCarrier): Future[T] = {
    if (conf.cacheEnabled) {
      cachingClient.fetchAndGetEntry[T](cacheId, "paye-income") flatMap {
        case Some(value) => Future.successful(value)
        case None => functionToCache map { res =>
          cachingClient.cache(cacheId, "paye-income", res)
          res
        }
      }
    } else {
      functionToCache
    }
  }
}
