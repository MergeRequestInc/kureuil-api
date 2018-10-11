package eu.epitech.kureuil

import com.typesafe.scalalogging.Logger

object Loggers {
  lazy val Main: Logger       = Logger( "KUREUIL.MAIN" )
  lazy val Exceptions: Logger = Logger( "KUREUIL.EXCEPTIONS" )
  lazy val Tls: Logger        = Logger( "KUREUIL.TLS" )
  lazy val HexDumps: Logger   = Logger( "KUREUIL.HEX" )
  lazy val Auth: Logger       = Logger( "KUREUIL.AUTH" )
  lazy val Access: Logger     = Logger( "KUREUIL.ACCESS" )
}
