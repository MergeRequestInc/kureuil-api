package eu.epitech.kureuil
package model

import io.circe._
import io.circe.generic.semiauto._

case class Link( id: Long, url: String, tags: List[Tag] )

object Link {
  implicit def linkEncoder: Encoder[Link] = deriveEncoder
  implicit def linkDecoder: Decoder[Link] = deriveDecoder
}
