package eu.epitech.kureuil
package api
package authn

import akka.http.scaladsl.server.directives.AuthenticationDirective
import cats.data.NonEmptyList
import scala.concurrent.ExecutionContext
//
import backend.KureuilDatabase
import backend.Identifier

class CompositeAuthenticator( authenticators: NonEmptyList[Authenticator] ) extends Authenticator {
  override val directive: AuthenticationDirective[Identifier] =
    authenticators.map( _.directive ).reduceLeft( _ | _ )
}

object CompositeAuthenticator {
  def apply( authenticators: NonEmptyList[Authenticator] ): Authenticator =
    new CompositeAuthenticator( authenticators )

  def fromConfig( backend: KureuilDatabase, jwtSecretKey: String )( implicit ec: ExecutionContext ): Authenticator = {
    val internalAuth: Authenticator     = InternalAuthenticator( backend )
    val jwtAuthenticator: Authenticator = JwtAuthenticator( backend, jwtSecretKey )
    val authenticators                  = NonEmptyList( internalAuth, List( jwtAuthenticator ) )
    new CompositeAuthenticator( authenticators )
  }
}
