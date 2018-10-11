package eu.epitech.kureuil.api.logs

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.`Remote-Address`
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.RouteResult.Rejected

private[logs] case class RequestResultPresenter( req: HttpRequest, res: RouteResult ) {
  def showUri( uri: Uri ) =
    s"${uri.path}${uri.rawQueryString.fold( "" )( "?" + _ )}"

  def clientAddress( req: HttpRequest ): String =
    req.header[`Remote-Address`].flatMap( _.address.toOption ).fold( "-" )( _.toString )

  def showResult( res: RouteResult ): String = res match {
    case Complete( response ) =>
      response.status.intValue.toString + " " + response.entity.contentLengthOption.fold( "-" )( _.toString )
    case Rejected( rejections ) =>
      rejections.toSet.mkString( "(REJ: ", ", ", ")" )
  }

  override def toString: String =
    clientAddress( req ) + " " +
      "\"" + req.method.value + " " +
      showUri( req.uri ) + " " +
      req.protocol.value + "\" " +
      showResult( res )

}
