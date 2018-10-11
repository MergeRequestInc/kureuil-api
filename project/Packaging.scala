import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerKeys
import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.Debian
import com.typesafe.sbt.packager.Keys._

object Packaging {
  val commonSettings = Seq(
    maintainer := "Alexandre Vanhecke <alexandre1.vanhecke@epitech.eu>",
    debianPackageDependencies in Debian := Seq( "openjdk-8-jre-headless" )
  )

  val apiSettings = commonSettings ++ Seq(
    packageDescription := "kureuil API",
    packageSummary := "kureuil API",
    serverLoading := Some( ServerLoader.Systemd ),
    NativePackagerKeys.bashScriptExtraDefines ++= Seq(
      """addJava "-Dconfig.file=/etc/kureuil-api/application.conf"""",
      """addJava "-Dlogback.configurationFile=/etc/kureuil-api/logback.xml"""",
      """addJava "-Duser.timezone=UTC""""
    )
  )
}
