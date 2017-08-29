val scalatest = "org.scalatest" %% "scalatest" % "3.0.1"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.15"

val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2" withSources()

lazy val commonsLang = "org.apache.commons" % "commons-lang3" % "3.4"

lazy val jparsec = "org.jparsec" % "jparsec" % "2.2.1"

lazy val junit = "junit" % "junit" % "4.11" % "test"

lazy val scaladeps = Seq(scalatest, scalacheck, scalaz, parserCombinators, commonsLang)
lazy val javadeps = Seq(jparsec, junit, commonsLang, "com.novocode" % "junit-interface" % "0.11" % "test")

lazy val root = (project in file(".")).
  settings(
    name := "evgeniy-portfolio",
    organization := "com.github.oneugene",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.12.3",
    libraryDependencies ++= scaladeps ++ javadeps/*,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")*/
  )
