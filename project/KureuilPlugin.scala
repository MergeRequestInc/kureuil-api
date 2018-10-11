package eu.epitech

import sbt._
import Keys._

object KureuilPlugin extends AutoPlugin {

  val options: Seq[String] = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Ywarn-macros:after",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )

  def workaroundForIntellij( opts: Seq[String] ): Seq[String] =
    if (sys.props.contains( "idea.runid" ))
      forTest( opts )
    else
      opts

  def forTest( opts: Seq[String] ): Seq[String] =
    opts.filterNot( _ == "-Ywarn-value-discard" )

  def forConsole( opts: Seq[String] ): Seq[String] =
    opts.filterNot( Set( "-Xlint", "-Xfatal-warnings", "-Ywarn-unused-import" ) )

  override val projectSettings: Seq[Def.Setting[_]] =
    // format: off
    Seq(
      scalacOptions                         ++= workaroundForIntellij( options ),
      scalacOptions   in Test               ~=  forTest,
      scalacOptions   in (Compile, console) ~=  forConsole,
      scalacOptions   in (Test,    console) :=  forTest( (scalacOptions in (Compile, console)).value ),
      testOptions     in Test               += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
      publishArtifact in Test               := (publishArtifact in Test).value || (publishArtifact in Compile).value
    )
    // format: on

  override val buildSettings: Seq[Def.Setting[_]] = Seq(
    organization := "eu.epitech",
    scalaVersion := "2.12.7",
    conflictManager := ConflictManager.strict
  )
}
