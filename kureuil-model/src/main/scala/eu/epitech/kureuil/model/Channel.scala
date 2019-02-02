package eu.epitech.kureuil
package model

import cats.data.NonEmptyList
import io.circe._
import io.circe.generic.semiauto._

case class UserChannel( user: Long, isAdmin: Boolean, isSubscribed: Boolean )

object UserChannel {
  implicit def userChannelEncoder: Encoder[UserChannel] = deriveEncoder
  implicit def userChannelDecoder: Decoder[UserChannel] = deriveDecoder
}

case class Channel( id: Long, name: String, query: String, users: List[UserChannel] )

object Channel {
  implicit def channelEncoder: Encoder[Channel] = deriveEncoder
  implicit def channelDecoder: Decoder[Channel] = deriveDecoder
}
