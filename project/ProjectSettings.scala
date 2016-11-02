import sbt._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import Keys._

object ProjectSettings {

  def apply() = Seq(
    name := "voltdbscala",

    organization := "com.full360",

    version := "0.3.1-SNAPSHOT",

    scalaVersion := "2.11.8",

    libraryDependencies ++= libraryDependencies_,

    scalacOptions ++= scalacOptions_,

    scalacOptions in (Compile, doc) ++= Seq("-no-link-warnings"),

    pomExtra := pomExtra_,

    parallelExecution in Test := false,

    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      sys.props("sonatype.username"),
      sys.props("sonatype.password"))

  ) ++ formatSettings

  lazy val libraryDependencies_ = Seq(
    "org.voltdb"              % "voltdbclient"   % "6.6" % "provided",
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
    "-Ywarn-unused-import"
  )

  def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  }

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test    := formattingPreferences
  )

  lazy val pomExtra_ = {
    <url>https://github.com/full360/voltdbscala</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>http://opensource.org/licenses/MIT</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/full360/voltdbscala.git</connection>
      <developerConnection>scm:git:git@github.com:full360/voltdbscala.git</developerConnection>
      <url>github.com/full360/voltdbscala.git</url>
    </scm>
    <developers>
      <developer>
        <id>diorman</id>
        <name>Diorman Colmenares</name>
      </developer>
    </developers>
  }
}
