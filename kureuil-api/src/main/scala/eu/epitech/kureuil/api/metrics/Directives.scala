package eu.epitech.kureuil
package api
package metrics

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult._
import akka.http.scaladsl.server._
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.lyranthe.prometheus.client.Timer
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
//
import prometheus.Metrics

object Directives {

  def reportMetric( mag: ReportMetricMagnet ): Directive0 = mag.directive

  private def aroundRequest[R]( pre: => R )( post: R => Unit )( implicit ec: ExecutionContext ): Directive0 = {
    extractRequestContext.flatMap { ctx =>
      val res: R = pre
      mapRouteResult {
        case c @ Complete( response ) =>
          Complete( response.mapEntity { entity =>
            if (entity.isKnownEmpty()) {
              post( res )
              entity
            } else {
              entity.transformDataBytes( Flow[ByteString].watchTermination() {
                case ( m, f ) =>
                  f.onComplete( _ => post( res ) )
                  m
              } )
            }
          } )
        case r: Rejected =>
          post( res )
          r
      }
    }
  }

  sealed trait Metric
  final case class RequestsInProgress( label: String ) extends Metric
  case object RequestTime                              extends Metric

  trait ReportMetricMagnet {
    def directive: Directive0
  }

  private class ReportRequestTimeMagnet( implicit ec: ExecutionContext ) extends ReportMetricMagnet {

    def directive: Directive0 =
      aroundRequest( Timer() )( Metrics.requestsLatencySeconds.observeDuration )
  }

  private class ReportRequestsInProgressMagnet( val label: String )( implicit ec: ExecutionContext )
      extends ReportMetricMagnet {

    private val exceptionHandler: ExceptionHandler = ExceptionHandler {
      case NonFatal( e ) =>
        ctx =>
          Metrics.inProgressRequests.labelValues( label ).dec()
          ctx.fail( e )
    }

    private def onTimeout: HttpResponse = {
      Metrics.inProgressRequests.labelValues( label ).dec()
      HttpResponse( StatusCodes.RequestTimeout, entity = "Unable to serve response within time limit." )
    }

    private val metricDirective: Directive0 =
      aroundRequest( Metrics.inProgressRequests.labelValues( label ).inc() )(
        _ => Metrics.inProgressRequests.labelValues( label ).dec()
      )

    val directive: Directive0 =
      metricDirective &
        withRequestTimeoutResponse( _ => onTimeout ) &
        handleExceptions( exceptionHandler )
  }

  object ReportMetricMagnet {

    import language.implicitConversions

    implicit def ToReportRequestTimeMagnet( ev: Metric )( implicit ec: ExecutionContext ): ReportMetricMagnet =
      ev match {
        case RequestTime                 => new ReportRequestTimeMagnet
        case RequestsInProgress( label ) => new ReportRequestsInProgressMagnet( label )
      }
  }

}
