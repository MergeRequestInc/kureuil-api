package eu.epitech.kureuil
package api
package authn

import akka.http.scaladsl.server.directives.AuthenticationDirective
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.SecurityDirectives.authenticateBasicAsync
import cats.instances.future._
import cats.instances.option._
import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.traverse._
import java.util.UUID
import mouse.boolean._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
//
import backend.ApiToken
import backend.KureuilDatabase
import backend.Identifier

class InternalAuthenticator( val backend: KureuilDatabase )( implicit ec: ExecutionContext ) extends Authenticator {

  private def log( status: String, token: ApiToken ): Unit =
    Loggers.Auth.debug( s"Auth $status (Basic) {} = {}", token.id, token.comment.getOrElse( "<no comment>" ) )

  private def checkCredentials( creds: Credentials.Provided ): Future[Option[ApiToken]] = {
    val tokenQuery: Future[Option[( ApiToken, String )]] =
      Either
        .catchNonFatal( UUID.fromString( creds.identifier ) )
        .toOption
        .flatTraverse( uuid => backend.getApiToken( uuid ) )

    tokenQuery.map(
      _.orElse( {
        Loggers.Auth.debug( s"Auth failure (Basic): Unknown token {}", creds.identifier ); none[( ApiToken, String )]
      } )
        .flatMap {
          case ( tok, secret ) =>
            val verify = creds.verify( secret )
            log( if (verify) "success" else "failure", tok )
            verify.option( tok )
        }
    )
  }

  private val authenticate: Credentials => Future[Option[ApiToken]] = {
    case cr @ Credentials.Provided( _ ) => checkCredentials( cr )

    case Credentials.Missing => Future.successful( None )
  }

  override val directive: AuthenticationDirective[Identifier] =
    authenticateBasicAsync( "kureuil-api", authenticate )

}

object InternalAuthenticator {
  def apply( backend: KureuilDatabase )( implicit ec: ExecutionContext ): Authenticator =
    new InternalAuthenticator( backend )
}
