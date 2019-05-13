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

  def getChannels: Future[List[model.Channel]]

  def getUserChannels( userEmail: String ): Future[List[model.Channel]]

  def createChannel( channel: Channel, userEmail: String ): Future[Int]

  def channelExists( id: Long ): Future[Option[model.Channel]]

  def deleteChannel( id: Long ): Future[Int]

  def createTag( tag: model.Tag ): Future[Int]

  def getTagsByLinkId( id: Long ): Future[List[model.Tag]]

  def getAllTags: Future[List[model.Tag]]

  def getLinks( channelQuery: String ): Future[List[model.Link]]

  def getAllLinks: Future[List[model.Link]]

  def createOrUpdateLink( link: model.Link ): Future[Unit]

  def linkExisting( link: model.Link ): Future[Option[model.Link]]

  def getLink( linkId: Long ): Future[model.Link]

  def deleteLink( linkId: Long ): Future[Unit]

  def close(): Future[Unit]
}
