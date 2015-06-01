import sbt.dsl._

name := """subsequence"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava).enablePlugins(DebianPlugin)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)


maintainer := "Subsequence Project <info@subsequence.io>"

packageSummary := "A scalable plug-and-play Bitcoin Payments Infrastructure"

packageDescription := "Accept Bitcoin payments without relying on a third party service"

javaOptions in Debian ++= Seq(
  "-Dhttp.port=80", "-Dconfig.file=/etc/subsequence/prod.conf", "-Dpidfile.path=/var/run/subsequence/play.pid"
)
