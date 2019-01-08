package eu.epitech.kureuil
package backend

import java.util.UUID

import scala.concurrent.Future

trait KureuilDatabase {
  def getUser( email: String ): Future[Option[User]]

  def getApiToken( id: UUID ): Future[Option[( ApiToken, String )]]

  def close(): Future[Unit]
}
