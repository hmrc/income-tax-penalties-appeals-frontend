import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.5.0"
  

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"   %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"   %% "play-frontend-hmrc-play-30" % "11.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% "bootstrap-test-play-30"     % bootstrapVersion    % Test,
    "org.jsoup"      %  "jsoup"                      % "1.18.1"            % Test
  )

  val it: Seq[Nothing] = Seq.empty
}
