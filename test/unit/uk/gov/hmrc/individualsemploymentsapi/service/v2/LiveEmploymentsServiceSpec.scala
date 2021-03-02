package unit.uk.gov.hmrc.individualsemploymentsapi.service.v2

import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Format
import uk.gov.hmrc.individualsemploymentsapi.connector.{IfConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{CacheIdBase, CacheService, LiveEmploymentsService, ScopesHelper}
import unit.uk.gov.hmrc.individualsemploymentsapi.util.SpecBase
import utils.Intervals

import java.util.UUID
import scala.concurrent.Future

class LiveEmploymentsServiceSpec extends SpecBase with Intervals {

  trait Setup {

    implicit val ec = scala.concurrent.ExecutionContext.global

    val mockCacheService = new CacheService(null, null)(null) {
      override def get[T: Format](cacheId: CacheIdBase,
                                  functionToCache: => Future[T]) =
        functionToCache
    }

    val mockIndividualsMatchingApiConnector = mock[IndividualsMatchingApiConnector]
    val mockIfConnector = mock[IfConnector]
    val mockScopesHelper = mock[ScopesHelper]

    val matchId = UUID.randomUUID()

    val liveEmploymentsService = new LiveEmploymentsService(
      mockIndividualsMatchingApiConnector,
      mockIfConnector,
      mockScopesHelper,
      retryDelay = 0,
      mockCacheService
    )
  }

}
