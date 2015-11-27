resolvers += "Eventuate Releases" at "https://dl.bintray.com/rbmhtechnology/maven"

libraryDependencies ++= Seq(
  "com.rbmhtechnology" %% "eventuate" % "0.3",
  "org.scalatest" %% "scalatest" % "2.2.2" % Test
)