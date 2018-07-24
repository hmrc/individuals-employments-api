/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.config

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.{Configuration, Environment}

class ConfigModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {
    val delay = configuration.getInt("retryDelay").getOrElse(1000)
    val hmctsClientId = configuration.getString("clientIds.hmcts")
      .getOrElse(throw new RuntimeException("Missing required configuration 'clientIds.hmcts'"))

    bindConstant().annotatedWith(Names.named("retryDelay")).to(delay)
    bindConstant().annotatedWith(Names.named("hmctsClientId")).to(hmctsClientId)
  }
}
