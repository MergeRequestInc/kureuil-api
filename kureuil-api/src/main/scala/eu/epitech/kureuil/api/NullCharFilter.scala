package eu.epitech.kureuil
package api

import io.circe.Decoder
import io.circe.Json
import io.circe.parser
import monocle.function.Plated
//
import compat.CirceOptics._

object NullCharFilter {
  val filterNullChars1: Json => Json = jsonString.modify( _.filter( _ != '\u0000' ) )
  val filterNullChars: Json => Json  = Plated.transform( filterNullChars1 )

  implicit def nullCharFiltering[A]( implicit decode: Decoder[A] ): Decoder[NullCharFilter[A]] =
    Decoder
      .instance( hc => decode( filterNullChars( hc.value ).hcursor ) )
      .map( new NullCharFilter( _ ) )

  def unmarshalJson[A]( string: String )( implicit dec: Decoder[A] ): A =
    parser.decode[NullCharFilter[A]]( string ).fold( m => throw m, _.filterNulls )
}

class NullCharFilter[A]( val filterNulls: A ) extends AnyVal
