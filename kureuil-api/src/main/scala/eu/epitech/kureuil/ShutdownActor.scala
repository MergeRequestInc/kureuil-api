package eu.epitech.kureuil

import akka.actor._
import akka.io._
import cats.Eval
import java.net.InetSocketAddress
import scala.concurrent.Future

class ShutdownActor( port: Int, action: Eval[Future[Unit]] ) extends Actor {
  import context.system

  override def receive: Receive = {
    case _: Tcp.Bound =>
      Loggers.Main.info( s"To shut down, connect to TCP port $port or http://127.0.0.1:$port" )
      context.become( bound( sender ) )
  }

  def bound( tcp: ActorRef ): Receive = {
    case Tcp.Connected( _, _ ) =>
      Loggers.Main.info( "Shutdown requested via TCP." )
      sender() ! Tcp.Abort
      tcp ! Tcp.Unbind
    case Tcp.Unbound =>
      Loggers.Main.info( "Shutdown actor unbound." )
      action.value
      ()
  }

  IO( Tcp ) ! Tcp.Bind( self, new InetSocketAddress( "127.0.0.1", port ) )

}
