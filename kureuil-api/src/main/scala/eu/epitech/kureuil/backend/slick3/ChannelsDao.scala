package eu.epitech.kureuil
package backend
package slick3

import eu.epitech.kureuil.model._
import eu.epitech.kureuil.poly._
import eu.epitech.kureuil.prometheus.Metrics
import org.lyranthe.prometheus.client.Timer
import shapeless._

import scala.{collection => sc}
import scala.collection.mutable.{Map => MutMap}
import scala.collection.mutable.{Set => MutSet}
import scala.concurrent.Future

trait ChannelsDao { self: DbContext with TimerObserver with StreamingSupport with Tables =>

  import profile.api._

  def getChannels: Future[List[Channel]] = observeDbTime( Metrics.getChannelsLatency, getAllChannels() )

  def getUserChannels( userEmail: String ): Future[List[Channel]] =
    observeDbTime( Metrics.getChannelsLatency, buildChannelsFor( userEmail ) )

  def createOrUpdate( channel: Channel, userEmail: String ): Future[Int] =
    observeDbTime(
      Metrics.putChannelsLatency,
      createOrUpdateChannel( channel, userEmail )
    )

  def createOrUpdateChannel( channel: Channel, userEmail: String ) = {
    val insertQuery = channels returning channels.map( _.id ) into ( ( channel, id ) => channel.copy( id = id ) )
    val newChannel  = DbChannel( channel.id, channel.name, channel.query )
    for {
      user <- getUserByEmail( userEmail ).result.head
      up   <- insertQuery.insertOrUpdate( newChannel )
      ins  <- userChannels.insertOrUpdate( DbUserChannel( user.id, up.getOrElse( newChannel ).id, true, true ) )
    } yield ins
  }

  def getUserByEmail( userEmail: String ) =
    for {
      u <- users if u.email === userEmail
    } yield u

  def deleteChannel( id: Long ): Future[Int] =
    observeDbTime( Metrics.deleteChannelLatency, queryDeleteChannel( id ) )

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

  def getAllChannels(): DmlIO[List[Channel]] =
    for {
      channels <- foldStream( channels.result, new ChannelBuilder() )
      channelsIds = channels.channelsIds
      withUsers <- foldStream( getUserChannels( channelsIds ).result, channels )
    } yield withUsers.build

  def buildChannelsFor( userEmail: String ): DmlIO[List[Channel]] =
    for {
      channels <- foldStream( queryChannelsFor( userEmail ).result, new ChannelBuilder() )
      channelsIds = channels.channelsIds
      withUsers <- foldStream( getUserChannels( channelsIds ).result, channels )
    } yield withUsers.build

  private[this] class ChannelBuilder() extends CasesBuilder[DbChannel :+: DbUserChannel :+: CNil, List[Channel]] {
    private val channelById: MutMap[Long, DbChannel]                 = MutMap()
    private val usersByChannels: MutMap[Long, MutSet[DbUserChannel]] = MutMap()
    private val timer: Timer                                         = Timer()
    def channelsIds: sc.Set[Long]                                    = channelById.keySet

    def toUserChannel: DbUserChannel => UserChannel =
      userChannel => UserChannel( userChannel.idUser, userChannel.isAdmin, userChannel.isSubscribed )

    private def toChannel: ( ( Long, DbChannel ) ) => Channel = {
      case ( id, channel ) =>
        Channel(
          id,
          channel.name,
          channel.query,
          usersByChannels.getOrElse( id, MutSet[DbUserChannel]() ).toList.map( toUserChannel )
        )
    }

    override def build: List[Channel] = {
      Metrics.streamingLatency.labelValues( "channeloverview" ).observeDuration( timer )
      channelById.toMap.map( toChannel ).toList
    }

    object append extends Poly1 {
      implicit def atChannel: Case.Aux[DbChannel, This]         = at[DbChannel].apply[This]( addChannel )
      implicit def atUserChannel: Case.Aux[DbUserChannel, This] = at[DbUserChannel].apply[This]( addUserChannel )
    }

    override val poly: append.type = append

    override val applyPoly: Cases.Aux[poly.type, Input, this.type] = implicitly

    private def addChannel( channel: DbChannel ): this.type = {
      channelById.getOrElseUpdate( channel.id, channel )
      this
    }

    private def addUserChannel( user: DbUserChannel ): this.type = {
      usersByChannels.getOrElseUpdate( user.idChannel, MutSet( user ) ) += user
      this
    }

  }
}
