import sbt.dsl._

name := """subsequence"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava).enablePlugins(DebianPlugin).enablePlugins(RpmPlugin)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

daemonUser in Linux := "root"

maintainer := "Subsequence Project <info@subsequence.io>"

packageSummary := "A scalable plug-and-play Bitcoin Payments Infrastructure"

packageDescription := "Accept Bitcoin payments without relying on a third party service"


// RPM

rpmVendor := "Subsequence Project"

name in Rpm := "Subsequence"

rpmUrl := Some("https://www.subsequence.io")

version in Rpm := version.value.replace('-','_')

rpmRelease := "1"

rpmLicense := Some("MIT")

rpmGroup := Some("subsequence")