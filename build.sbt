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

maintainer in Linux := "Subsequence Project <contact@getsubsequence.com>"

packageSummary in Linux := "A plug and play Bitcoin Payments Infrastructure"

packageDescription := "Accept Bitcoin payments without relying on a third party service"