package eu.epitech.kureuil
package backend
package slick3

import cats.syntax.option._
import eu.epitech.kureuil.model._
import eu.epitech.kureuil.poly._
import eu.epitech.kureuil.prometheus.Metrics
import org.lyranthe.prometheus.client.Timer
import shapeless._

import scala.{collection => sc}
import scala.collection.mutable.{Map => MutMap}
import scala.collection.mutable.{Set => MutSet}
import scala.concurrent.Future

trait LinkDao { self: DbContext with TimerObserver with StreamingSupport with Tables =>

  import profile.api._

  def getLinks( channelId: Long ): Future[List[Link]] = observeDbTime( Metrics.getChannelsLatency, getAllLinks( channelId ) )

  def createTag( tag: model.Tag ): Future[Int] = observeDbTime( Metrics.putTagLatency, tags.insertOrUpdate( DbTag( tag.id, tag.name.some ) ) )

  def queryTagsByLinkId( id: Long ) =
    for {
      l <- linkTags if l.idLink === id
      t <- tags if t.id === l.idTag
    } yield t

  def toTag: scala.Seq[DbTag] => List[model.Tag] = list =>
    list.map( t => model.Tag( t.id, t.name.getOrElse("") ) ).toList

  def getTagsByLinkId( id: Long ): Future[List[model.Tag]] = observeDbTime( Metrics.getTagByLinkIdLatency, queryTagsByLinkId( id ).result.map( toTag ) )
  def getAllTags: Future[List[model.Tag]] = observeDbTime( Metrics.getTagsLatency, tags.result.map( toTag ) )

  def getLinksByChannel( channelId: Long ) =
    links

  def getLinkTags( ids: sc.Set[Long] ) =
    linkTags.filter( _.idLink inSet ids )

  def getTags( ids: sc.Set[Long] ) =
    tags.filter( _.id inSet ids )

  def getAllLinks( channelId: Long ): DmlIO[List[Link]] =
    for {
      linksBuilder <- foldStream( getLinksByChannel( channelId ).result, new LinkBuilder() )
      linksIds = linksBuilder.linksIds
      withLinkTag <- foldStream( getLinkTags( linksIds ).result, linksBuilder )
      tagsIds = withLinkTag.tagsIds
      withTags <- foldStream( getTags( tagsIds ).result, withLinkTag )
    } yield withTags.build

  private[this] class LinkBuilder() extends CasesBuilder[DbLink :+: DbLinkTag :+: DbTag :+: CNil, List[Link]] {
    private val linkById: MutMap[Long, DbLink] = MutMap()
    private val tagsIdByLink: MutMap[Long, MutSet[Long]] = MutMap()
    private val tagsById: MutMap[Long, DbTag] = MutMap()
    private val timer: Timer = Timer()
    def linksIds: sc.Set[Long] = linkById.keySet
    def tagsIds: sc.Set[Long] = tagsIdByLink.flatMap { case ( _, t ) => t }.toSet

    val toTag: DbTag => model.Tag = tag =>
      Tag( tag.id, tag.name.getOrElse(""))

    def getTagsForLink( id: Long ): List[model.Tag] =
      tagsIdByLink.getOrElse( id, sc.Set() ).toList.flatMap( id => tagsById.get( id ) ).map( toTag )

    def toLink: ((Long, DbLink)) => Link = { case (id, link) =>
      Link( link.url.getOrElse(""), getTagsForLink( id ) )
    }

    override def build: List[Link] = {
      Metrics.streamingLatency.labelValues("channeloverview").observeDuration( timer )
      linkById.toMap.map( toLink ).toList
    }

    object append extends Poly1 {
      implicit def atLink: Case.Aux[DbLink, This] = at[DbLink].apply[This]( addLink )
      implicit def atLinkTag: Case.Aux[DbLinkTag, This] = at[DbLinkTag].apply[This]( addLinkTag )
      implicit def atTag: Case.Aux[DbTag, This] = at[DbTag].apply[This]( addTag )
    }

    override val poly: append.type = append

    override val applyPoly: Cases.Aux[poly.type, Input, this.type] = implicitly

    private def addLink( link: DbLink ): this.type = {
      linkById.getOrElseUpdate( link.id, link )
      this
    }

    private def addLinkTag( item: DbLinkTag ): this.type = {
      tagsIdByLink.getOrElseUpdate( item.idLink, MutSet( item.idTag ) ) += item.idTag
      this
    }

    private def addTag( tag: DbTag ): this.type = {
      tagsById.getOrElseUpdate( tag.id, tag )
      this
    }
  }
}