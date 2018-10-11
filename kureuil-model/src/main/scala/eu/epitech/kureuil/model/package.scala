package eu.epitech.kureuil

import io.circe._
import java.time.OffsetDateTime

package object model {

  implicit val dateTimeEncoder: Encoder[OffsetDateTime] = datetime.dateTimeEncoder
  implicit val dateTimeDecoder: Decoder[OffsetDateTime] = datetime.dateTimeDecoder

}
