package eu.epitech.kureuil
package backend
package slick3

import java.util.UUID
import scala.concurrent.Future

trait ApiTokensDao { self: DbContext with Tables =>

  import profile.api._

  def getApiToken( id: UUID ): Future[Option[( ApiToken, String )]] = runTx {
    apiTokens
      .filter( p => p.uid === id )
      .result
      .headOption
      .map( _.map( toApiToken ) )
  }

  def toApiToken( dbUser: DbApiToken ): ( ApiToken, String ) = dbUser match {
    case DbApiToken( _, uid, secret, r, w, a, comment ) =>
      import Permission._
      val perms = for {
        ( v, p ) <- List( r, w, a ).zip( List( Read, Write, Admin ) ) if v
      } yield p

      ApiToken( uid, comment, perms.toSet ) -> secret
  }

}
