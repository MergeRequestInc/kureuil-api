package eu.epitech.kureuil
package model

import io.circe._
import io.circe.generic.semiauto._

case class Tag( id: Long, name: String )

object Tag {
  implicit def tagEncoder: Encoder[Tag] = deriveEncoder
  implicit def tagDecoder: Decoder[Tag] = deriveDecoder
}