package eu.epitech.kureuil
package backend

import java.util.UUID

sealed trait Permission
object Permission {
  case object Read  extends Permission
  case object Write extends Permission
  case object Admin extends Permission
}

sealed trait Identifier {
  def id: String
  def comment: Option[String]
  def permissions: Set[Permission]
  def hasPermission( p: Permission ): Boolean = permissions.contains( p )
}

case class ApiToken( uid: UUID, comment: Option[String], permissions: Set[Permission] ) extends Identifier {

  override val id: String = uid.toString
}

case class User( login: String, permissions: Set[Permission] ) extends Identifier {

  override val id: String              = login
  override val comment: Option[String] = None
}
