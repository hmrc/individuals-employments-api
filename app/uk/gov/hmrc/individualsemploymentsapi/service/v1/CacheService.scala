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

package uk.gov.hmrc.individualsemploymentsapi.service.v1

import play.api.libs.json.Format
import uk.gov.hmrc.individualsemploymentsapi.cache.v1.{CacheRepositoryConfiguration, ShortLivedCache}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(cachingClient: ShortLivedCache, conf: CacheRepositoryConfiguration)(
  implicit ec: ExecutionContext) {

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def get[T: Format](cacheId: String, fallbackFunction: => Future[T]): Future[T] =
    if (cacheEnabled)
      cachingClient.fetchAndGetEntry[T](cacheId) flatMap {
        case Some(value) =>
          Future.successful(value)
        case None =>
          fallbackFunction map { result =>
            cachingClient.cache(cacheId, result)
            result
          }
      } else {
      fallbackFunction
    }

}
