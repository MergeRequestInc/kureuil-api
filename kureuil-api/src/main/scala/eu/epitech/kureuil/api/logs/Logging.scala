package eu.epitech.kureuil
package api
package logs

import akka.event.{Logging => AkkaLogging}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.RouteResult
import cats.syntax.option._
import scala.util.control.NonFatal
//
import backend.sqlExceptionStream

class Logging( debugConfig: DebugConfig ) {

  private[logs] val hexDumpEntity: Directive0 =
    if (debugConfig.logRequestHexDump)
      entity( as[Array[Byte]] ).flatMap { ba =>
        Loggers.HexDumps.info( "\n" + HexDump( 16, ba ).toString )
        pass
      } else
      pass

  private[logs] val showRequest: HttpRequest => RouteResult => Option[LogEntry] = req =>
    res => LogEntry( RequestResultPresenter( req, res ), AkkaLogging.InfoLevel ).some

  private[logs] val logRequest: Directive0 =
    if (debugConfig.logAllRequests)
      logRequestResult( showRequest )
    else
      pass

  private[logs] val exceptionHandler = ExceptionHandler {
    case NonFatal( e ) =>
      extractRequest { req =>
        def msg =
          s"""|An error occured while processing a request:
              |    Request:
              |        $req""".stripMargin
        Loggers.Exceptions.warn( msg, e )

        sqlExceptionStream( e ).foreach { ex =>
          Loggers.Exceptions.warn( "Next SQL exception", ex )
        }

        hexDumpEntity {
          complete( StatusCodes.InternalServerError )
        }
      }
  }

  val directive: Directive0 =
    logRequest & handleExceptions( exceptionHandler )

}
