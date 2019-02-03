package eu.epitech.kureuil
package api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import io.circe._
import io.circe.generic.semiauto._
import scala.concurrent.ExecutionContext
//
import backend._

class MainRoutes( val backend: KureuilDatabase )( implicit val ec: ExecutionContext ) {

  case class UpdateChannel( id: Long, name: String, query: String )
  object UpdateChannel {
    implicit def channelEncoder: Encoder[UpdateChannel] = deriveEncoder
    implicit def channelDecoder: Decoder[UpdateChannel] = deriveDecoder
  }

  case class PostChannel( name: String, query: String )
  object PostChannel {
    implicit def channelEncoder: Encoder[PostChannel] = deriveEncoder
    implicit def channelDecoder: Decoder[PostChannel] = deriveDecoder
  }

  import akka.http.scaladsl.server.Directives._
  import HttpMethods._
  //
  import MainRoutes._
  import authz.Directives._
  import metrics.Directives._

  def testAuthz( id: Identifier ): Route =
    authzPrefix( pathTest, GET, Permission.Read )( id ) {
      pathEndOrSingleSlash {
        complete( StatusCodes.OK )
      }
    }

  def channels( id: Identifier ): Route =
    authzPrefix( pathChannels, GET, Permission.Read )( id ) {
      pathEndOrSingleSlash {
        complete( backend.getChannels )
      } ~ path( "user" ) {
        complete( backend.getUserChannels( id.id ) )
      }
    } ~ authzPrefix( pathChannels, POST, Permission.Write )( id ) {
      pathEndOrSingleSlash {
        (post & entity( as[PostChannel] )) { channel =>
          val result = backend.createOrUpdate( model.Channel( 0, channel.name, channel.query, List() ), id.id )
          onComplete( result ) { done =>
            complete( done.map {
              case 0 => ( StatusCodes.BadRequest, "Failed" )
              case _ => ( StatusCodes.Created, "Created" )
            } )
          }
        }
      }
    } ~ authzPrefix( pathChannels, PUT, Permission.Write )( id ) {
      pathEndOrSingleSlash {
        (put & entity( as[UpdateChannel] )) { channel =>
          val result = backend.createOrUpdate( model.Channel( channel.id, channel.name, channel.query, List() ), id.id )
          onComplete( result ) { done =>
            complete( done.map {
              case 0 => ( StatusCodes.BadRequest, "Failed" )
              case _ => ( StatusCodes.OK, "Updated" )
            } )
          }
        }
      }
    } ~ authzPrefix( pathChannels, DELETE, Permission.Write )( id ) {
      (path( LongNumber ) & delete) { id =>
        val result = backend.deleteChannel( id )
        onComplete( result ) { done =>
          complete( done.map {
            case 0 => ( StatusCodes.BadRequest, "Failed" )
            case _ => ( StatusCodes.OK, "OK" )
          } )
        }
      }
    }

  def tags( id: Identifier ): Route =
    authzPrefix( pathTags, GET, Permission.Read )( id ) {
      pathEndOrSingleSlash {
        complete( backend.getAllTags )
      } ~ path( LongNumber ) { id =>
        complete( backend.getTagsByLinkId( id ) )
      }
    } ~ authzPrefix( pathTags, POST, Permission.Write )( id ) {
      pathEndOrSingleSlash {
        (post & entity( as[model.Tag] )) { tag =>
          val result = backend.createTag( tag )
          onComplete( result ) { done =>
            complete( done.map {
              case 0 => ( StatusCodes.BadRequest, "Failed" )
              case _ => ( StatusCodes.Created, "Created or Updated" )
            } )
          }
        }
      }
    }

  def links( id: Identifier ): Route =
    authzPrefix( pathLinks, GET, Permission.Read )( id ) {
      (path( LongNumber ) & get) { id =>
        complete( backend.getLink( id ) )
      } ~ (path( "query" / Segment ) & get) { _ =>
        complete( backend.getAllLinks ) // TODO : HANDLE QUERY CORRECTLY
      }
    } ~ authzPrefix( pathLinks, POST, Permission.Write )( id ) {
      pathEndOrSingleSlash {
        (post & entity( as[model.Link] )) { link =>
          val result = backend.createOrUpdateLink( link )
          onComplete( result ) { done =>
            complete( done.map {
              case 0 => ( StatusCodes.BadRequest, "Failed" )
              case _ => ( StatusCodes.Created, "Created" )
            } )
          }
        }
      }
    } ~ authzPrefix( pathLinks, PUT, Permission.Write )( id ) {
      pathEndOrSingleSlash {
        (put & entity( as[model.Link] )) { link =>
          val result = backend.createOrUpdateLink( link )
          onComplete( result ) { done =>
            complete( done.map {
              case 0 => ( StatusCodes.BadRequest, "Failed" )
              case _ => ( StatusCodes.OK, "Updated" )
            } )
          }
        }
      }
    }

  def route( id: Identifier ): Route =
    channels( id ) ~ tags( id ) ~ links( id ) ~ testAuthz( id )

  def authzPrefix( segment: String, expMethod: HttpMethod, permission: Permission )(
      id: Identifier
  ): Directive0 = {
    val matchFilter: Directive0           = requireSegmentPrefix( segment ) & method( expMethod )
    val permissionOrForbidden: Directive0 = requirePermission( id, permission ) | status( StatusCodes.Forbidden )
    val completionOrNotFound: Directive0 =
      reportMetric( RequestsInProgress( "main" ) ) | status( StatusCodes.NotFound )

    matchFilter & permissionOrForbidden & completionOrNotFound
  }
}

object MainRoutes {
  val pathTest: String     = "test"
  val pathChannels: String = "channels"
  val pathTags: String     = "tags"
  val pathLinks: String    = "links"
}
