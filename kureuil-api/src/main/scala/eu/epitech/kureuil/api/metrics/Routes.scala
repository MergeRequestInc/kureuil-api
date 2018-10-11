package eu.epitech.kureuil
package api
package metrics

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
//
import prometheus.Metrics

object Routes {

  val metricsRoute: Route = {
    import directives.RouteDirectives.complete

    val mediaType =
      MediaTypes.`text/plain`
        .withParams( Map( "version" -> "0.0.4" ) )
        .withCharset( HttpCharsets.`UTF-8` )

    def metricsResponse =
      HttpResponse(
        entity = HttpEntity(
          mediaType,
          s"${Metrics.defaultRegistry.outputText}\n${Metrics.kamonReporter.scrapeData()}"
        )
      )

    complete( metricsResponse )
  }

}
