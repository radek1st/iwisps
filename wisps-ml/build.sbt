enablePlugins(GatlingPlugin)

name := "i-wisps-ml"

version := "1.0.0"

scalaVersion := "2.11.8"

val sparkVersion = "1.6.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core"        % sparkVersion ,
  "org.apache.spark" %% "spark-sql"         % sparkVersion ,
  "org.apache.spark" %% "spark-mllib"       % sparkVersion,
  "com.databricks"   %% "spark-csv"         % "1.4.0"
)

//For Performance Tests
libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.0" % "test",
  "io.gatling"            % "gatling-test-framework"    % "2.2.0" % "test"
)