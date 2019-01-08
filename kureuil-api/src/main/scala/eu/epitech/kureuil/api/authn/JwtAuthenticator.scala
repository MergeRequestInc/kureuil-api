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
    jwt.getClaim[Iss].fold( Future.successful( none[User] ) ) { iss =>
      backend.getUser( iss.value )
    }
  }

  private def checkCredentials( token: String ): Future[Option[User]] = {
    val res = for {
      claims <- DecodedJwt.validateEncodedJwt(
                 token,
                 jwtSecretKey,
                 Algorithm.HS256,
                 Set( Typ ),
                 Set( Iss ),
                 iss = Some( Iss( "email" ) )
               )
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
