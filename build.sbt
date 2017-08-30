val scalaVertion = "2.12.3"
val projectVersion = "1.0-SNAPSHOT"
val rootGroup = "com.github.oneugene.evgeniy-portfolio"

val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.15"

val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6" withSources()

val commonsLang = "org.apache.commons" % "commons-lang3" % "3.4"

val jparsec = "org.jparsec" % "jparsec" % "2.2.1"

val junit = "junit" % "junit" % "4.11" % "test"
val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"

val scaladeps = Seq(scalatest, scalacheck, scalaz, parserCombinators, commonsLang)
val javadeps = Seq(jparsec, junit, commonsLang, junitInterface)

lazy val joinProject = (project in file("join")).
  settings(
      name := "join",
      organization := rootGroup,
      version := projectVersion,
      scalaVersion := scalaVertion,
      libraryDependencies ++= Seq(scalatest, scalacheck)
  )

lazy val changelogProject = (project in file("changelog")).
  settings(
    name := "changelog",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(scalatest, scalacheck, scalaz)
  )

lazy val parsersProject = (project in file("parser")).
  settings(
    name := "parser",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(scalatest, scalacheck, parserCombinators, commonsLang, jparsec, junit, commonsLang, junitInterface)/*,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")*/
  )
