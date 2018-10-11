package eu.epitech.kureuil
package api
package cors

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import cats.data.NonEmptyList
import cats.data.ValidatedNel
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.validated._
import mouse.option._
import scala.concurrent.duration._

class Cors( allowedMethods: Seq[HttpMethod], allowedHeaders: HttpHeaderRange, allowedOrigins: HttpOriginRange ) {

  def directive: Directive0 = {
    extractRequest.flatMap { request =>
      ( request.method, request.header[Origin], request.header[`Access-Control-Request-Method`] ) match {

        case ( _, None, _ ) =>
          nonCorsRequest
        case ( HttpMethods.OPTIONS, Some( origin ), Some( accessControlRequestMethod ) ) =>
          preflightRequest(
            origin,
            accessControlRequestMethod,
            request.header[`Access-Control-Request-Headers`]
          )
        case ( _, Some( origin ), _ ) =>
          actualCorsRequest( origin )
      }
    }
  }

  def preflightRequest(
      origin: Origin,
      method: `Access-Control-Request-Method`,
      headers: Option[`Access-Control-Request-Headers`]
  ): Directive0 =
    preflightResponseHeaders( origin, method, headers ).fold(
      errs => complete( ( StatusCodes.BadRequest, "CORS: " + errs.reduceLeft( _ + ", " + _ ) ) ),
      headers => complete( HttpResponse( StatusCodes.OK, corsResponseHeaders( origin ) ++ headers ) )
    )

  def actualCorsRequest( origin: Origin ): Directive0 =
    respondWithHeaders( corsResponseHeaders( origin ) )

  def nonCorsRequest: Directive0 = pass

  private def corsResponseHeaders( origin: Origin ): Seq[HttpHeader] = Seq(
    `Access-Control-Allow-Origin`.forRange( HttpOriginRange.Default( origin.origins ) ),
    `Access-Control-Allow-Credentials`( true )
  )

  private def preflightResponseHeaders(
      origin: Origin,
      accessControlRequestMethod: `Access-Control-Request-Method`,
      accessControlRequestHeaders: Option[`Access-Control-Request-Headers`]
  ): ValidatedNel[String, Seq[HttpHeader]] =
    (
      validatePreflightOrigin( origin ),
      validatePreflightRequestMethod( accessControlRequestMethod.method ),
      validatePreflightRequestHeaders( accessControlRequestHeaders )
    ).mapN( ( _, m, h ) => h.toList ++ List( m, `Access-Control-Max-Age`( 1.day.toSeconds ) ) )

  private def validatePreflightOrigin( origin: Origin ): ValidatedNel[String, Unit] =
    if (origin.origins.exists( allowedOrigins.matches ))
      ().validNel
    else
      s"invalid origins: $origin".invalidNel

  private def validatePreflightRequestMethod(
      accessControlRequestMethod: HttpMethod
  ): ValidatedNel[String, `Access-Control-Allow-Methods`] =
    if (allowedMethods.contains( accessControlRequestMethod ))
      `Access-Control-Allow-Methods`( accessControlRequestMethod ).validNel
    else
      s"Invalid method $accessControlRequestMethod".invalidNel

  private def validatePreflightRequestHeaders(
      accessControlRequestHeadersOpt: Option[`Access-Control-Request-Headers`]
  ): ValidatedNel[String, Option[`Access-Control-Allow-Headers`]] =
    accessControlRequestHeadersOpt
      .map( _.headers )
      .cata(
        headers =>
          NonEmptyList
            .fromList( headers.filterNot( allowedHeaders.matches ).toList )
            .map( rej => s"invalid headers: ${rej.reduceLeft( _ + ", " + _ )}" )
            .toInvalidNel( allowedHeaders.allowedHeaders( headers ) ),
        none.validNel
      )
}

object Cors {

  def apply( allowedMethods: Seq[HttpMethod] ): Cors = apply( allowedMethods, HttpHeaderRange.`*` )

  def apply( allowedMethods: Seq[HttpMethod], allowedHeaders: HttpHeaderRange ): Cors =
    apply( allowedMethods, allowedHeaders, HttpOriginRange.`*` )

  def apply( allowedMethods: Seq[HttpMethod], allowedHeaders: HttpHeaderRange, allowedOrigins: HttpOriginRange ): Cors =
    new Cors( allowedMethods, allowedHeaders, allowedOrigins )

}
