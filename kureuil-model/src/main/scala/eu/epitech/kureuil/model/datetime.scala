package eu.epitech.kureuil

import cats.syntax.either._
import io.circe.Decoder
import io.circe.Encoder
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

object datetime {
  def parseOffset( s: String ): Either[String, OffsetDateTime] =
    Either.catchNonFatal( OffsetDateTime.parse( s ) ).leftMap( _.getMessage )

  val dateTimeEncoder: Encoder[OffsetDateTime] =
    Encoder[String].contramap( _.toString )

  val dateTimeDecoder: Decoder[OffsetDateTime] =
    Decoder[String].emap( parseOffset )

  implicit class OffsetDateTimeConversions( val self: OffsetDateTime ) extends AnyVal {
    def asTimestamp: Timestamp = Timestamp.from( self.toInstant )
  }

  implicit class TimestampConversions( val self: Timestamp ) extends AnyVal {
    def asOffsetDateTime: OffsetDateTime = OffsetDateTime.ofInstant( self.toInstant, ZoneOffset.UTC )
  }
}
