name := "voltdbscala"

organization := "com.full360"

version := "0.1.0"


libraryDependencies ++= Seq(
  "org.voltdb"              % "voltdbclient"   % "6.5"   % "provided",
  "org.scala-lang.modules"  % "scala-xml_2.11" % "1.0.5" % "provided",
  "org.scalatest"          %% "scalatest"      % "3.0.0" % "test"
)

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Ywarn-unused-import"
)

/**
  * Code formatting
  */
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(RewriteArrowSymbols, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
