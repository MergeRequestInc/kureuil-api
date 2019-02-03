package eu.epitech.kureuil
package backend
package slick3

import akka.actor.ActorSystem
import cats.syntax.either._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
//

abstract class Backend(
    override val dc: DatabaseConfig[JdbcProfile],
    implicit override val system: ActorSystem
) extends KureuilDatabase
    with TimerObserver
    with Tables
    with ApiTokensDao
    with AuthDao
    with ChannelsDao
    with LinkDao
    with Queries
    with StreamingSupport {
  override implicit val ec: ExecutionContext = system.dispatcher
}

object Backend {
  class PostgresBackend( dc: DatabaseConfig[JdbcProfile] )( implicit system: ActorSystem )
      extends Backend( dc, system )
      with PostgresEnumSupport

  def init( config: Config )( implicit system: ActorSystem ): Either[Throwable, KureuilDatabase] =
    Either.catchNonFatal( DatabaseConfig.forConfig[JdbcProfile]( "", config ) ).map( new PostgresBackend( _ ) )

}
