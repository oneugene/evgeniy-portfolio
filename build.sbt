lazy val scalatest = Seq("org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "test")
lazy val scalaz = Seq("org.scalaz" %% "scalaz-core" % "7.1.1")

lazy val parserCombinators = Seq ("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2" withSources())

lazy val commonsLang = Seq ("org.apache.commons" % "commons-lang3" % "3.4")

lazy val jparsec = Seq ("org.jparsec" % "jparsec" % "2.2.1")
lazy val junit = Seq ("junit" % "junit" % "4.11")

lazy val scaladeps =scalatest ++ scalaz ++ parserCombinators ++ commonsLang
lazy val javadeps = jparsec ++ junit ++ commonsLang

lazy val root = (project in file(".")).
  settings(
    name := "evgeniy-portfolio",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= scaladeps ++ javadeps
  )
