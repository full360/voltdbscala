import sbt._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import Keys._
import com.typesafe.sbt.SbtPgp.autoImportImpl.{ pgpPassphrase, pgpPublicRing, pgpSecretRing }

object ProjectSettings {

  def apply() = Seq(
    name := "voltdbscala",

    organization := "com.full360",

    version := "0.5.0-SNAPSHOT",

    scalaVersion := "2.11.12",

    crossScalaVersions := Seq("2.11.12", "2.12.6"),

    libraryDependencies ++= libraryDependencies_,

    scalacOptions ++= scalacOptions_,

    scalacOptions in (Compile, doc) ++= Seq("-no-link-warnings"),

    pomExtra := pomExtra_,

    parallelExecution in Test := false,

    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      sys.env.getOrElse("SONATYPE_USERNAME", ""),
      sys.env.getOrElse("SONATYPE_PASSWORD", "")),

    pgpSecretRing := file(".secring.gpg"),
    pgpPublicRing := file(".pubring.gpg"),
    pgpPassphrase := sys.env.get("SONATYPE_KEY_PASSPHRASE").map(_.toArray)

  ) ++ formatSettings

  lazy val libraryDependencies_ = Seq(
    "org.voltdb"              % "voltdbclient"   % "6.8" % "provided",
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
