name := """subsequence"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)


import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

maintainer in Linux := "Subsequence Project <info@subsequence.io>"

packageSummary in Linux := "A scalable plug-and-play Bitcoin Payments Infrastructure"

packageDescription := "Accept Bitcoin payments without relying on a third party service"

javaOptions in Linux ++= Seq(
  "-Dhttp.port=80", "-Dconfig.file=/etc/subsequence/prod.conf", "-Dpidfile.path=/var/run/subsequence/play.pid"
)
