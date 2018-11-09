import sbt._
import Keys._
import com.typesafe.sbt.SbtPgp.autoImportImpl.{ pgpPassphrase, pgpPublicRing, pgpSecretRing }
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.autoImport.scalariformPreferences

object ProjectSettings {

  def apply() = Seq(
    name := "voltdbscala",

    organization := "com.full360",

    version := "0.8.0.8",

    scalaVersion := "2.11.12",

    crossScalaVersions := Seq("2.11.12", "2.12.6"),

    libraryDependencies ++= libraryDependencies_,

    scalacOptions ++= scalacOptions_,

    scalacOptions in (Compile, doc) ++= Seq("-no-link-warnings"),

    parallelExecution in Test := false,

    formatSettings
  )

  lazy val libraryDependencies_ = Seq(
    "org.voltdb"              % "voltdbclient"   % "8.3" % "provided",
    "org.scalatest"          %% "scalatest"      % "3.0.0" % "test",
    "org.mockito"             % "mockito-all"    % "1.9.5" % "test"
  )

  lazy val scalacOptions_ = Seq(
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
    "-Xlint:-unused",
    "-Ywarn-unused-import"
  )

  val formatSettings = scalariformPreferences := scalariformPreferences.value
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentConstructorArguments, true)
}
