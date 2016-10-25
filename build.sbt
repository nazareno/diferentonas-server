name := """diferentonas-server"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

PlayKeys.externalizeResources := false

resolvers += "UUID from eaio.com" at "http://repo.eaio.com/maven2"

libraryDependencies ++= Seq(
  javaJdbc,
  jdbc,
  javaJpa,
  javaWs,
  "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
  "org.postgresql" % "postgresql" % "9.4.1207.jre7",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "com.eaio.uuid" % "uuid" % "3.4",
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "com.nimbusds" % "nimbus-jose-jwt" % "4.22"
)

herokuAppName in Compile := "diferentonas"

herokuIncludePaths in Compile := Seq(
  "app", "conf/routes", "dist/data", "dados-externos"
)

mappings in Universal ++=
  (baseDirectory.value / "dados-externos" * "*" get) map
    (x => x -> ("dados-externos/" + x.getName))

mappings in Universal ++=
  (baseDirectory.value / "R" * "*" get) map
    (x => x -> ("R/" + x.getName))
