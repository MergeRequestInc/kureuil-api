package eu.epitech.kureuil

import akka.http.scaladsl.marshalling.PredefinedToEntityMarshallers
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.util.Tuple
import io.circe._
//

package object api {
  implicit def jsonMarshaller[A]( implicit encode: Encoder[A] ): ToEntityMarshaller[A] =
    PredefinedToEntityMarshallers
      .stringMarshaller( MediaTypes.`application/json` )
      .compose( (a: A) => encode( a ).spaces2 )

  implicit def jsonEntityUnmarshaller[A]( implicit decode: Decoder[A] ): FromEntityUnmarshaller[A] =
    PredefinedFromEntityUnmarshallers.stringUnmarshaller
      .map( NullCharFilter.unmarshalJson[A] )

  implicit class ResponseOptionOps[A]( val self: Option[A] ) extends AnyVal {
    def orElseNotFound( implicit M: ToResponseMarshaller[A] ): ToResponseMarshallable = self match {
      case Some( v ) => ToResponseMarshallable( v )( M )
      case None      => StatusCodes.NotFound
    }
  }

  def status[L: Tuple]( code: StatusCode ): Directive[L] = Directives.complete( code )

}
