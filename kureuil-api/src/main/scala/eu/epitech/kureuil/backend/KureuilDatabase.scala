package eu.epitech.kureuil
package backend

import java.util.UUID

import scala.concurrent.Future

trait KureuilDatabase {

  def getUser( email: String ): Future[Option[model.User]]

  def registerUser( name: String, email: String, hash: String ): Future[Boolean]

  def getAuthUser( email: String ): Future[Option[User]]

  def getApiToken( id: UUID ): Future[Option[( ApiToken, String )]]

  def close(): Future[Unit]
}
