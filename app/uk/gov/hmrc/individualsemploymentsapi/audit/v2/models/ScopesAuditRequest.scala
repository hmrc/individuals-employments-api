package uk.gov.hmrc.individualsemploymentsapi.audit.v2.models

import play.api.mvc.RequestHeader

case class ScopesAuditRequest(correlationId: String,
                              matchId: String,
                              scopes:  String,
                              request: RequestHeader)
