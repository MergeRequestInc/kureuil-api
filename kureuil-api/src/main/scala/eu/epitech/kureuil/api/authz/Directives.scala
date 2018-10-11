package eu.epitech.kureuil
package api
package authz

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.authorize
import akka.http.scaladsl.server.Directives.mapRequestContext
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives.provide
import akka.http.scaladsl.server.Directives.reject
import akka.http.scaladsl.server.PathMatcher
//
import backend.Identifier
import backend.Permission
import backend.User

object Directives {
  def requirePermission( u: Identifier, p: Permission ): Directive0 =
    authorize( u.hasPermission( p ) )

  def requireUserAuthentication( u: Identifier ): Directive1[String] = u match {
    case User( login, _ ) => provide( login )
    case _                => reject( AuthorizationFailedRejection )
  }

  def requireSegmentPrefix( segment: PathMatcher[Unit] ): Directive0 =
    path( segment ) | (
      pathPrefix( segment./ ) &
        mapRequestContext( ctx => ctx.withUnmatchedPath( Path.Slash( ctx.unmatchedPath ) ) )
    )
}
