import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = Project("income-tax-penalties-appeals-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(RoutesKeys.routesImport ++= Seq(
    "uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse",
    "uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.ReasonableExcuse._",
    "uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.Mode",
    "uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.NormalMode",
    "uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.CheckMode",
    "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
  ),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.incometaxpenaltiesappealsfrontend.models.Mode"
    ))
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-Werror",
      "-Wconf:msg=unused import&src=html/.*:s",
      "-Wconf:msg=unused import&src=xml/.*:s",
      "-Wconf:msg=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:msg=unused&src=.*Routes\\.scala:s",
      "-Wconf:msg=unused&src=.*ReverseRoutes\\.scala:s",
      "-Wconf:msg=Flag.*repeatedly:s",
      "-Wconf:msg=Setting -Wunused set to all redundantly:s"
    ),
    pipelineStages := Seq(gzip),
    PlayKeys.playDefaultPort := 9188
  )
  .settings(Test/logBuffered := false)
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
      unmanagedSourceDirectories.withRank(KeyRanks.Invisible) := Seq(baseDirectory.value / "test-fixtures"),
      Test / javaOptions += "-Dlogger.resource=logback-test.xml"
  ))
  .settings(Test/logBuffered := false)
  .settings(libraryDependencies ++= AppDependencies.it)
