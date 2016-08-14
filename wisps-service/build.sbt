name := """i-wisps-service"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.iheart" %% "play-swagger" % "0.2.1-PLAY2.5",
  "org.webjars" % "swagger-ui" % "2.1.4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += Resolver.jcenterRepo

//Docker stuff
packageName in Docker := "radek1st/iwisps"

dockerBaseImage := "radek1st/java8-scala-sbt"

maintainer := "Radek Ostrowski radek@fastdata.com"

dockerExposedPorts in Docker := Seq(80)

daemonUser in Docker := "root"

mappings in Universal += file("models/businesses.tsv") -> "models/businesses.tsv"

mappings in Universal += file("models/users.tsv") -> "models/users.tsv"

mappings in Universal += file("models/perBusiness.tsv") -> "models/perBusiness.tsv"

mappings in Universal += file("models/perCategory.tsv") -> "models/perCategory.tsv"

mappings in Universal += file("models/perUser.tsv") -> "models/perUser.tsv"
