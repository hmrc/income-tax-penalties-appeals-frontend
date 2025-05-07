import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice = Project("income-tax-penalties-appeals-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    pipelineStages := Seq(gzip),
    PlayKeys.playDefaultPort := 9188
  )
  .settings(Test/logBuffered := false)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings *)
  .settings(inConfig(Test)(testSettings): _*)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
    unmanagedSourceDirectories += baseDirectory.value / "test-fixtures",
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings() ++ Seq(
      unmanagedSourceDirectories := Seq(baseDirectory.value / "test-fixtures"),
      Test / javaOptions += "-Dlogger.resource=logback-test.xml"
  ))
  .settings(Test/logBuffered := false)
  .settings(libraryDependencies ++= AppDependencies.it)
