package eu.epitech.kureuil
package api
package authn

import akka.http.scaladsl.server.directives.AuthenticationDirective
import scala.concurrent.ExecutionContext
//
import backend.KureuilDatabase
import backend.Identifier

trait Authenticator {
  val directive: AuthenticationDirective[Identifier]
}

object Authenticator {
  def apply( backend: KureuilDatabase )( implicit ec: ExecutionContext ): Authenticator =
    CompositeAuthenticator.fromConfig( backend )
}
