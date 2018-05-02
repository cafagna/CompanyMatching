name := "matching-service"

scalaVersion := "2.11.7"

enablePlugins(PlayJava)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.14" % Compile
)