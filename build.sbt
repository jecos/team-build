name := "team-build"

version := "1.0"

scalaVersion := "2.11.7"


lazy val commonSettings = Seq(
  organization := "com.jecos",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

lazy val user = (project in file("user")).
  settings(commonSettings: _*)

lazy val league = (project in file("league")).
  settings(commonSettings: _*)