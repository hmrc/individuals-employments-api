package uk.gov.hmrc.individualsemploymentsapi.audit.v2.events

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.HttpExtendedAuditEvent
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.models.{ApiResponseEventModel, ScopesAuditEventModel, ScopesAuditRequest}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

class ScopesAuditEvent @Inject()(httpAuditEvent: HttpExtendedAuditEvent) {

  import httpAuditEvent.extendedDataEvent

  def auditType = "ApiResponseEvent"
  def transactionName = "AuditCall"
  def apiVersion = "2.0"

  def apply(
             correlationId: String,
             matchId : String,
             scopes : String,
             request: RequestHeader)(
             implicit hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)
           ): ExtendedDataEvent = {

    val event = extendedDataEvent(
      auditType,
      transactionName,
      request,
      Json.toJson(ScopesAuditEventModel(apiVersion, matchId, correlationId, scopes)))

    Logger.debug(s"$auditType - AuditEvent: $event")

    event

  }
}
