package eu.epitech.kureuil.backend
package slick3

import eu.epitech.kureuil.model
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

  def getChannels(): Future[List[Channel]] = observeDbTime( Metrics.getChannelsLatency, getAllChannels() )

  def getUserChannels( ids: sc.Set[Long] ): Query[DbUserChannels, DbUserChannel, Seq] =
    userChannels.filter( _.idChannel inSet ids )

  def getAllChannels(): DmlIO[List[Channel]] =
    for {
      channels <- foldStream( channels.result, new ChannelBuilder() )
      channelsIds = channels.channelsIds
      withUsers <- foldStream( getUserChannels(channelsIds).result, channels )
    } yield withUsers.build


  private[this] class ChannelBuilder() extends CasesBuilder[DbChannel :+: DbUserChannel :+: CNil, List[Channel]] {
    private val channelById: MutMap[Long, DbChannel] = MutMap()
    private val usersByChannels: MutMap[Long, MutSet[DbUserChannel]] = MutMap()
    private val timer: Timer = Timer()
    def channelsIds: sc.Set[Long] = channelById.keySet

    def toUserChannel: DbUserChannel => UserChannel = userChannel =>
      UserChannel( userChannel.idUser, userChannel.isAdmin, userChannel.isSubscribed )

    private def toChannel: ((Long, DbChannel)) => Channel = {
      case ( id, channel ) =>
      Channel( id,
              channel.name,
              channel.query,
              usersByChannels.getOrElse( id, MutSet[DbUserChannel]() ).toList.map( toUserChannel ) )
    }

    override def build: List[Channel] = {
      Metrics.streamingLatency.labelValues("channeloverview").observeDuration( timer )
      channelById.toMap.map(toChannel).toList
    }

    object append extends Poly1 {
      implicit def atChannel: Case.Aux[DbChannel, This] = at[DbChannel].apply[This]( addChannel )
      implicit def atUserChannel: Case.Aux[DbUserChannel, This] = at[DbUserChannel].apply[This]( addUserChannel )
    }

    override val poly: append.type = append

    override val applyPoly: Cases.Aux[poly.type, Input, this.type] = implicitly

    private def addChannel( channel: DbChannel ): this.type = {
      channelById.getOrElseUpdate( channel.id, channel )
      this
    }

    private def addUserChannel( user: DbUserChannel ): this.type = {
      usersByChannels.getOrElseUpdate( user.idChannel, MutSet(user) ) += user
      this
    }

  }
}