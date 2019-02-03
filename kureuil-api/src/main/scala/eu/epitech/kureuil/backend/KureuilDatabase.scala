package eu.epitech.kureuil
package backend

import java.util.UUID

import eu.epitech.kureuil.model.Channel

import scala.concurrent.Future

trait KureuilDatabase {

  def getUser( email: String ): Future[Option[model.User]]

  def registerUser( name: String, email: String, hash: String ): Future[Boolean]

  def getAuthUser( email: String ): Future[Option[User]]

  def getApiToken( id: UUID ): Future[Option[( ApiToken, String )]]

  def getChannels(): Future[List[model.Channel]]

  def createOrUpdate( channel: Channel ): Future[Int]

  def deleteChannel( id: Long ): Future[Int]

  def getLinks( channelId: Long ): Future[List[model.Link]]

  def close(): Future[Unit]
}
