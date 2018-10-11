import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.debian.JDebPackaging

import sbt._
import Keys._

enablePlugins( DependenciesPlugin )

val ci = TaskKey[Unit]( "ci", "Run the CI build tasks" )

val sharedSettings =
  Defaults.coreDefaultSettings ++
    Seq(
      scmInfo := Some(
        ScmInfo(
          url( "https://github.com/MergeRequestInc/kureuil-api" ),
          "scm:git@github.com/MergeRequestInc/kureuil-api.git"
        )
      ),
      publishArtifact in (IntegrationTest, packageBin) := true,
      // enable publishing the integration test sources jar
      publishArtifact in (IntegrationTest, packageSrc) := true,
      maintainer := "Alexandre Vanhecke <alexandre1.vanhecke@epitech.eu>"
    )


val prometheus = Seq( "org.lyranthe.prometheus" %% "client" % "0.9.0-M1" )

val kamon = Seq(
  "io.kamon" %% "kamon-core"          % "1.1.3",
  "io.kamon" %% "kamon-akka-2.5"      % "1.1.1",
  "io.kamon" %% "kamon-akka-http-2.5" % "1.1.0",
  "io.kamon" %% "kamon-prometheus"    % "1.1.1"
)

val commonDependencies: Deps =
  kindProjector ++
    splain ++
    cats ++
    mouse ++
    enumeratum ++
    java8compat ++
    scalatest ++
    scalacheck

val kureuilSettings =
  sharedSettings ++
    Seq(
      libraryDependencies ++= commonDependencies,
      fork in Test := true,
      javaOptions in Test += "-Duser.timezone=UTC"
    )

val sharedFlywaySettings = Seq(
  flywayBaselineVersion := "1",
  flywayGroup := true,
  flywayLocations := Seq( "classpath:db.migration" ),
  flywayUser := "kureuil",
  flywayPassword := "kureuil",
  flywayBaseline := flywayBaseline.dependsOn( copyResources in Compile ).value,
  flywayInfo := flywayInfo.dependsOn( copyResources in Compile ).value,
  flywayMigrate := flywayMigrate.dependsOn( copyResources in Compile ).value,
  flywayRepair := flywayRepair.dependsOn( copyResources in Compile ).value,
  flywayValidate := flywayValidate.dependsOn( copyResources in Compile ).value
)

val `kureuil-api-codegen` = project
  .settings( kureuilSettings )
  .settings( libraryDependencies ++= slick ++ postgresql ++ slickCodegen ++ pureconfig )
  .settings( mainClass := Some( "eu.epitech.kureuil.schema.Main" ) )
  .settings( initialCommands := "import eu.epitech.kureuil.schema._" )
  .enablePlugins( KureuilPlugin, FormatPlugin )

val `kureuil-api-migrations` = project
  .settings( kureuilSettings )
  .settings( sharedFlywaySettings )
  .settings( flywayUrl := "jdbc:postgresql://localhost:5432/kureuil" )
  .settings( libraryDependencies ++= flywayCore ++ postgresql )
  .enablePlugins( FlywayPlugin )
  .enablePlugins( KureuilPlugin, FormatPlugin )

val `kureuil-model` = project
  .settings( kureuilSettings )
  .settings( Console.modelImports.settings )
  .settings( libraryDependencies ++= circe ++ monocle ++ univocity ++ scalaArm )
  .enablePlugins( KureuilPlugin, FormatPlugin )

val `kureuil-api` = project
  .settings( kureuilSettings )
  .settings( Packaging.apiSettings )
  .settings( Console.apiImports.settings )
  .settings( dependencyOverrides ++= kamon )
  .settings(
    libraryDependencies ++=
      slick ++
        logging ++
        akkaHttp ++
        monocle ++
        circe ++
        pureconfig ++
        h2database ++
        jose4j ++
        prometheus ++
        kamon
  )
  .settings( mainClass := Some( "eu.epitech.kureuil.Main" ) )
  .settings( Db.slickBindingsPath := sourceDirectory.value / "gen" / "scala",
            Db.slickBindingsPackage := "eu.epitech.kureuil.backend.slick3" )
  .settings( Db.slickCodeGenSettings )
  .settings( Db.tasksSettings( `kureuil-api-codegen`, `kureuil-api-migrations` ) )
  .settings( javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.1" )
  .dependsOn( `kureuil-model` % "compile->compile;test->test" )
  .dependsOn( `kureuil-api-migrations` )
  .enablePlugins( JavaAgent, JavaServerAppPackaging, SystemdPlugin, JDebPackaging )
  .enablePlugins( KureuilPlugin, FormatPlugin )

val `kureuil-api-it` = project
  .settings( kureuilSettings )
  .settings( Console.apiImports.settings )
  .settings( sharedFlywaySettings )
  .settings(
    flywayUrl := "jdbc:postgresql://localhost:5432/kureuil_it",
    flywayCleanOnValidationError := true,
    test in Test := (test in Test).dependsOn( flywayMigrate ).value
  )
  .dependsOn( `kureuil-api` % "compile->compile;test->test" )
  .enablePlugins( FlywayPlugin, KureuilPlugin, FormatPlugin )

val `kureuil-api-run` = project
  .settings( kureuilSettings )
  .settings( Console.apiImports.settings )
  .settings( mainClass in Compile := Some( "eu.epitech.kureuil.Main" ) )
  .dependsOn( `kureuil-api` % "compile->compile;test->test" )
  .enablePlugins( KureuilPlugin, FormatPlugin )

val `kureuil-api-parent` = project
  .in( file( "." ) )
  .settings( sharedSettings )
  .settings( publish := {}, publishLocal := {} )
  .settings( ci := {

    Def.taskDyn {
      def sequence( tasks: Def.Initialize[Task[Unit]]* ): Def.Initialize[Task[Unit]] = {
        def go( tasks: List[Def.Initialize[Task[Unit]]] ): Def.Initialize[Task[Unit]] =
          tasks match {
            case Nil =>
              Def.task {
                ()
              }
            case t :: ts =>
              Def.taskDyn {
                val _ = t.value
                go( ts )
              }
          }

        go( tasks.toList )
      }

      sequence(
        test.in( `kureuil-model`, Test ),
        test.in( `kureuil-api`, Test ),
        test.in( `kureuil-api-it`, Test )
      )
    }.value
  } )
  .aggregate( `kureuil-model`,
             `kureuil-api-codegen`,
             `kureuil-api-migrations`,
             `kureuil-api`,
             `kureuil-api-run` )
