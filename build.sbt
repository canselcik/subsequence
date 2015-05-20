name := "subsequence"


version := "1.0"

lazy val `subsequence` = (project in file(".")).enablePlugins(play.PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( javaJdbc , javaEbean , cache , javaWs )


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  