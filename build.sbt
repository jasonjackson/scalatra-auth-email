organization := "com.example"

name := "scalatra-sbt-prototype"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

scalacOptions += "-Xcheckinit"

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.0.2",
  "org.scalatra" %% "scalatra-scalate" % "2.0.2",
  "org.scalatra" %% "scalatra-auth" % "2.0.2",
  "org.scalatra" %% "scalatra-specs2" % "2.0.2" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "container",
  "net.liftweb" %% "lift-json" % "2.4-M4",
  "net.liftweb" %% "lift-mongodb-record" % "2.4-M4",
  "org.slf4j" % "log4j-over-slf4j" % "1.6.1", 
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.apache.commons" % "commons-email" % "1.2"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
