package eu.epitech.kureuil
package backend
package slick3

import scala.{collection => sc}

trait ChannelsQueries { self: Queries with DbContext with TimerObserver with StreamingSupport with Tables =>

  import profile.api._

  def updateOrInsertChannelName = new UpdateOrInsert[Long, String, DbChannel, DbChannels](
    channels,
    _.id,
    (v: DbChannel) => (r: DbChannels) => r.id === v.id,
    ( v, k ) => v.copy( id = k ),
    _.name,
    _.name
  )

  def updateOrInsertChannelQuery = new UpdateOrInsert[Long, String, DbChannel, DbChannels](
    channels,
    _.id,
    (v: DbChannel) => (r: DbChannels) => r.id === v.id,
    ( v, k ) => v.copy( id = k ),
    _.query,
    _.query
  )

  def getUserByEmail( userEmail: String ) =
    for {
      u <- users if u.email === userEmail
    } yield u

  def queryDeleteChannel( id: Long ) =
    for {
      _ <- userChannels.filter( _.idChannel === id ).delete
      c <- channels.filter( _.id === id ).delete
    } yield c

  def getUserChannels( ids: sc.Set[Long] ): Query[DbUserChannels, DbUserChannel, sc.Seq] =
    userChannels.filter( _.idChannel inSet ids )

  def queryChannelsFor( userEmail: String ) =
    for {
      u  <- users if u.email === userEmail
      uc <- userChannels if uc.idUser === u.id
      c  <- channels if c.id === uc.idChannel
    } yield c

}
