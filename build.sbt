name := "spark-streaming-kafka"

version := "1.0"

scalaVersion := "2.10.5"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls"
)

val sparkVersion = "1.5.1"
val hbaseVersion = "1.1.1"
val hadoopVersion = "2.6.0"
val lang3Version = "3.0"
val jacksonVersion = "3.2.11"

val hbaseCommon = "org.apache.hbase" % "hbase-common" % hbaseVersion % "compile"
val hbaseClient = "org.apache.hbase" % "hbase-client" % hbaseVersion % "compile"
val hbaseServer = "org.apache.hbase" % "hbase-server" % hbaseVersion % "compile" excludeAll ExclusionRule(organization = "org.mortbay.jetty")
val streaming = "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion % "compile"

val lang3 = "org.apache.commons" % "commons-lang3" % lang3Version
val jackson = "org.json4s" %% "json4s-jackson" % jacksonVersion % "provided"

libraryDependencies ++= Seq(
  hbaseCommon,
  hbaseClient,
  hbaseServer,
  streaming,
  lang3,
  jackson
)

doc in Compile <<= target.map(_ / "none")

fork in Test := true