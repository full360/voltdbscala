/*
 * Copyright © 2018 Full 360 Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.typesafe.sbt.SbtPgp.autoImportImpl._
import sbt.Keys._
import sbt.{ Credentials, _ }

object Publish {

  private lazy val credential = Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    sys.env.getOrElse("SONATYPE_USERNAME", ""),
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  )

  private lazy val pom = {
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

  def apply() = Seq(
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),

    credentials += credential,

    publishArtifact in Compile := true,
    publishArtifact in Test := false,
    publishMavenStyle := true,

    pomExtra := pom,

    pgpSecretRing := file(".secring.gpg"),
    pgpPublicRing := file(".pubring.gpg"),
    pgpPassphrase := sys.env.get("SONATYPE_KEY_PASSPHRASE").map(_.toArray)
  )
}