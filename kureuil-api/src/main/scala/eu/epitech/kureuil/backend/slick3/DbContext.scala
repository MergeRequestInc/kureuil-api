package eu.epitech.kureuil
package backend
package slick3

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DbContext { self: Tables =>

  val dc: DatabaseConfig[_ <: JdbcProfile]

  override val profile: JdbcProfile = dc.profile

  val db: profile.Backend#Database = dc.db

  implicit val system: ActorSystem
  implicit val ec: ExecutionContext

  import profile.api._

  def runTx[A]( action: DmlIO[A] ): Future[A] =
    db.run( action.transactionally )

  def close(): Future[Unit] = Future.fromTry( Try( db.close() ) )

}
