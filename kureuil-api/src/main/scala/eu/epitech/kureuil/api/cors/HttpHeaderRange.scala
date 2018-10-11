package eu.epitech.kureuil
package api
package cors

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.ModeledCompanion
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Headers`
import java.util.Locale
import mouse.boolean._

sealed trait HttpHeaderRange {

  def matches( header: String ): Boolean
  def matches( header: HttpHeader ): Boolean = matches( header.lowercaseName )

  def allowedHeaders( requestHeaders: Seq[String] ): Option[`Access-Control-Allow-Headers`]
}

object HttpHeaderRange {
  object `*` extends HttpHeaderRange {
    override def matches( header: String ): Boolean = true

    override def allowedHeaders( requestHeaders: Seq[String] ): Option[`Access-Control-Allow-Headers`] =
      requestHeaders.nonEmpty.option(
        `Access-Control-Allow-Headers`( requestHeaders )
      )
  }

  case class Default( headers: Seq[ModeledCompanion[_ <: HttpHeader]] ) extends HttpHeaderRange {
    override def matches( header: String ): Boolean =
      headers.exists( h => h.lowercaseName == header.toLowerCase( Locale.ROOT ) )

    override def allowedHeaders( requestHeaders: Seq[String] ): Option[`Access-Control-Allow-Headers`] =
      Some( accessControlAllowHeaders )

    private val accessControlAllowHeaders =
      `Access-Control-Allow-Headers`( headers.map( _.name ) )
  }

  def apply( headers: ModeledCompanion[_ <: HttpHeader]* ): HttpHeaderRange =
    Default( headers.to[Seq] )

}
