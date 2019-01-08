package eu.epitech.kureuil

import akka.http.scaladsl.ConnectionContext
import com.typesafe.config.Config
//
import api.logs.DebugConfig

case class ApiConfig(
    kureuilDb: Config,
    http: HttpConfig,
    debug: DebugConfig,
    flywayRepairOnStartup: Boolean,
    jwtSecretKey: String,
)

case class HttpConfig(
    listenAddress: String,
    listenPort: Int,
    shutdownPort: Option[Int],
    metricsAddress: String,
    metricsPort: Int
) {
  val connectionContext: ConnectionContext = ConnectionContext.noEncryption()
}
