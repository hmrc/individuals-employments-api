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

package uk.gov.hmrc.individualsemploymentsapi.service.v2

import play.api.hal.Hal.linksSeq
import play.api.hal.{HalLink, HalResource}
import uk.gov.hmrc.individualsemploymentsapi.config.EndpointConfig
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MissingQueryParameterException

import java.util.UUID
import javax.inject.Inject

class ScopesHelper @Inject() (scopesService: ScopesService) {

  /** @param scopes
    *   The list of scopes associated with the user
    * @param endpoints
    *   The endpoint that the user has called
    * @return
    *   A google fields-style query string with the fields determined by the provided endpoint(s) and scopes
    */
  def getQueryStringFor(scopes: Iterable[String], endpoints: List[String]): String = {
    val filters = scopesService.getValidFilters(scopes, endpoints)
    s"${PathTree(scopesService.getIfDataPaths(scopes, endpoints)).toString}${if (filters.nonEmpty)
        s"&filter=${filters.mkString("&filter=")}"
      else ""}"
  }

  private def verifyRequiredParameters(
    scopes: List[String],
    endpoint: String,
    parameters: Map[String, String]
  ): Iterable[String] = {
    val filterParameterMappings = scopesService.getFilterToken(scopes, endpoint)
    if (filterParameterMappings.isEmpty)
      Seq()
    else
      filterParameterMappings.values
        .filter(s => parameters.get(s).isEmpty)
        .map(s => s"$s is required for the scopes you have been assigned")
  }

  private def updateString(queryString: String, tokens: List[(String, String)]): String =
    if (tokens.isEmpty)
      queryString
    else
      updateString(queryString.replace(s"<${tokens.head._1}>", tokens.head._2), tokens.tail)

  /** @param scopes
    *   the authorised scopes of the user
    * @param endpoint
    *   the endpoint for which to generate a query string
    * @param tokens
    *   a map of tokens in the query string to the parameters with which to replace them
    * @return
    */
  def getParameterisedQueryStringFor(scopes: List[String], endpoint: String, tokens: Map[String, String]): String = {

    val paramErrors = verifyRequiredParameters(scopes, endpoint, tokens)

    if (paramErrors.nonEmpty)
      throw new MissingQueryParameterException(paramErrors.head)

    if (tokens.isEmpty) {
      getQueryStringFor(scopes, List(endpoint))
    } else {
      updateString(getQueryStringFor(scopes, List(endpoint)), tokens.toList)
    }
  }

  def getHalLinks(
    matchId: UUID,
    excludeList: Option[List[String]],
    scopes: Iterable[String],
    allowedList: Option[List[String]]
  ): HalResource = {

    val links = getAllHalLinks(matchId, excludeList, allowedList, () => scopesService.getInternalEndpoints(scopes))

    linksSeq(links)
  }

  private def getAllHalLinks(
    matchId: UUID,
    excludeList: Option[List[String]],
    allowedList: Option[List[String]],
    getEndpoints: () => Iterable[EndpointConfig]
  ): Seq[HalLink] =
    getEndpoints()
      .filter(c =>
        !excludeList.getOrElse(List()).contains(c.name) &&
          allowedList.getOrElse(getEndpoints().map(e => e.name).toList).contains(c.name)
      )
      .map(endpoint =>
        HalLink(
          rel = endpoint.name,
          href = endpoint.link.replaceAll("<matchId>", s"$matchId"),
          title = Some(endpoint.title)
        )
      )
      .toSeq
}
