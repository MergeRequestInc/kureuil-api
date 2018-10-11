import sbt._
import sbt.Keys._

object Console {

  case class Imports( compileList: List[String], testList: List[String] ) {
    def depends( others: Imports* ): Imports =
      Imports( others.foldRight( compileList )( _.compileList ++ _ ), others.foldRight( testList )( _.testList ++ _ ) )

    def compileS = compileList.map( "import " + _ ).mkString( "\n" )
    def testS    = (compileList ++ testList).map( "import " + _ ).mkString( "\n" )

    def settings = Seq(
      initialCommands := compileS,
      initialCommands in Test := testS
    )
  }

  val modelImports = Imports(
    "eu.epitech.kureuil._" ::
      "eu.epitech.kureuil.model._" ::
      "cats._, cats.data._, cats.implicits._" ::
      "io.circe._, io.circe.parser._" ::
      "scala.concurrent._" ::
      "scala.concurrent.duration._" ::
      Nil,
    "org.scalacheck.Gen, org.scalacheck.Gen._" ::
      "eu.epitech.kureuil.gen._" ::
      "scala.concurrent.ExecutionContext.Implicits.global" ::
      Nil
  )

  val apiImports = Imports(
    "eu.epitech.kureuil.api._" ::
      "eu.epitech.kureuil.backend._" ::
      Nil,
    "eu.epitech.kureuil.future._" ::
      "eu.epitech.kureuil.sql._" ::
      "org.scalacheck.Gen, org.scalacheck.Gen._" ::
      "scala.concurrent.ExecutionContext.Implicits.global" ::
      Nil
  ).depends( modelImports )

  val traceImports = Imports(
    "eu.epitech.kureuil.trace._" :: Nil,
    Nil
  ).depends( modelImports )

  val diffImports =
    Imports( "eu.epitech.kureuil.diff._" :: "java.nio.file._" :: Nil, "DataTest._" :: Nil ).depends( modelImports )
}
