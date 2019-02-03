package eu.epitech.kureuil
package api
package authn

import akka.http.scaladsl.server.directives.AuthenticationDirective
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.SecurityDirectives._
import cats.syntax.option._
import io.igl.jwt._

import scala.concurrent.Future
//
import backend._

class JwtAuthenticator( val backend: KureuilDatabase, jwtSecretKey: String ) extends Authenticator {

  private def getUserFromJwt( jwt: Jwt ): Future[Option[User]] = {
    jwt.getClaim[Sub].fold( Future.successful( none[User] ) ) { sub =>
      backend.getAuthUser( sub.value )
    }
  }

  private def checkCredentials( token: String ): Future[Option[User]] = {
    val res = for {
      claims <- new AuthUtils.JwtToken( jwtSecretKey ).validate( token )
    } yield getUserFromJwt( claims )
    res.getOrElse( Future.successful( none ) )
  }

  private val authenticate: AsyncAuthenticator[User] = {
    case Credentials.Provided( tok ) => checkCredentials( tok )

    case Credentials.Missing => Future.successful( None )
  }

  override val directive: AuthenticationDirective[Identifier] =
    authenticateOAuth2Async( "kureuil-api", authenticate )

}

object JwtAuthenticator {
  def apply( backend: KureuilDatabase, jwtSecret: String ): Authenticator =
    new JwtAuthenticator( backend, jwtSecret )
}
