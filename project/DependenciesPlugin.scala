package eu.epitech

import sbt._
import sbt.Keys._
import scala.language.implicitConversions

object DependenciesPlugin extends AutoPlugin {

  type Deps = Seq[ModuleID]

  override def requires = super.requires && KureuilPlugin

  def group( organization: String, version: String )( artifacts: String* )( testArtifacts: String* ): Seq[ModuleID] =
    artifacts.map( organization %% _ % version ) ++ testArtifacts.map( organization %% _ % version % "test" )

  object autoImport {
    type Deps = DependenciesPlugin.Deps

    def depsGroup( organization: String, version: String )( artifacts: String* )(
        testArtifacts: String* ): Seq[ModuleID] =
      DependenciesPlugin.group( organization, version )( artifacts: _* )( testArtifacts: _* )

    implicit def ToGroupOps( deps: Deps ): GroupOps = new GroupOps( deps )

    val kindProjector: Deps = Seq( compilerPlugin( "org.spire-math" %% "kind-projector" % "0.9.8" ) )
    val splain: Deps        = Seq( compilerPlugin( "io.tryp"        % "splain"          % "0.3.3" cross CrossVersion.patch ) )

    val catsVersion    = "1.4.0"
    val cats: Deps     = Seq( "org.typelevel" %% "cats-core" % catsVersion )
    val catsFree: Deps = Seq( "org.typelevel" %% "cats-free" % catsVersion )
    // for overrides only
    private[DependenciesPlugin] val catsOther: Deps =
      group( "org.typelevel", catsVersion )( "cats-kernel", "cats-macros" )()

    val catsEffectVersion = "1.0.0"
    val catsEffect: Deps  = Seq( "org.typelevel" %% "cats-effect" % catsEffectVersion )

    val mouse: Deps = group( "org.typelevel", "0.18" )( "mouse" )()

    val scalaArm: Deps = group( "com.jsuereth", "2.0" )( "scala-arm" )()
    val monocleVersion = "1.5.1-cats"
    val monocle: Deps =
      group( "com.github.julien-truffaut", monocleVersion )( "monocle-core", "monocle-macro" )()

    val circeVersion = "0.10.0"
    val circe: Deps =
      group( "io.circe", circeVersion )( "circe-core", "circe-generic", "circe-parser" )()
    val circeOptics = Seq( "io.circe" %% "circe-optics" % circeVersion )

    val akkaVersion           = "2.5.17"
    val akkaHttpVersion       = "10.1.5"
    val akkaHttpTestkit: Deps = Seq( "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion )

    val akkaHttp: Deps =
      group( "com.typesafe.akka", akkaVersion )( "akka-actor", "akka-stream", "akka-slf4j" )() ++
        group( "com.typesafe.akka", akkaHttpVersion )( "akka-http" )( "akka-http-testkit" ) ++
        Seq( "de.heikoseeberger" %% "akka-http-circe" % "1.22.0" )

    val akkaHttpCors = Seq( "ch.megard" %% "akka-http-cors" % "0.3.4" )

    val jsonWebToken = Seq( "io.igl" %% "jwt" % "1.2.2" )

    val sslConfigCore   = Seq( "com.typesafe"        %% "ssl-config-core" % "0.2.2" )
    val reactiveStreams = Seq( "org.reactivestreams" % "reactive-streams" % "1.0.2" )

    val enumeratum: Deps =
      Seq( "com.beachape" %% "enumeratum" % "1.5.13", "com.beachape" %% "enumeratum-circe" % "1.5.18" )

    val shapeless: Deps = Seq( "com.chuusai" %% "shapeless" % "2.3.3" )

    val java8compat: Deps      = Seq( "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0" )
    val scalaLangModules: Deps = group( "org.scala-lang.modules", "1.0.6" )( "scala-parser-combinators", "scala-xml" )()

    val logging: Deps = Seq( "org.slf4j" % "slf4j-api" % "1.7.25",
                            "ch.qos.logback"             % "logback-classic" % "1.2.3",
                            "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.0" )

    val pureconfigVersion = "0.9.2"
    val pureconfig: Deps =
      group( "com.github.pureconfig", pureconfigVersion )( "pureconfig", "pureconfig-cats" )() :+
        ("com.typesafe" % "config" % "1.3.3")
    val pureconfigEnumeratum: Deps =
      Seq( "com.github.pureconfig" %% "pureconfig-enumeratum" % pureconfigVersion )

    val decline: Deps = Seq( "com.monovore" %% "decline" % "0.5.1" )

    val doobieVersion = "0.6.0-RC1"
    val doobie =
      depsGroup( "org.tpolecat", doobieVersion )( "doobie-core", "doobie-postgres" )( "doobie-h2", "doobie-scalatest" )

    val slickVersion       = "3.2.3"
    val slick: Deps        = group( "com.typesafe.slick", slickVersion )( "slick", "slick-hikaricp" )()
    val slickCodegen: Deps = Seq( "com.typesafe.slick" %% "slick-codegen" % slickVersion )

    val postgresql: Deps     = Seq( "org.postgresql" % "postgresql" % "42.2.5" )
    val h2databaseMain: Deps = Seq( "com.h2database" % "h2" % "1.4.197" )
    val h2database: Deps     = h2databaseMain % "test"
    val flywayCore: Deps     = Seq( "org.flywaydb" % "flyway-core" % "5.0.0" )

    val univocity: Deps = Seq( "com.univocity" % "univocity-parsers" % "2.7.6" )

    val jose4j: Deps = Seq( "org.bitbucket.b_c" % "jose4j" % "0.6.4" )

    val scalatestMain: Deps = Seq( "org.scalatest" %% "scalatest" % "3.0.5" )
    val scalatest: Deps     = scalatestMain % "test"

    val scalacheckMain: Deps =
      Seq(
        "org.scalacheck"      %% "scalacheck"      % "1.13.5",
        "io.github.amrhassan" %% "scalacheck-cats" % "0.4.0"
      )
    val scalacheck: Deps = scalacheckMain % "test"

    val prometheus = Seq( "org.lyranthe.prometheus" %% "client" % "0.9.0-M5" )

    val kamon = Seq(
      "io.kamon" %% "kamon-core"          % "1.1.3",
      "io.kamon" %% "kamon-akka-2.5"      % "1.1.2",
      "io.kamon" %% "kamon-akka-http-2.5" % "1.1.1",
      "io.kamon" %% "kamon-prometheus"    % "1.1.1"
    )

    // kamon & doobie introduce version conflict of these
    val lihaoyi = Seq( "com.lihaoyi" %% "sourcecode" % "0.1.4", "com.lihaoyi" %% "fansi" % "0.2.5" )
  }

  import autoImport._

  override def buildSettings: Seq[Def.Setting[_]] =
    dependencyOverrides in ThisBuild ++= Seq(
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ) ++
      cats ++
      catsFree ++
      catsOther ++
      mouse ++
      scalaArm ++
      monocle ++
      circe ++
      akkaHttp ++
      sslConfigCore ++
      reactiveStreams ++
      enumeratum ++
      shapeless ++
      java8compat ++
      scalaLangModules ++
      logging ++
      pureconfig ++
      slick ++
      slickCodegen ++
      postgresql ++
      h2database ++
      flywayCore ++
      univocity ++
      jose4j ++
      scalatest ++
      scalacheck ++
      prometheus ++
      kamon ++
      lihaoyi

  class GroupOps( val self: Seq[ModuleID] ) extends AnyVal {
    def exclude( org: String, name: String ): Seq[ModuleID] =
      self.map( _.exclude( org, name ) )

    def %( configurations: String ): Seq[ModuleID] =
      self.map( _ % configurations )

    def classifier( c: String ): Seq[ModuleID] =
      self.map( _ classifier c )
  }

}
