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

mappings in Universal += {
  // we are using the reference.conf as default application.conf
  // the user can override settings here
  val conf = (resourceDirectory in Compile).value / "application.conf"
  conf -> "/etc/subsequence/application.conf"
}