package eu.epitech.kureuil

import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.ConnectionContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.Eval
import cats.data.Validated
import cats.data.ValidatedNel
import cats.instances.future._
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.validated._
import java.util.concurrent.atomic.AtomicBoolean
import kamon.Kamon
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
//
import api.Api
import api.metrics.Routes.metricsRoute
import backend.KureuilDatabase

class ApiApp(
    val config: ApiConfig,
    val mainDb: KureuilDatabase,
    val backEndActorSystem: ActorSystem
)( implicit val system: ActorSystem ) {

  import ApiApp._

  val shuttingDown: AtomicBoolean = new AtomicBoolean( false )

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext            = system.dispatcher

  def start(): Future[Unit] = {

    Loggers.Main.debug(
      s"""kureuil-api initialized with config
         |  $config
         """.stripMargin
    )

    startWithConnectionContext( config.http.connectionContext )
  }

  def startWithConnectionContext( context: ConnectionContext ): Future[Unit] = {

    val api  = Api( config, mainDb )
    val http = Http()

    def bindHttp(
        route: Route,
        listenAddress: String,
        port: Int,
        context: ConnectionContext
    ): Future[Http.ServerBinding] =
      http
        .bindAndHandle( route, listenAddress, port, context )
        .handleErrorWith( e => Future.failed( KureuilApiError( s"Unable to bind HTTP port $port", e :: Nil ) ) )

    sequenceAccumulating(
      "Unable to bind",
      List(
        Eval.now( bindHttp( api.route, config.http.listenAddress, config.http.listenPort, context ) ),
        Eval.now( bindHttp( metricsRoute, config.http.metricsAddress, config.http.metricsPort, context ) )
      )
    ).flatMap( running )

  }

  def running( bindings: List[Http.ServerBinding] ): Future[Unit] = {

    Runtime.getRuntime.addShutdownHook( new Thread( () => Await.result( shutdown( bindings ), 1.minute ) ) )

    val shutdownPort = config.http.shutdownPort

    Loggers.Main.info(
      s"API listening at http://${config.http.listenAddress}:${config.http.listenPort}"
    )

    shutdownPort.foreach(
      p => system.actorOf( Props( new ShutdownActor( p, Eval.later( shutdown( bindings ) ) ) ) )
    )

    system.whenTerminated.map( _ => () )( ExecutionContext.global )
  }

  def shutdown( serverBindings: List[Http.ServerBinding] ): Future[Unit] = {
    import ExecutionContext.Implicits.global

    if (shuttingDown.compareAndSet( false, true )) {

      val unbindActs = serverBindings.map( b => Eval.now( b.unbind() ) )

      def unbindPorts: Future[Unit] =
        sequenceAccumulating( "Unable to unbind", unbindActs )( global ).map( _ => () )( global )

      val actions: List[Eval[Future[Unit]]] =
        Eval.later( mainDb.close() ) ::
          Eval.later( unbindPorts ) ::
          Eval.later( system.terminate().map( _ => () )( global ) ) ::
          Eval.later( backEndActorSystem.terminate().map( _ => () )( global ) ) ::
          Eval.later( Kamon.stopAllReporters() ) :: Nil

      sequenceAccumulating( "Error(s) during shutdown", actions )( global )
        .map( _ => Loggers.Main.info( "Shutdown complete" ) )( global )

    } else Future.unit
  }
}

object ApiApp {
  def sequenceAccumulating[A]( errorMsg: => String, acts: List[Eval[Future[A]]] )(
      implicit ec: ExecutionContext
  ): Future[List[A]] = {
    def loop( xs: List[Eval[Future[A]]] ): Future[ValidatedNel[Throwable, List[A]]] = xs match {
      case Nil => Future.successful( Nil.validNel )
      case h :: t =>
        for {
          eh <- h.value.attempt
          vh = Validated.fromEither( eh ).toValidatedNel
          vt <- loop( t )
        } yield ( vh, vt ).mapN( _ :: _ )
    }
    loop( acts ).flatMap(
      _.fold(
        errs => Future.failed( KureuilApiError( errorMsg, errs.toList ) ),
        Future.successful
      )
    )
  }
}
