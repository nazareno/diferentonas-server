name := """diferentonas-server"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

PlayKeys.externalizeResources := false

libraryDependencies ++= Seq(
  filters,
  javaJdbc,
  jdbc,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
)

