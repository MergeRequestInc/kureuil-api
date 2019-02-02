package eu.epitech.kureuil
package model

import cats.data.NonEmptyList
import io.circe._
import io.circe.generic.semiauto._

case class Link( url: String, tags: NonEmptyList[Tag] )

object Link {
  implicit def linkEncoder: Encoder[Link] = deriveEncoder
  implicit def linkDecoder: Decoder[Link] = deriveDecoder
}
