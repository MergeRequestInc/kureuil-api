package eu.epitech.kureuil
package backend
package slick3

import cats.syntax.option._
//
import scala.{collection => sc}
//
import model._

trait LinkQueries { self: Queries with DbContext with TimerObserver with StreamingSupport with Tables =>

  import profile.api._

  def deleteLinkById( linkId: Long ): DmlIO[Unit] = {
    for {
      _ <- linkTags.filter( _.idLink === linkId ).delete
      _ <- links.filter( _.id === linkId ).delete
    } yield ()
  }

  def getLinkFromModel( link: Link ) =
    for {
      l <- links if l.id === link.id
    } yield (l)

  def upsertLink( link: Link ): DmlIO[Unit] = {
    val dbLink = DbLink( link.id, link.url.some )
    val dbTags = link.tags.map { tag =>
      DbTag( tag.id, tag.name.some )
    }
    for {
      l <- getOrInsertLink( dbLink )
      t <- DmlIO.sequence( dbTags.map { dbTag =>
            insertDbTag( l.id )( dbTag )
          } )
      _ <- linkTags.filter( _.idLink === l.id ).delete
      _ <- linkTags ++= t
    } yield ()
  }

  def insertDbTag( linkId: Long )( tag: DbTag ) = {
    for {
      t <- getOrInsertTag( tag )
    } yield DbLinkTag( linkId, t.id )
  }

  def getOrInsertLink = new UpdateOrInsert[Long, String, DbLink, DbLinks](
    links,
    _.id,
    (v: DbLink) => (r: DbLinks) => r.id === v.id,
    ( v, k ) => v.copy( id = k ),
    _.url.getOrElse( "" ),
    _.url.getOrElse( "" )
  )

  def getOrInsertTag = new UpdateOrInsert[Long, String, DbTag, DbTags](
    tags,
    _.id,
    (v: DbTag) => (r: DbTags) => r.id === v.id || r.name.getOrElse( "a" ) === v.name.getOrElse( "b" ),
    ( v, k ) => v.copy( id = k ),
    _.name.getOrElse( "" ),
    _.name.getOrElse( "" )
  )

  def queryTagsByLinkId( id: Long ) =
    for {
      l <- linkTags if l.idLink === id
      t <- tags if t.id === l.idTag
    } yield t

  def getLinkTags( ids: sc.Set[Long] ) =
    linkTags.filter( _.idLink inSet ids )

  def getTags( ids: sc.Set[Long] ) =
    tags.filter( _.id inSet ids )

}
