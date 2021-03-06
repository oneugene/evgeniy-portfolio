import sbt.CrossVersion

val scalaVertion = "2.12.6"
val projectVersion = "1.0-SNAPSHOT"
val rootGroup = "com.github.oneugene.evgeniy-portfolio"

val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.25"

val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1" withSources()

val commonsLang = "org.apache.commons" % "commons-lang3" % "3.7"

val commonsText =  "org.apache.commons" % "commons-text" % "1.4"

val jparsec = "org.jparsec" % "jparsec" % "2.2.1"

val junit = "junit" % "junit" % "4.12" % "test"
val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"

val jmh = "org.openjdk.jmh" % "jmh-core" % "1.21"

val akkaVersion = "2.5.13"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

val monocleVersion = "1.5.1-cats"
val monocleCore = "com.github.julien-truffaut" %% "monocle-core" % monocleVersion
val monocleMacro = "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion

val cats = "org.typelevel" %% "cats-core" % "1.1.0"

lazy val joinProject = (project in file("join")).
  settings(
    name := "join",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(scalatest, scalacheck)
  )

lazy val changelogProject = (project in file("changelog"))
  .settings(
    name := "changelog",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(scalatest, scalacheck, cats, akkaActor, monocleCore, monocleMacro % "test")
  )

lazy val changelogMacroProject = (project in file("changelog-macro"))
  .settings(
    name := "changelog-macro",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    scalacOptions += "-language:experimental.macros",
    libraryDependencies ++= Seq(scalatest, scalacheck, cats,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
    ),
    addCompilerPlugin(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
  )
  .dependsOn(changelogProject)

lazy val changelogDemoProject = (project in file("changelog-demo"))
  .settings(
    name := "changelog-demo",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(scalatest, scalacheck, cats, akkaActor, monocleCore, monocleMacro)
  )
  .dependsOn(changelogProject)
  .dependsOn(changelogMacroProject)

lazy val parsersProject = (project in file("parser")).
  settings(
    name := "parser",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(scalatest, scalacheck, parserCombinators, commonsLang, commonsText, jparsec, junit, junitInterface) /*,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")*/
  )

lazy val performanceTests = (project in file("jmhtests"))
  .enablePlugins(JmhPlugin)
  .settings(
    name := "performanceTests",
    organization := rootGroup,
    version := projectVersion,
    scalaVersion := scalaVertion,
    libraryDependencies ++= Seq(monocleCore, monocleMacro, scalaz, cats, jmh))
  .dependsOn(changelogProject)
  .dependsOn(changelogDemoProject)
  .dependsOn(parsersProject)
