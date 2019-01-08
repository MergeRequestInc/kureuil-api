package eu.epitech.kureuil
package api

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server._
import eu.epitech.kureuil.backend.Identifier
import scala.concurrent.ExecutionContext
//
import authn.Authenticator
import backend.KureuilDatabase
import cors.Cors
import cors.HttpHeaderRange
import logs.Logging

class Api(
    val mainRoute: Identifier => Route,
    val authenticator: Directive1[Identifier],
    val logging: Directive0
)( implicit ec: ExecutionContext ) {

  import akka.http.scaladsl.coding._
  import akka.http.scaladsl.server.Directives._
  //
  import metrics.Directives._

  val cors: Cors = Cors(
    Seq( HttpMethods.GET, HttpMethods.POST ),
    HttpHeaderRange( `Accept-Encoding`, `Authorization`, `Content-Type`, `Content-Length` )
  )

  val mainDirective: Directive0 =
    reportMetric( RequestTime ) &
      logging &
      cors.directive &
      encodeResponseWith( Gzip, Deflate )

  val route: Route = mainDirective {
    {
      authenticator { id =>
        mainRoute( id )
      } ~ complete( StatusCodes.Unauthorized )
    }
  }

}

object Api {
  def apply( config: ApiConfig, mainBackend: KureuilDatabase )(
      implicit ec: ExecutionContext
  ): Api = {

    val mainRoutes = new MainRoutes( mainBackend )

    val logging       = new Logging( config.debug )
    val authenticator = Authenticator( mainBackend, config.jwtSecretKey )

    new Api( mainRoutes.route, authenticator.directive, logging.directive )
  }
}
