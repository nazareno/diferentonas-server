name := """diferentonas-server"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

PlayKeys.externalizeResources := false

resolvers += "UUID from eaio.com" at "http://repo.eaio.com/maven2"

libraryDependencies ++= Seq(
  filters,
  javaJdbc,
  jdbc,
  javaJpa,
  javaWs,
  "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
  "org.postgresql" % "postgresql" % "9.4.1207.jre7",
  "com.eaio.uuid" % "uuid" % "3.4"
)

herokuAppName in Compile := "diferentonas"

herokuIncludePaths in Compile := Seq(
  "app", "conf/routes", "dist/data"
)

fork in run := true
