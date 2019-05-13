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

trait LinkDao { self: Queries with DbContext with TimerObserver with StreamingSupport with Tables with LinkQueries =>

  import profile.api._

  def getLinks( channelQuery: String ): Future[List[Link]] =
    observeDbTime( Metrics.getLinksLatency, getAllLinks( channelQuery ) )

  def getLink( linkId: Long ): Future[Link] = observeDbTime( Metrics.getLinksLatency, getLinksById( linkId ) )

  def getAllLinks: Future[List[Link]] = observeDbTime( Metrics.getLinksLatency, buildAllLinks )

  def deleteLink( linkId: Long ): Future[Unit] = observeDbTime( Metrics.deleteLinkLatency, deleteLinkById( linkId ) )

  def createOrUpdateLink( link: model.Link ): Future[Unit] =
    observeDbTime( Metrics.putLinkLatency, upsertLink( link ) )

  def linkExisting( link: model.Link ): Future[Option[Link]] =
    observeDbTime(
      Metrics.putLinkLatency,
      getLinkFromModel( link ).result.headOption
        .map( opt => opt.map( l => Link( l.id, l.url.getOrElse( "" ), List() ) ) )
    )

  def createTag( tag: model.Tag ): Future[Int] =
    observeDbTime( Metrics.putTagLatency, tags.insertOrUpdate( DbTag( tag.id, tag.name.some ) ) )

  def toTag: scala.Seq[DbTag] => List[model.Tag] =
    list => list.map( t => model.Tag( t.id, t.name.getOrElse( "" ) ) ).toList

  def getTagsByLinkId( id: Long ): Future[List[model.Tag]] =
    observeDbTime( Metrics.getTagByLinkIdLatency, queryTagsByLinkId( id ).result.map( toTag ) )
  def getAllTags: Future[List[model.Tag]] = observeDbTime( Metrics.getTagsLatency, tags.result.map( toTag ) )

  def getAllLinks( channelQuery: String ): DmlIO[List[Link]] =
    for {
      linksBuilder <- foldStream( links.result, new LinkBuilder( channelQuery.some ) )
      linksIds = linksBuilder.linksIds
      withLinkTag <- foldStream( getLinkTags( linksIds ).result, linksBuilder )
      tagsIds = withLinkTag.tagsIds
      withTags <- foldStream( getTags( tagsIds ).result, withLinkTag )
    } yield withTags.build

  def buildAllLinks: DmlIO[List[Link]] =
    for {
      linksBuilder <- foldStream( links.result, new LinkBuilder( none ) )
      linksIds = linksBuilder.linksIds
      withLinkTag <- foldStream( getLinkTags( linksIds ).result, linksBuilder )
      tagsIds = withLinkTag.tagsIds
      withTags <- foldStream( getTags( tagsIds ).result, withLinkTag )
    } yield withTags.build

  def getLinksById( linkId: Long ): DmlIO[Link] =
    for {
      linksBuilder <- foldStream( links.filter( _.id === linkId ).result, new LinkBuilder( none ) )
      linksIds = linksBuilder.linksIds
      withLinkTag <- foldStream( getLinkTags( linksIds ).result, linksBuilder )
      tagsIds = withLinkTag.tagsIds
      withTags <- foldStream( getTags( tagsIds ).result, withLinkTag )
    } yield withTags.build.head

  private[this] class LinkBuilder( channelQuery: Option[String] )
      extends CasesBuilder[DbLink :+: DbLinkTag :+: DbTag :+: CNil, List[Link]] {
    private val linkById: MutMap[Long, DbLink]           = MutMap()
    private val tagsIdByLink: MutMap[Long, MutSet[Long]] = MutMap()
    private val tagsById: MutMap[Long, DbTag]            = MutMap()
    private val timer: Timer                             = Timer()
    def linksIds: sc.Set[Long]                           = linkById.keySet
    def tagsIds: sc.Set[Long]                            = tagsIdByLink.flatMap { case ( _, t ) => t }.toSet

    val toTag: DbTag => model.Tag = tag => Tag( tag.id, tag.name.getOrElse( "" ) )

    def getTagsForLink( id: Long ): List[model.Tag] =
      tagsIdByLink.getOrElse( id, sc.Set() ).toList.flatMap( id => tagsById.get( id ) ).map( toTag )

    def toLink: ( ( Long, DbLink ) ) => Link = {
      case ( id, link ) =>
        Link( id, link.url.getOrElse( "" ), getTagsForLink( id ) )
    }

    def filterByQuery( links: List[Link], str: String ): List[Link] = {
      Parser.parseBnf( str ).fold( links ) { expr =>
        links.filter { link =>
          val parsedTags = link.tags.map { tag =>
            Parser.isMandatoryOrAbsent( expr, tag.name )
          }
          parsedTags.exists( _.isDefined )
        }
      }
    }

    override def build: List[Link] = {
      Metrics.streamingLatency.labelValues( "channeloverview" ).observeDuration( timer )
      val fullList = linkById.toMap.map( toLink ).toList
      channelQuery.fold( fullList ) { query =>
        filterByQuery( fullList, query )
      }
    }

    object append extends Poly1 {
      implicit def atLink: Case.Aux[DbLink, This]       = at[DbLink].apply[This]( addLink )
      implicit def atLinkTag: Case.Aux[DbLinkTag, This] = at[DbLinkTag].apply[This]( addLinkTag )
      implicit def atTag: Case.Aux[DbTag, This]         = at[DbTag].apply[This]( addTag )
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
