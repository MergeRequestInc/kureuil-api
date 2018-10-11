package eu.epitech.kureuil

import akka.actor.ActorSystem
import cats.instances.either._
import cats.syntax.either._
import cats.syntax.functor._
import java.nio.file.Paths
import kamon.Kamon
import pureconfig._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
//
import backend.slick3
import backend.slick3.Migration
import backend.KureuilDatabase

object Main extends App {

  Loggers.Main.info(
    s"Startup of kureuil-api in ${Paths.get( "." ).toAbsolutePath}"
  )

  Kamon.addReporter( prometheus.Metrics.kamonReporter )
  Kamon.loadReportersFromConfig()

  def loadAppConfig: Either[KureuilApiError, ApiConfig] =
    loadConfig[ApiConfig].leftMap(
      fs =>
        KureuilApiError(
          fs.toList.foldLeft( "Error(s) in configuration:" )( ( msg, err ) => msg + s"\n  $err" )
        )
    )

  def playFlywayMigrations( apiConfig: ApiConfig ): Either[Throwable, Unit] =
    Migration.prepare( apiConfig.kureuilDb, apiConfig.flywayRepairOnStartup ).void

  def initDb( apiConfig: ApiConfig )( implicit system: ActorSystem ): Either[KureuilApiError, KureuilDatabase] =
    slick3.Backend
      .init( apiConfig.kureuilDb )
      .leftMap( err => KureuilApiError( "Unable to initialize database", err :: Nil ) )

  def app(): Future[ApiApp] = {
    implicit val frontEndActorSystem: ActorSystem = ActorSystem( "kureuil-api" )
    val backEndActorSystem: ActorSystem           = ActorSystem( "kureuil-db" )

    val result = for {
      config <- loadAppConfig
      _      <- playFlywayMigrations( config )
      mainDb <- initDb( config )( backEndActorSystem )
    } yield new ApiApp( config, mainDb, backEndActorSystem )

    result.fold(
      err => {
        import ExecutionContext.Implicits.global
        Future
          .sequence(
            Kamon.stopAllReporters() ::
              frontEndActorSystem.terminate() ::
              backEndActorSystem.terminate() ::
              Nil
          )
          .transform( _ => Failure( err ) )
      },
      Future.successful
    )
  }

  Await
    .ready( app().flatMap( _.start() )( ExecutionContext.global ), Duration.Inf )
    .failed
    .foreach( logException )( ExecutionContext.global )

  def logException( t: Throwable ): Unit = logException0( t, "", "" )

  def logException0( t: Throwable, num: String, ind: String ): Unit = t match {
    case KureuilApiError( message, exns ) =>
      Loggers.Main.error( ind + message )
      exns match {
        case Nil =>
        case _ =>
          Loggers.Exceptions.error( s"${ind}KureuilApiError: $message with ${exns.size} cause(s)" )
          exns.zipWithIndex.foreach { case ( exn, ix ) => logException0( exn, s"${ix + 1})", "  " + ind ) }
      }
    case _ =>
      Loggers.Main.error( s"${t.getClass.getSimpleName}: ${t.getMessage}" )
      Loggers.Exceptions.error( s"$ind$num", t )
  }

}
