name := "subseq"


version := "1.0"

lazy val `subseq` = (project in file(".")).enablePlugins(play.PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( javaJdbc , javaEbean , cache , javaWs )
