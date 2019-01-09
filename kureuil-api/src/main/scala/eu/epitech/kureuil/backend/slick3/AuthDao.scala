package eu.epitech.kureuil
package backend
package slick3

import scala.concurrent.Future

trait AuthDao { self: DbContext with Tables =>

  import profile.api._

  def toUsersWithPermissions: DbUser => User = { user =>
    if (user.admin) {
      User( user.email, Set( Permission.Read, Permission.Write, Permission.Admin ) )
    } else {
      User( user.email, Set( Permission.Read, Permission.Write ) )
    }
  }

  def toUser: DbUser => model.User = { user =>
    model.User( user.name, user.email, user.password, user.admin )
  }

  def getUser( email: String ): Future[Option[model.User]] = runTx {
    users.filter( p => p.email === email ).result.headOption.map( _.map( toUser ) )
  }

  def registerUser( name: String, email: String, hash: String ): Future[Boolean] = runTx {
    for {
      u <- users ++= Seq( DbUser( 0, name, email, hash ) )
    } yield u.isDefined
  }

  def getAuthUser( email: String ): Future[Option[User]] = runTx {
    users
      .filter( p => p.email === email )
      .result
      .headOption
      .map( _.map( toUsersWithPermissions ) )
  }

}
